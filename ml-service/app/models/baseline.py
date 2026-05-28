def rolling_mean_forecast(history, horizon, window=7):
    vals = history.tail(window).astype(float)
    m = float(vals.mean()) if len(vals) else 0.0
    return [max(0.0, m)] * horizon

def seasonal_naive_7(history, horizon):
    hist = history.astype(float).tolist()
    base = hist[-7:] if len(hist) >= 7 else (hist if hist else [0.0])
    return [max(0.0, float(base[i % len(base)])) for i in range(horizon)]
