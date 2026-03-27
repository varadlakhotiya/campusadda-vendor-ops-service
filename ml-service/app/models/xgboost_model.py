import pandas as pd
from xgboost import XGBRegressor

def run_xgboost(df: pd.DataFrame, horizon_days: int):
    work = df.copy()
    features = ["day_of_week", "is_weekend", "lag_1", "lag_7", "rolling_3", "impactLevel"]

    train = work.dropna(subset=["quantitySold"])
    X = train[features]
    y = train["quantitySold"]

    model = XGBRegressor(
        n_estimators=100,
        max_depth=4,
        learning_rate=0.1,
        objective="reg:squarederror"
    )
    model.fit(X, y)

    forecasts = []
    last_date = work["date"].max()
    last_qty = float(work["quantitySold"].iloc[-1]) if not work.empty else 0.0

    for i in range(1, horizon_days + 1):
        forecast_date = last_date + pd.Timedelta(days=i)
        row = pd.DataFrame([{
            "day_of_week": forecast_date.dayofweek,
            "is_weekend": 1 if forecast_date.dayofweek in [5, 6] else 0,
            "lag_1": last_qty,
            "lag_7": last_qty,
            "rolling_3": last_qty,
            "impactLevel": 0
        }])

        pred = float(model.predict(row)[0])
        pred = max(0.0, pred)

        forecasts.append({
            "forecastDate": forecast_date.date().isoformat(),
            "predictedQuantity": pred,
            "lowerBoundQty": max(0.0, pred * 0.85),
            "upperBoundQty": pred * 1.15,
            "confidenceLevelPct": 85.0
        })
        last_qty = pred

    return forecasts, {"model": "XGBOOST"}