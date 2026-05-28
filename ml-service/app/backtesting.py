import numpy as np

def generate_time_splits(n_rows, horizon, folds):
    splits = []
    if n_rows < horizon * (folds + 2):
        return splits
    train_end = n_rows - horizon * (folds + 1)
    for i in range(folds):
        test_start = train_end + i * horizon
        splits.append((test_start, test_start + horizon))
    return splits

def mae(y_true, y_pred):
    return float(np.mean(np.abs(np.array(y_true) - np.array(y_pred))))

def wmape(y_true, y_pred):
    y_true = np.array(y_true, dtype=float); y_pred = np.array(y_pred, dtype=float)
    denom = np.abs(y_true).sum()
    return 0.0 if denom == 0 else float(np.abs(y_true-y_pred).sum()/denom*100.0)

def smape(y_true, y_pred):
    y_true = np.asarray(y_true, dtype=float)
    y_pred = np.asarray(y_pred, dtype=float)

    denom = np.abs(y_true) + np.abs(y_pred)
    ratio = np.zeros_like(denom, dtype=float)

    mask = denom != 0
    ratio[mask] = 2.0 * np.abs(y_true[mask] - y_pred[mask]) / denom[mask]

    return float(np.mean(ratio) * 100.0) if len(ratio) else 999.0

def business_score(y_true, y_pred):
    y_true = np.array(y_true, dtype=float); y_pred = np.array(y_pred, dtype=float)
    under = np.clip(y_true - y_pred, 0, None).sum()
    over = np.clip(y_pred - y_true, 0, None).sum()
    return float((1.15 * under + over) / max(1.0, np.abs(y_true).sum()) * 100.0)

def composite_metrics(y_true, y_pred):
    return {
        "mae": round(mae(y_true, y_pred), 4),
        "wmape": round(wmape(y_true, y_pred), 4),
        "smape": round(smape(y_true, y_pred), 4),
        "business_score": round(business_score(y_true, y_pred), 4),
    }
