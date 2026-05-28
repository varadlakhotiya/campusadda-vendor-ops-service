from __future__ import annotations

import argparse
import json
from pathlib import Path
from typing import Any, Dict, List

import pandas as pd

from app.db import Db


def _load_daily_history(db: Db) -> pd.DataFrame:
    return db.read_sql(
        """
        SELECT vendor_id,
               menu_item_id,
               sales_date,
               quantity_sold
        FROM daily_item_sales
        ORDER BY vendor_id, menu_item_id, sales_date
        """
    )


def _build_anomalies_for_group(
    vendor_id: int,
    menu_item_id: int,
    group: pd.DataFrame,
    min_history_days: int,
    z_warn: float,
    z_critical: float,
    ratio_warn_up: float,
    ratio_warn_down: float,
) -> List[dict]:

    rows, borderline = [], []

    g = group.sort_values("sales_date").copy()

    g["quantity_sold"] = pd.to_numeric(
        g["quantity_sold"], errors="coerce"
    ).fillna(0.0)

    if len(g) < min_history_days:
        return rows

    g["rolling_mean_7"] = g["quantity_sold"].rolling(
        7, min_periods=7
    ).mean()

    g["rolling_std_7"] = g["quantity_sold"].rolling(
        7, min_periods=7
    ).std()

    g["baseline_mean_21"] = g["quantity_sold"].rolling(
        21, min_periods=14
    ).mean()

    for _, row in g.tail(14).iterrows():

        actual = float(row["quantity_sold"])

        mean = (
            float(row["rolling_mean_7"])
            if pd.notna(row["rolling_mean_7"])
            else None
        )

        std = (
            float(row["rolling_std_7"])
            if pd.notna(row["rolling_std_7"])
            else None
        )

        baseline = (
            float(row["baseline_mean_21"])
            if pd.notna(row["baseline_mean_21"])
            else mean
        )

        if mean is None or baseline is None:
            continue

        ratio = (
            actual / mean
            if mean > 0
            else (999.0 if actual > 0 else 1.0)
        )

        z = (
            (actual - mean) / std
            if std is not None and std > 0
            else 0.0
        )

        strict_hit = (
            abs(z) >= z_warn
            or ratio >= ratio_warn_up
            or (mean > 0 and ratio <= ratio_warn_down)
        )

        anomaly_type = (
            "SPIKE"
            if actual > mean
            else ("DROP" if actual < mean else None)
        )

        if not anomaly_type:
            continue

        severity = (
            "CRITICAL"
            if (
                abs(z) >= z_critical
                or ratio >= (ratio_warn_up + 0.8)
                or (
                    mean > 0
                    and ratio <= max(ratio_warn_down - 0.15, 0.05)
                )
            )
            else "WARNING"
        )

        payload = {
            "vendor_id": int(vendor_id),
            "menu_item_id": int(menu_item_id),
            "inventory_item_id": None,
            "anomaly_date": row["sales_date"].date().isoformat(),
            "anomaly_type": anomaly_type,
            "observed_value": round(actual, 3),
            "expected_value": round(baseline, 3),
            "deviation_score": round(float(z), 4),
            "severity": severity,
            "status": "OPEN",
            "details_json": json.dumps(
                {
                    "rolling_mean_7": round(mean, 3),
                    "rolling_std_7": round(std or 0.0, 3),
                    "baseline_mean_21": round(baseline, 3),
                    "ratio_vs_recent_mean": round(ratio, 3),
                }
            ),
        }

        if strict_hit:
            rows.append(payload)

        elif abs(z) >= max(z_warn - 0.35, 1.0):
            borderline.append(payload)

    if not rows and borderline:
        rows.extend(borderline[:2])

    deduped = {}

    for row in rows:

        key = (
            row["vendor_id"],
            row["menu_item_id"],
            row["anomaly_date"],
            row["anomaly_type"],
        )

        existing = deduped.get(key)

        if (
            existing is None
            or (
                existing["severity"] != "CRITICAL"
                and row["severity"] == "CRITICAL"
            )
        ):
            deduped[key] = row

    return list(deduped.values())


def run_anomaly_job(config: Dict[str, Any]) -> Dict[str, Any]:

    db = Db(config)

    history = _load_daily_history(db)

    if history.empty:
        return {
            "inserted_anomalies": 0,
            "message": "No daily sales history found."
        }

    history["sales_date"] = pd.to_datetime(history["sales_date"])

    anomaly_cfg = config.get("anomaly", {})

    min_history_days = int(
        anomaly_cfg.get("min_history_days", 14)
    )

    z_warn = float(
        anomaly_cfg.get("z_warn_threshold", 1.35)
    )

    z_critical = float(
        anomaly_cfg.get("z_critical_threshold", 2.10)
    )

    ratio_warn_up = float(
        anomaly_cfg.get("ratio_warn_up", 2.0)
    )

    ratio_warn_down = float(
        anomaly_cfg.get("ratio_warn_down", 0.45)
    )

    rows = []

    for (
        vendor_id,
        menu_item_id
    ), group in history.groupby(
        ["vendor_id", "menu_item_id"]
    ):

        rows.extend(
            _build_anomalies_for_group(
                int(vendor_id),
                int(menu_item_id),
                group,
                min_history_days,
                z_warn,
                z_critical,
                ratio_warn_up,
                ratio_warn_down,
            )
        )

    if not rows:
        return {
            "inserted_anomalies": 0,
            "message": "No anomalies crossed thresholds."
        }

    db.execute(
        """
        DELETE FROM anomaly_records
        WHERE status = 'OPEN'
        AND anomaly_date >= DATE_SUB(CURDATE(), INTERVAL 45 DAY)
        """
    )

    sql = """
        INSERT INTO anomaly_records
        (vendor_id, menu_item_id, inventory_item_id,
         anomaly_date, anomaly_type, observed_value,
         expected_value, deviation_score,
         severity, status, details_json,
         created_at, updated_at)
        VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,NOW(3),NOW(3))
    """

    params = [
        (
            row["vendor_id"],
            row["menu_item_id"],
            row["inventory_item_id"],
            row["anomaly_date"],
            row["anomaly_type"],
            row["observed_value"],
            row["expected_value"],
            row["deviation_score"],
            row["severity"],
            row["status"],
            row["details_json"],
        )
        for row in rows
    ]

    inserted = db.executemany(sql, params)

    return {
        "inserted_anomalies": inserted
    }


if __name__ == "__main__":

    parser = argparse.ArgumentParser(
        description="Generate anomalies from daily demand history"
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

    result = run_anomaly_job(config)

    print(json.dumps(result, indent=2))