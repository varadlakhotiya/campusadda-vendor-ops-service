import pandas as pd

def run_baseline(df: pd.DataFrame, horizon_days: int):
    mean_value = float(df["quantitySold"].tail(7).mean()) if not df.empty else 0.0
    last_date = df["date"].max()

    forecasts = []
    for i in range(1, horizon_days + 1):
        forecast_date = last_date + pd.Timedelta(days=i)
        forecasts.append({
            "forecastDate": forecast_date.date().isoformat(),
            "predictedQuantity": mean_value,
            "lowerBoundQty": max(0.0, mean_value * 0.8),
            "upperBoundQty": mean_value * 1.2,
            "confidenceLevelPct": 80.0
        })

    return forecasts, {"mae": None, "rmse": None, "model": "BASELINE"}