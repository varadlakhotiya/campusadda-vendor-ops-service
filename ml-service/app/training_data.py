from dataclasses import dataclass
import pandas as pd

from .features import (
    add_calendar_features,
    add_event_proximity_features,
    add_series_lags,
    encode_entity_features,
)


def _to_numeric(df: pd.DataFrame, cols):
    out = df.copy()
    for col in cols:
        if col in out.columns:
            out[col] = pd.to_numeric(out[col], errors="coerce")
    return out


def _to_datetime(df: pd.DataFrame, cols):
    out = df.copy()
    for col in cols:
        if col in out.columns:
            out[col] = pd.to_datetime(out[col], errors="coerce")
    return out


def load_base_data(db):
    base = {
        "daily_item": db.read_sql("""
            SELECT
                sales_date,
                vendor_id,
                menu_item_id,
                quantity_sold,
                gross_revenue
            FROM daily_item_sales
        """),
        "daily_vendor": db.read_sql("""
            SELECT
                sales_date,
                vendor_id,
                total_orders,
                gross_revenue
            FROM daily_vendor_sales
        """),
        "hourly_vendor": db.read_sql("""
            SELECT
                sales_date,
                vendor_id,
                sales_hour,
                total_orders
            FROM hourly_vendor_sales
        """),
        "menu_items": db.read_sql("""
            SELECT
                id AS menu_item_id,
                vendor_id,
                category_id,
                price,
                prep_time_minutes,
                IFNULL(is_veg, 1) AS is_veg,
                IFNULL(track_inventory, 1) AS track_inventory,
                item_name
            FROM menu_items
            WHERE is_active = 1
        """),
        "categories": db.read_sql("""
            SELECT
                id AS category_id,
                vendor_id,
                category_name
            FROM menu_categories
            WHERE is_active = 1
        """),
        "events": db.read_sql("""
            SELECT
                event_date AS sales_date,
                vendor_id,
                IFNULL(impact_level, 0) AS impact_level,
                1 AS event_flag
            FROM calendar_events
            WHERE is_active = 1
        """),
    }

    base["daily_item"] = _to_numeric(
        base["daily_item"],
        ["vendor_id", "menu_item_id", "quantity_sold", "gross_revenue"],
    )
    base["daily_item"] = _to_datetime(base["daily_item"], ["sales_date"])
    base["daily_item"]["quantity_sold"] = base["daily_item"]["quantity_sold"].fillna(0.0)
    base["daily_item"]["gross_revenue"] = base["daily_item"]["gross_revenue"].fillna(0.0)

    base["daily_vendor"] = _to_numeric(
        base["daily_vendor"],
        ["vendor_id", "total_orders", "gross_revenue"],
    )
    base["daily_vendor"] = _to_datetime(base["daily_vendor"], ["sales_date"])
    base["daily_vendor"]["total_orders"] = base["daily_vendor"]["total_orders"].fillna(0.0)
    base["daily_vendor"]["gross_revenue"] = base["daily_vendor"]["gross_revenue"].fillna(0.0)

    base["hourly_vendor"] = _to_numeric(
        base["hourly_vendor"],
        ["vendor_id", "sales_hour", "total_orders"],
    )
    base["hourly_vendor"] = _to_datetime(base["hourly_vendor"], ["sales_date"])
    base["hourly_vendor"]["sales_hour"] = base["hourly_vendor"]["sales_hour"].fillna(0).astype(int)
    base["hourly_vendor"]["total_orders"] = base["hourly_vendor"]["total_orders"].fillna(0.0)

    base["menu_items"] = _to_numeric(
        base["menu_items"],
        [
            "menu_item_id",
            "vendor_id",
            "category_id",
            "price",
            "prep_time_minutes",
            "is_veg",
            "track_inventory",
        ],
    )
    base["menu_items"]["category_id"] = base["menu_items"]["category_id"].fillna(-1)
    base["menu_items"]["price"] = base["menu_items"]["price"].fillna(0.0)
    base["menu_items"]["prep_time_minutes"] = base["menu_items"]["prep_time_minutes"].fillna(0.0)
    base["menu_items"]["is_veg"] = base["menu_items"]["is_veg"].fillna(1).astype(int)
    base["menu_items"]["track_inventory"] = base["menu_items"]["track_inventory"].fillna(1).astype(int)

    base["categories"] = _to_numeric(
        base["categories"],
        ["category_id", "vendor_id"],
    )
    base["events"] = _to_numeric(
        base["events"],
        ["vendor_id", "impact_level", "event_flag"],
    )
    base["events"] = _to_datetime(base["events"], ["sales_date"])
    base["events"]["impact_level"] = base["events"]["impact_level"].fillna(0.0)
    base["events"]["event_flag"] = base["events"]["event_flag"].fillna(0).astype(int)

    return base


def profile_series(base, cfg):
    df = base["daily_item"].merge(
        base["menu_items"][["menu_item_id", "vendor_id", "category_id"]],
        on=["vendor_id", "menu_item_id"],
        how="left",
    )

    df["quantity_sold"] = pd.to_numeric(df["quantity_sold"], errors="coerce").fillna(0.0)
    df["category_id"] = pd.to_numeric(df["category_id"], errors="coerce").fillna(-1).astype(int)

    agg = df.groupby(["vendor_id", "menu_item_id", "category_id"], as_index=False).agg(
        nonzero_days=("quantity_sold", lambda s: int((s > 0).sum())),
        total_qty=("quantity_sold", "sum"),
        total_days=("sales_date", "nunique"),
    )

    agg["nonzero_ratio"] = agg["nonzero_days"] / agg["total_days"].clip(lower=1)

    def classify(r):
        if (
            r["nonzero_days"] >= cfg["dense_min_nonzero_days"]
            and r["total_qty"] >= cfg["dense_min_total_qty"]
        ):
            return "dense"
        if (
            r["nonzero_days"] >= cfg["medium_min_nonzero_days"]
            and r["total_qty"] >= cfg["medium_min_total_qty"]
        ):
            return "medium"
        return "sparse"

    agg["series_type"] = agg.apply(classify, axis=1)
    return agg


def build_dense_item_panel(base, profiles, cfg):
    item_df = base["daily_item"].copy()

    sel = (
        profiles.sort_values(["vendor_id", "total_qty"], ascending=[True, False])
        .groupby("vendor_id", group_keys=False)
        .head(int(cfg["top_items_per_vendor"]))
        .reset_index(drop=True)
    )

    item_df = item_df.merge(
        sel[["vendor_id", "menu_item_id", "series_type"]],
        on=["vendor_id", "menu_item_id"],
        how="inner",
    )

    item_df["sales_date"] = pd.to_datetime(item_df["sales_date"])
    all_dates = pd.date_range(item_df["sales_date"].min(), item_df["sales_date"].max(), freq="D")

    keys = item_df[["vendor_id", "menu_item_id", "series_type"]].drop_duplicates()
    panel = (
        keys.assign(key=1)
        .merge(pd.DataFrame({"sales_date": all_dates, "key": 1}), on="key")
        .drop(columns="key")
    )

    panel = panel.merge(
        item_df,
        on=["vendor_id", "menu_item_id", "sales_date", "series_type"],
        how="left",
    )

    panel["quantity_sold"] = pd.to_numeric(panel["quantity_sold"], errors="coerce").fillna(0.0)
    panel["gross_revenue"] = pd.to_numeric(panel["gross_revenue"], errors="coerce").fillna(0.0)

    panel = panel.merge(base["menu_items"], on=["vendor_id", "menu_item_id"], how="left")
    panel = panel.merge(base["categories"], on=["vendor_id", "category_id"], how="left")

    events = base["events"].copy()
    events["sales_date"] = pd.to_datetime(events["sales_date"])
    panel = panel.merge(events, on=["vendor_id", "sales_date"], how="left")

    dv = base["daily_vendor"].copy()
    dv["sales_date"] = pd.to_datetime(dv["sales_date"])
    dv = dv.sort_values(["vendor_id", "sales_date"])

    dv["vendor_recent_orders_7"] = (
        dv.groupby("vendor_id")["total_orders"].shift(1).rolling(7, min_periods=1).sum()
    )
    dv["vendor_recent_revenue_7"] = (
        dv.groupby("vendor_id")["gross_revenue"].shift(1).rolling(7, min_periods=1).sum()
    )

    panel = panel.merge(
        dv[["vendor_id", "sales_date", "vendor_recent_orders_7", "vendor_recent_revenue_7"]],
        on=["vendor_id", "sales_date"],
        how="left",
    )

    hv = base["hourly_vendor"].copy()
    hv["sales_date"] = pd.to_datetime(hv["sales_date"])

    late_night = (
        hv.assign(is_late=hv["sales_hour"].isin([22, 23, 0, 1]).astype(int))
        .groupby(["vendor_id", "sales_date"], as_index=False)
        .agg(
            late_orders=("total_orders", lambda s: float(s[hv.loc[s.index, "sales_hour"].isin([22, 23, 0, 1])].sum())),
            total_orders=("total_orders", "sum"),
        )
    )

    late_night["vendor_late_night_share"] = late_night["late_orders"] / late_night["total_orders"].replace(0, 1.0)
    late_night = late_night.sort_values(["vendor_id", "sales_date"])
    late_night["vendor_late_night_share_14"] = (
        late_night.groupby("vendor_id")["vendor_late_night_share"].shift(1).rolling(14, min_periods=1).mean()
    )

    panel = panel.merge(
        late_night[["vendor_id", "sales_date", "vendor_late_night_share_14"]],
        on=["vendor_id", "sales_date"],
        how="left",
    )

    panel["series_key"] = panel["menu_item_id"].astype(str)

    panel = add_calendar_features(panel)
    panel = add_event_proximity_features(panel)
    panel = add_series_lags(panel)
    panel = encode_entity_features(panel)

    panel["category_id"] = pd.to_numeric(panel["category_id"], errors="coerce").fillna(-1).astype(int)
    return panel


def build_category_panel(base, profiles):
    item_panel = build_dense_item_panel(base, profiles, {"top_items_per_vendor": 10000})

    cat = item_panel.groupby(["vendor_id", "category_id", "sales_date"], as_index=False).agg(
        quantity_sold=("quantity_sold", "sum"),
        price=("price", "mean"),
        prep_time_minutes=("prep_time_minutes", "mean"),
        is_veg=("is_veg", "max"),
        track_inventory=("track_inventory", "max"),
        event_flag=("event_flag", "max"),
        impact_level=("impact_level", "max"),
        vendor_recent_orders_7=("vendor_recent_orders_7", "max"),
        vendor_recent_revenue_7=("vendor_recent_revenue_7", "max"),
        vendor_late_night_share_14=("vendor_late_night_share_14", "max"),
    )

    cat["menu_item_id"] = -1
    cat["series_key"] = cat["category_id"].astype(str)

    cat = add_calendar_features(cat)
    cat = add_event_proximity_features(cat)
    cat = add_series_lags(cat, group_cols=("vendor_id", "series_key"))
    cat = encode_entity_features(cat)
    return cat


def compute_sparse_allocation_weights(panel):
    x = panel.copy()
    x["sales_date"] = pd.to_datetime(x["sales_date"])
    recent = x[x["sales_date"] >= x["sales_date"].max() - pd.Timedelta(days=28)]

    share = recent.groupby(["vendor_id", "category_id", "menu_item_id"], as_index=False).agg(
        item_qty=("quantity_sold", "sum")
    )
    cat = share.groupby(["vendor_id", "category_id"], as_index=False).agg(cat_qty=("item_qty", "sum"))
    share = share.merge(cat, on=["vendor_id", "category_id"], how="left")
    share["item_share"] = share["item_qty"] / share["cat_qty"].replace(0, 1.0)

    return share[["vendor_id", "category_id", "menu_item_id", "item_share"]]