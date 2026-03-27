import pandas as pd

def build_features(sales_series, calendar_events):
    df = pd.DataFrame(sales_series)
    if df.empty:
        return df

    df["date"] = pd.to_datetime(df["date"])
    df["quantitySold"] = pd.to_numeric(df["quantitySold"])
    df = df.sort_values("date")

    df["day_of_week"] = df["date"].dt.dayofweek
    df["is_weekend"] = df["day_of_week"].isin([5, 6]).astype(int)
    df["lag_1"] = df["quantitySold"].shift(1)
    df["lag_7"] = df["quantitySold"].shift(7)
    df["rolling_3"] = df["quantitySold"].rolling(3).mean()

    event_df = pd.DataFrame(calendar_events)
    if not event_df.empty:
        event_df["eventDate"] = pd.to_datetime(event_df["eventDate"])
        event_df["impactLevel"] = pd.to_numeric(event_df["impactLevel"])
        event_df = event_df.rename(columns={"eventDate": "date"})
        df = df.merge(event_df[["date", "impactLevel"]], on="date", how="left")
    else:
        df["impactLevel"] = 0

    df["impactLevel"] = df["impactLevel"].fillna(0)
    return df.fillna(0)