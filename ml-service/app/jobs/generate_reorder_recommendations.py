from __future__ import annotations

import argparse
import json
import math
from pathlib import Path
from typing import Any, Dict

import pandas as pd

from app.db import Db


def _load_latest_forecasts(db: Db) -> pd.DataFrame:
    sql = """
    WITH latest_runs AS (
        SELECT vendor_id, menu_item_id, MAX(id) AS latest_run_id
        FROM forecast_runs
        WHERE status = 'SUCCESS'
        GROUP BY vendor_id, menu_item_id
    )
    SELECT fr.id AS forecast_run_id,
           fr.vendor_id,
           fr.menu_item_id,
           fr.model_name,
           fv.forecast_date,
           fv.predicted_quantity,
           fv.upper_bound_qty
    FROM latest_runs lr
    JOIN forecast_runs fr ON fr.id = lr.latest_run_id
    JOIN forecast_values fv ON fv.forecast_run_id = fr.id
    ORDER BY fr.vendor_id, fr.menu_item_id, fv.forecast_date
    """
    return db.read_sql(sql)


def run_reorder_job(config: Dict[str, Any]) -> Dict[str, Any]:

    db = Db(config)

    forecasts = _load_latest_forecasts(db)

    if forecasts.empty:
        return {"inserted_recommendations": 0, "message": "No forecasts found."}

    recipes = db.read_sql("""
        SELECT menu_item_id,
               inventory_item_id,
               quantity_required,
               wastage_pct,
               is_active
        FROM menu_item_ingredients
        WHERE is_active = 1
    """)

    inventory = db.read_sql("""
        SELECT id AS inventory_item_id,
               vendor_id,
               item_name,
               current_quantity,
               reserved_quantity
        FROM inventory_items
    """)

    policies = db.read_sql("""
        SELECT inventory_item_id,
               lead_time_days,
               review_period_days,
               service_level_pct,
               safety_stock_qty,
               reorder_point_qty,
               min_reorder_qty,
               max_reorder_qty,
               auto_recommend_enabled
        FROM inventory_policies
    """)

    recipes["quantity_required"] = pd.to_numeric(
        recipes["quantity_required"], errors="coerce"
    ).fillna(0)

    recipes["wastage_pct"] = pd.to_numeric(
        recipes["wastage_pct"], errors="coerce"
    ).fillna(0)

    forecasts["predicted_quantity"] = pd.to_numeric(
        forecasts["predicted_quantity"], errors="coerce"
    ).fillna(0)

    forecasts["upper_bound_qty"] = pd.to_numeric(
        forecasts["upper_bound_qty"], errors="coerce"
    ).fillna(forecasts["predicted_quantity"])

    inventory["current_quantity"] = pd.to_numeric(
        inventory["current_quantity"], errors="coerce"
    ).fillna(0)

    inventory["reserved_quantity"] = pd.to_numeric(
        inventory["reserved_quantity"], errors="coerce"
    ).fillna(0)

    for col in [
        "lead_time_days",
        "review_period_days",
        "service_level_pct",
        "safety_stock_qty",
        "reorder_point_qty",
        "min_reorder_qty",
        "max_reorder_qty",
    ]:
        policies[col] = pd.to_numeric(
            policies[col], errors="coerce"
        ).fillna(0)

    upper_multiplier = float(
        config.get("reorder", {}).get("use_upper_bound_multiplier", 1.0)
    )

    trigger_buffer_pct = float(
        config.get("reorder", {}).get("presentation_trigger_buffer_pct", 0.18)
    )

    minimum_demo_ratio = float(
        config.get("reorder", {}).get("minimum_demo_ratio", 0.12)
    )

    demand = forecasts.merge(recipes, on="menu_item_id", how="inner")

    demand["effective_forecast_qty"] = (
        demand[["predicted_quantity", "upper_bound_qty"]]
        .max(axis=1)
        * max(upper_multiplier, 1.0)
    )

    demand["ingredient_demand_qty"] = (
        demand["effective_forecast_qty"]
        * demand["quantity_required"]
        * (1 + demand["wastage_pct"] / 100.0)
    )

    agg = demand.groupby(
        ["vendor_id", "inventory_item_id"],
        as_index=False
    ).agg(
        forecast_demand_qty=("ingredient_demand_qty", "sum"),
        forecast_run_id=("forecast_run_id", "max"),
    )

    rec_df = (
        agg.merge(
            inventory,
            on=["vendor_id", "inventory_item_id"],
            how="left"
        )
        .merge(
            policies,
            on="inventory_item_id",
            how="left"
        )
    )

    rec_df = rec_df[
        rec_df["auto_recommend_enabled"].fillna(1) != 0
    ].copy()

    if rec_df.empty:
        return {
            "inserted_recommendations": 0,
            "message": "No eligible inventory policies found."
        }

    rec_df["available_qty"] = (
        rec_df["current_quantity"]
        - rec_df["reserved_quantity"]
    )

    rec_df["base_threshold_qty"] = rec_df[
        ["forecast_demand_qty", "reorder_point_qty"]
    ].max(axis=1) + rec_df["safety_stock_qty"]

    rec_df["presentation_threshold_qty"] = (
        rec_df["base_threshold_qty"]
        * (1.0 + trigger_buffer_pct)
    )

    def _recommend(row):

        available = float(row["available_qty"])
        reorder_point = float(row["reorder_point_qty"])
        presentation_threshold = float(row["presentation_threshold_qty"])
        min_reorder = float(row["min_reorder_qty"])
        max_reorder = float(row["max_reorder_qty"])
        forecast_demand = float(row["forecast_demand_qty"])

        if available > presentation_threshold:
            return 0.0, "SAFE"

        shortfall = max(
            0.0,
            presentation_threshold - available
        )

        if shortfall <= 0.0:

            shortfall = max(
                min_reorder * 0.5,
                forecast_demand * minimum_demo_ratio,
                max(reorder_point - available, 0.0),
            )

            status = "WATCH"

        else:
            status = "OPEN"

        suggested = shortfall

        if min_reorder > 0:
            suggested = max(suggested, min_reorder)

        if max_reorder > 0:
            suggested = min(suggested, max_reorder)

        return math.ceil(max(suggested, 0.0)), status


    computed = rec_df.apply(
        lambda row: _recommend(row),
        axis=1,
        result_type="expand"
    )

    rec_df["suggested_reorder_qty"] = computed[0]
    rec_df["recommendation_status"] = computed[1]

    rec_df = rec_df[
        rec_df["suggested_reorder_qty"] > 0
    ].copy()

    if rec_df.empty:
        return {
            "inserted_recommendations": 0,
            "message": "No reorder candidates after policy filtering."
        }

    db.execute(
        "DELETE FROM reorder_recommendations WHERE recommendation_date = CURDATE()"
    )

    insert_sql = """
        INSERT INTO reorder_recommendations
        (vendor_id, inventory_item_id, forecast_run_id,
         recommendation_date, current_stock_qty,
         lead_time_days, forecast_demand_qty,
         safety_stock_qty, reorder_point_qty,
         suggested_reorder_qty, recommendation_status,
         explanation, created_at, updated_at)
        VALUES (%s,%s,%s,CURDATE(),%s,%s,%s,%s,%s,%s,%s,%s,NOW(3),NOW(3))
    """

    params = []

    for _, row in rec_df.iterrows():

        explanation = (
            f"Forecast-driven reorder: "
            f"forecast_demand={row['forecast_demand_qty']:.3f}, "
            f"available={row['available_qty']:.3f}, "
            f"base_threshold={row['base_threshold_qty']:.3f}, "
            f"presentation_threshold={row['presentation_threshold_qty']:.3f}"
        )

        params.append((
            int(row["vendor_id"]),
            int(row["inventory_item_id"]),
            int(row["forecast_run_id"])
            if pd.notna(row["forecast_run_id"]) else None,
            round(float(row["available_qty"]), 3),
            int(row["lead_time_days"])
            if pd.notna(row["lead_time_days"]) else 1,
            round(float(row["forecast_demand_qty"]), 3),
            round(float(row["safety_stock_qty"]), 3),
            round(float(row["reorder_point_qty"]), 3),
            round(float(row["suggested_reorder_qty"]), 3),
            str(row["recommendation_status"]),
            explanation
        ))

    inserted = db.executemany(insert_sql, params)

    return {
        "inserted_recommendations": inserted
    }


if __name__ == "__main__":

    parser = argparse.ArgumentParser(
        description="Generate reorder recommendations from latest forecasts"
    )

    parser.add_argument(
        "--config",
        required=True,
        help="Path to ml_training_config.json"
    )

    args = parser.parse_args()

    config = json.loads(
        Path(args.config).read_text(encoding="utf-8")
    )

    result = run_reorder_job(config)

    print(json.dumps(result, indent=2))