import pandas as pd
from prophet import Prophet

def run_prophet(df: pd.DataFrame, horizon_days: int):
    prophet_df = df[["date", "quantitySold"]].rename(columns={"date": "ds", "quantitySold": "y"})
    model = Prophet(daily_seasonality=False, weekly_seasonality=True, yearly_seasonality=False)
    model.fit(prophet_df)

    future = model.make_future_dataframe(periods=horizon_days)
    forecast = model.predict(future).tail(horizon_days)

    forecasts = []
    for _, row in forecast.iterrows():
        pred = max(0.0, float(row["yhat"]))
        forecasts.append({
            "forecastDate": row["ds"].date().isoformat(),
            "predictedQuantity": pred,
            "lowerBoundQty": max(0.0, float(row["yhat_lower"])),
            "upperBoundQty": max(0.0, float(row["yhat_upper"])),
            "confidenceLevelPct": 90.0
        })

    return forecasts, {"model": "PROPHET"}