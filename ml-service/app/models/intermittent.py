import numpy as np

def _parts(y):
    arr = np.array(y, dtype=float)
    nz = np.where(arr > 0)[0]
    if len(nz) == 0:
        return np.array([]), np.array([])
    demand = arr[nz]
    intervals = np.diff(np.concatenate(([-1], nz)))
    return demand, intervals

def croston(y, horizon, alpha=0.1):
    demand, intervals = _parts(y)
    if len(demand) == 0:
        return [0.0] * horizon
    z, p = demand[0], intervals[0]
    for d, i in zip(demand[1:], intervals[1:]):
        z = z + alpha * (d - z)
        p = p + alpha * (i - p)
    fc = max(0.0, z / max(p, 1e-6))
    return [float(fc)] * horizon

def sba(y, horizon, alpha=0.1):
    return [max(0.0, croston(y, 1, alpha)[0] * (1 - alpha/2))] * horizon

def tsb(y, horizon, alpha_d=0.1, alpha_p=0.1):
    arr = np.array(y, dtype=float)
    if len(arr) == 0:
        return [0.0] * horizon
    p = 1.0 if arr[0] > 0 else 0.0
    z = arr[0] if arr[0] > 0 else 0.0
    for obs in arr[1:]:
        occurrence = 1.0 if obs > 0 else 0.0
        p = p + alpha_p * (occurrence - p)
        if obs > 0:
            z = z + alpha_d * (obs - z)
    return [max(0.0, float(p * z))] * horizon
