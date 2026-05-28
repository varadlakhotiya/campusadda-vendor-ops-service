import numpy as np
import pandas as pd

FEATURE_COLUMNS = [
    "vendor_id","menu_item_id","category_id","price","prep_time_minutes",
    "is_veg","track_inventory","dow","is_weekend","week_of_month","month","day_of_month",
    "event_flag","impact_level","days_since_event","days_until_event",
    "lag_1","lag_2","lag_3","lag_7","lag_14","lag_21","lag_28",
    "roll_mean_3","roll_mean_7","roll_mean_14","roll_mean_28",
    "roll_std_7","roll_std_14","roll_sum_7","roll_sum_14",
    "nonzero_ratio_7","nonzero_ratio_14","nonzero_ratio_28",
    "days_since_last_sale","last_nonzero_qty",
    "vendor_recent_orders_7","vendor_recent_revenue_7","vendor_late_night_share_14"
]

def add_calendar_features(df, date_col="sales_date"):
    x = df.copy()
    d = pd.to_datetime(x[date_col])
    x["dow"] = d.dt.dayofweek
    x["is_weekend"] = x["dow"].isin([5,6]).astype(int)
    x["week_of_month"] = ((d.dt.day - 1) // 7 + 1).astype(int)
    x["month"] = d.dt.month
    x["day_of_month"] = d.dt.day
    return x

def add_event_proximity_features(df):
    x = df.copy()
    x["event_flag"] = x.get("event_flag", 0)
    x["impact_level"] = x.get("impact_level", 0)
    x["event_flag"] = x["event_flag"].fillna(0).astype(int)
    x["impact_level"] = x["impact_level"].fillna(0).astype(float)
    x = x.sort_values(["vendor_id","series_key","sales_date"]).reset_index(drop=True)
    x["days_since_event"] = 999.0
    x["days_until_event"] = 999.0
    for _, idx in x.groupby(["vendor_id","series_key"]).groups.items():
        g = x.loc[idx].copy()
        dates = pd.to_datetime(g["sales_date"])
        event_dates = dates[g["event_flag"] == 1].tolist()
        since_vals, until_vals = [], []
        for dt in dates:
            before = [abs((dt-e).days) for e in event_dates if e <= dt]
            after = [abs((e-dt).days) for e in event_dates if e >= dt]
            since_vals.append(min(before) if before else 999.0)
            until_vals.append(min(after) if after else 999.0)
        x.loc[idx, "days_since_event"] = since_vals
        x.loc[idx, "days_until_event"] = until_vals
    return x

def add_series_lags(df, target_col="quantity_sold", group_cols=("vendor_id","series_key")):
    x = df.copy().sort_values(list(group_cols)+["sales_date"]).reset_index(drop=True)
    for lag in [1,2,3,7,14,21,28]:
        x[f"lag_{lag}"] = x.groupby(list(group_cols))[target_col].shift(lag)
    for win in [3,7,14,28]:
        shifted = x.groupby(list(group_cols))[target_col].shift(1)
        x[f"roll_mean_{win}"] = shifted.rolling(win, min_periods=1).mean()
        x[f"roll_std_{win}"] = shifted.rolling(win, min_periods=1).std()
        x[f"roll_sum_{win}"] = shifted.rolling(win, min_periods=1).sum()
        x[f"nonzero_ratio_{win}"] = shifted.rolling(win, min_periods=1).apply(lambda s: float(np.mean(np.array(s) > 0)), raw=False)
    x["days_since_last_sale"] = 999.0
    for _, idx in x.groupby(list(group_cols)).groups.items():
        last_seen = None
        vals = []
        g = x.loc[idx]
        for _, row in g.iterrows():
            dt = pd.to_datetime(row["sales_date"])
            vals.append(999.0 if last_seen is None else float((dt - last_seen).days))
            if row[target_col] > 0:
                last_seen = dt
        x.loc[idx, "days_since_last_sale"] = vals
    x["last_nonzero_qty"] = x.groupby(list(group_cols))[target_col].transform(lambda s: s.replace(0,np.nan).ffill())
    for c in [c for c in x.columns if c.startswith(("lag_","roll_","nonzero_ratio_","days_since_","last_nonzero_"))]:
        x[c] = x[c].replace([np.inf,-np.inf], np.nan).fillna(0.0)
    return x

def encode_entity_features(df):
    x = df.copy()
    for col in ["vendor_id","menu_item_id","category_id","is_veg","track_inventory"]:
        if col in x.columns:
            x[col] = x[col].fillna(-1).astype(int)
    for col in ["price","prep_time_minutes","vendor_recent_orders_7","vendor_recent_revenue_7","vendor_late_night_share_14"]:
        if col in x.columns:
            x[col] = x[col].fillna(0.0).astype(float)
    return x
