from __future__ import annotations

import argparse
import json
from pathlib import Path

from ..backtesting import composite_metrics, generate_time_splits
from ..db import Db, load_config
from ..models.baseline import rolling_mean_forecast, seasonal_naive_7
from ..models.intermittent import croston, sba, tsb
from ..models.xgboost_global import predict_global_xgb, save_xgb, train_global_xgb
from ..training_data import (
    build_category_panel,
    build_dense_item_panel,
    compute_sparse_allocation_weights,
    load_base_data,
    profile_series
)


def evaluate_direct_series(g, item_xgb, horizon, folds):
    g = g.sort_values("sales_date").reset_index(drop=True)
    splits = generate_time_splits(len(g), horizon, folds)

    if not splits:
        return {"selected_model": "DIRECT_SEASONAL_NAIVE_7", "smape": 999.0, "wmape": 999.0}

    candidates = {}

    for name in [
        "DIRECT_SEASONAL_NAIVE_7",
        "DIRECT_ROLLING_MEAN_7",
        "DIRECT_CROSTON",
        "DIRECT_SBA",
        "DIRECT_TSB",
        "DIRECT_XGBOOST_GLOBAL"
    ]:
        y_true_all, y_pred_all = [], []

        for test_start, test_end in splits:
            train = g.iloc[:test_start].copy()
            test = g.iloc[test_start:test_end].copy()
            hist = train["quantity_sold"].astype(float)

            if name == "DIRECT_SEASONAL_NAIVE_7":
                pred = seasonal_naive_7(hist, len(test))
            elif name == "DIRECT_ROLLING_MEAN_7":
                pred = rolling_mean_forecast(hist, len(test), 7)
            elif name == "DIRECT_CROSTON":
                pred = croston(hist, len(test))
            elif name == "DIRECT_SBA":
                pred = sba(hist, len(test))
            elif name == "DIRECT_TSB":
                pred = tsb(hist, len(test))
            else:
                pred = predict_global_xgb(item_xgb, test).tolist()

            y_true_all.extend(test["quantity_sold"].astype(float).tolist())
            y_pred_all.extend(pred)

        candidates[name] = composite_metrics(y_true_all, y_pred_all)

    best = sorted(
        candidates.items(),
        key=lambda kv: (kv[1]["business_score"], kv[1]["wmape"], kv[1]["smape"])
    )[0]

    return {"selected_model": best[0], "smape": best[1]["smape"], "wmape": best[1]["wmape"]}


def run_training_job(cfg: dict) -> dict:
    db = Db(cfg)
    train_cfg = cfg["training"]

    base = load_base_data(db)
    profiles = profile_series(base, train_cfg)
    item_panel = build_dense_item_panel(base, profiles, train_cfg)
    category_panel = build_category_panel(base, profiles)

    item_xgb = train_global_xgb(
    item_panel[
        item_panel["series_type"] == "dense"
    ].copy()
)
    category_xgb = train_global_xgb(category_panel.copy())

    root = Path(cfg["paths"]["artifacts_root"])
    item_path = root / "models" / "xgboost" / "global_item_xgb.joblib"
    cat_path = root / "models" / "xgboost" / "global_category_xgb.joblib"
    reg_path = root / "registry" / "model_registry.json"

    save_xgb(item_xgb, item_path)
    save_xgb(category_xgb, cat_path)

    allocation = compute_sparse_allocation_weights(item_panel)

    top_vendors = (
        profiles.groupby("vendor_id", as_index=False)["total_qty"]
        .sum()
        .sort_values("total_qty", ascending=False)
        .head(int(train_cfg["top_vendors_limit"]))["vendor_id"]
        .tolist()
    )

    selected = (
        profiles[profiles["vendor_id"].isin(top_vendors)]
        .sort_values(["vendor_id", "total_qty"], ascending=[True, False])
        .groupby("vendor_id", group_keys=False)
        .head(int(train_cfg["top_items_per_vendor"]))
        .reset_index(drop=True)
    )

    selections = []

    for _, row in selected.iterrows():
        vendor_id = int(row["vendor_id"])
        menu_item_id = int(row["menu_item_id"])
        category_id = int(row["category_id"]) if str(row["category_id"]) != "nan" else -1
        series_type = row["series_type"]

        g = item_panel[
            (item_panel["vendor_id"] == vendor_id) &
            (item_panel["menu_item_id"] == menu_item_id)
        ].copy()

        if g.empty:
            continue

        if series_type == "sparse":
            cat_g = category_panel[
                (category_panel["vendor_id"] == vendor_id) &
                (category_panel["category_id"] == category_id)
            ].copy()

            share_row = allocation[
                (allocation["vendor_id"] == vendor_id) &
                (allocation["category_id"] == category_id) &
                (allocation["menu_item_id"] == menu_item_id)
            ]
            share = float(share_row["item_share"].iloc[0]) if not share_row.empty else 0.0

            y_true = g["quantity_sold"].tail(train_cfg["validation_horizon_days"]).tolist()
            cat_hist = (
                cat_g["quantity_sold"].tail(train_cfg["validation_horizon_days"]).tolist()
                if not cat_g.empty
                else [0.0] * int(train_cfg["validation_horizon_days"])
            )
            y_pred = [max(0.0, c * share) for c in cat_hist]
            m = composite_metrics(y_true, y_pred)

            result = {
                "selected_model": "HIERARCHICAL_CATEGORY_XGBOOST",
                "smape": m["smape"],
                "wmape": m["wmape"]
            }
        else:
            result = evaluate_direct_series(
                g,
                item_xgb,
                int(train_cfg["validation_horizon_days"]),
                int(train_cfg["backtest_folds"])
            )

            if series_type == "medium":
                cat_g = category_panel[
                    (category_panel["vendor_id"] == vendor_id) &
                    (category_panel["category_id"] == category_id)
                ].copy()

                share_row = allocation[
                    (allocation["vendor_id"] == vendor_id) &
                    (allocation["category_id"] == category_id) &
                    (allocation["menu_item_id"] == menu_item_id)
                ]

                if not cat_g.empty and not share_row.empty:
                    share = float(share_row["item_share"].iloc[0])
                    y_true = g["quantity_sold"].tail(train_cfg["validation_horizon_days"]).tolist()
                    cat_hist = cat_g["quantity_sold"].tail(train_cfg["validation_horizon_days"]).tolist()
                    hm = composite_metrics(y_true, [max(0.0, c * share) for c in cat_hist])

                    if result["wmape"] > 140 and hm["wmape"] <= result["wmape"] * 1.05:
                        result = {
                            "selected_model": "HIERARCHICAL_CATEGORY_XGBOOST",
                            "smape": hm["smape"],
                            "wmape": hm["wmape"]
                        }

        selections.append(
            {
                "vendor_id": vendor_id,
                "menu_item_id": menu_item_id,
                "category_id": category_id,
                "series_type": series_type,
                "selected_model": result["selected_model"],
                "smape": round(float(result["smape"]), 4),
                "wmape": round(float(result["wmape"]), 4)
            }
        )

    reg_path.parent.mkdir(parents=True, exist_ok=True)

    reg = {
        "item_xgboost_model_path": str(item_path),
        "category_xgboost_model_path": str(cat_path),
        "allocation_policy": "recent_28d_share",
        "selections": selections
    }

    reg_path.write_text(json.dumps(reg, indent=2), encoding="utf-8")

    return {
        "selected_series_count": len(selections),
        "selected_category_count": int(category_panel[["vendor_id", "category_id"]].drop_duplicates().shape[0]),
        "registry_path": str(reg_path),
        "item_xgboost_model_path": str(item_path),
        "category_xgboost_model_path": str(cat_path),
        "selections": selections
    }


def main(config_path: str) -> None:
    cfg = load_config(config_path)
    result = run_training_job(cfg)
    print(json.dumps(result, indent=2))


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--config", required=True)
    args = parser.parse_args()
    main(args.config)
