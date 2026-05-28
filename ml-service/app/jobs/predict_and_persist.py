from __future__ import annotations

import argparse
import json
from datetime import datetime
from pathlib import Path

import pandas as pd

from ..db import Db, load_config
from ..features import add_calendar_features, add_event_proximity_features, add_series_lags, encode_entity_features
from ..models.baseline import rolling_mean_forecast, seasonal_naive_7
from ..models.intermittent import croston, sba, tsb
from ..models.xgboost_global import load_xgb, predict_global_xgb
from ..training_data import (
    build_category_panel,
    build_dense_item_panel,
    compute_sparse_allocation_weights,
    load_base_data,
    profile_series
)


def make_future_frame(history, future_dates, event_df):
    last = history.iloc[-1].copy()
    rows = []
    for d in future_dates:
        row = last.copy()
        row["sales_date"] = d
        row["quantity_sold"] = 0.0
        rows.append(row)

    fut = pd.DataFrame(rows)

    for col in list(fut.columns):
        if col.startswith(("lag_", "roll_", "nonzero_ratio_", "days_since_", "last_nonzero_")):
            fut.drop(columns=[col], inplace=True)

    fut = fut.merge(event_df, on=["vendor_id", "sales_date"], how="left", suffixes=("", "_evt"))
    fut["event_flag"] = fut.get("event_flag", 0).fillna(0)
    fut["impact_level"] = fut.get("impact_level", 0).fillna(0)

    out = pd.concat([history, fut], ignore_index=True)
    out = add_calendar_features(out)
    out = add_event_proximity_features(out)
    out = add_series_lags(out)
    out = encode_entity_features(out)
    return out.tail(len(future_dates)).copy()


def insert_run(db, vendor_id, menu_item_id, model_name, horizon):
    db.execute(
        """
        INSERT INTO forecast_runs
        (
            vendor_id,
            menu_item_id,
            model_name,
            model_version,
            horizon_days,
            training_start_date,
            training_end_date,
            feature_set_version,
            mlflow_run_id,
            metrics_json,
            status,
            started_at,
            completed_at,
            created_at,
            updated_at
        )
        VALUES
        (
            %s,%s,%s,'v2',%s,
            CURDATE()-INTERVAL 110 DAY,
            CURDATE()-INTERVAL 1 DAY,
            'strong-ml-v2',
            NULL,
            '{}',
            'SUCCESS',
            NOW(3),
            NOW(3),
            NOW(3),
            NOW(3)
        )
        """,
        (vendor_id, menu_item_id, model_name, horizon)
    )

    return int(db.read_sql("SELECT MAX(id) id FROM forecast_runs")["id"].iloc[0])


def insert_values(db, run_id, future_dates, preds):
    rows = []
    now = datetime.now()

    for d, p in zip(future_dates, preds):
        rows.append(
            (
                run_id,
                pd.Timestamp(d).date(),
                float(p),
                float(max(0.0, p * 0.7)),
                float(max(0.0, p * 1.3)),
                95.0,
                now,
                now
            )
        )

    db.executemany(
        """
        INSERT INTO forecast_values
        (
            forecast_run_id,
            forecast_date,
            predicted_quantity,
            lower_bound_qty,
            upper_bound_qty,
            confidence_level_pct,
            created_at,
            updated_at
        )
        VALUES (%s,%s,%s,%s,%s,%s,%s,%s)
        """,
        rows
    )


def run_predict_job(cfg: dict, horizon: int) -> dict:
    db = Db(cfg)

    reg = json.loads(
        (Path(cfg["paths"]["artifacts_root"]) / "registry" / "model_registry.json").read_text(encoding="utf-8")
    )

    item_xgb = load_xgb(reg["item_xgboost_model_path"])
    cat_xgb = load_xgb(reg["category_xgboost_model_path"])

    base = load_base_data(db)
    profiles = profile_series(base, cfg["training"])
    item_panel = build_dense_item_panel(base, profiles, cfg["training"])
    cat_panel = build_category_panel(base, profiles)
    alloc = compute_sparse_allocation_weights(item_panel)

    events = base["events"].copy()
    events["sales_date"] = pd.to_datetime(events["sales_date"])

    future_dates = pd.date_range(
        pd.to_datetime(item_panel["sales_date"]).max() + pd.Timedelta(days=1),
        periods=horizon,
        freq="D"
    )

    results = []

    for sel in reg["selections"]:
        vendor_id = int(sel["vendor_id"])
        menu_item_id = int(sel["menu_item_id"])
        category_id = int(sel["category_id"])
        model_name = sel["selected_model"]

        if model_name == "HIERARCHICAL_CATEGORY_XGBOOST":
            hist = cat_panel[
                (cat_panel["vendor_id"] == vendor_id) &
                (cat_panel["category_id"] == category_id)
            ].copy().sort_values("sales_date")

            if hist.empty:
                continue

            fut = make_future_frame(hist, future_dates, events)
            pred_cat = predict_global_xgb(cat_xgb, fut)

            share_row = alloc[
                (alloc["vendor_id"] == vendor_id) &
                (alloc["category_id"] == category_id) &
                (alloc["menu_item_id"] == menu_item_id)
            ]
            share = float(share_row["item_share"].iloc[0]) if not share_row.empty else 0.0
            preds = [max(0.0, float(x * share)) for x in pred_cat]
        else:
            hist = item_panel[
                (item_panel["vendor_id"] == vendor_id) &
                (item_panel["menu_item_id"] == menu_item_id)
            ].copy().sort_values("sales_date")

            if hist.empty:
                continue

            if model_name == "DIRECT_SEASONAL_NAIVE_7":
                preds = seasonal_naive_7(hist["quantity_sold"], horizon)
            elif model_name == "DIRECT_ROLLING_MEAN_7":
                preds = rolling_mean_forecast(hist["quantity_sold"], horizon, 7)
            elif model_name == "DIRECT_CROSTON":
                preds = croston(hist["quantity_sold"], horizon)
            elif model_name == "DIRECT_SBA":
                preds = sba(hist["quantity_sold"], horizon)
            elif model_name == "DIRECT_TSB":
                preds = tsb(hist["quantity_sold"], horizon)
            else:
                fut = make_future_frame(hist, future_dates, events)
                preds = predict_global_xgb(item_xgb, fut).tolist()

        run_id = insert_run(db, vendor_id, menu_item_id, model_name, horizon)
        insert_values(db, run_id, future_dates, preds)

        results.append(
            {
                "vendor_id": vendor_id,
                "menu_item_id": menu_item_id,
                "model_name": model_name,
                "forecast_run_id": run_id,
                "points": horizon
            }
        )

    return {
        "inserted_forecast_runs": len(results),
        "inserted_forecast_values": len(results) * horizon,
        "series_results": results
    }


def main(config_path: str, horizon: int) -> None:
    cfg = load_config(config_path)
    result = run_predict_job(cfg, horizon)
    print(json.dumps(result, indent=2))


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--config", required=True)
    parser.add_argument("--horizon", type=int, default=7)
    args = parser.parse_args()
    main(args.config, args.horizon)
