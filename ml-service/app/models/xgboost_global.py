import joblib
import numpy as np
from pathlib import Path
from xgboost import XGBRegressor
from ..features import FEATURE_COLUMNS

def train_global_xgb(df):
    features = [c for c in FEATURE_COLUMNS if c in df.columns]
    X = df[features].fillna(0.0)
    y = df["quantity_sold"].astype(float).clip(lower=0.0)
    model = XGBRegressor(
        objective="count:poisson",
        n_estimators=400,
        learning_rate=0.05,
        max_depth=6,
        min_child_weight=3,
        subsample=0.9,
        colsample_bytree=0.9,
        reg_alpha=0.1,
        reg_lambda=1.5,
        random_state=42,
        tree_method="hist",
    )
    model.fit(X, y)
    return {"model": model, "feature_columns": features}

def predict_global_xgb(artifacts, frame):
    X = frame[artifacts["feature_columns"]].fillna(0.0)
    pred = artifacts["model"].predict(X)
    return np.clip(np.array(pred, dtype=float), 0.0, None)

def save_xgb(artifacts, path):
    Path(path).parent.mkdir(parents=True, exist_ok=True)
    joblib.dump(artifacts, path)

def load_xgb(path):
    return joblib.load(path)
