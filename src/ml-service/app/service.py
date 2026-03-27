from app.features import build_features
from app.models.baseline import run_baseline
from app.models.xgboost_model import run_xgboost
from app.models.prophet_model import run_prophet
from app.mlflow_utils import log_run_stub

def run_forecast_logic(request):
    df = build_features(request.salesSeries, request.calendarEvents)

    model_name = (request.modelName or "XGBOOST").upper()

    if model_name == "PROPHET":
        forecasts, metrics = run_prophet(df, request.horizonDays)
    elif model_name == "BASELINE":
        forecasts, metrics = run_baseline(df, request.horizonDays)
    else:
        forecasts, metrics = run_xgboost(df, request.horizonDays)

    mlflow_run_id = log_run_stub(model_name, metrics)

    return {
        "modelVersion": "v1",
        "mlflowRunId": mlflow_run_id,
        "metrics": metrics,
        "forecasts": forecasts
    }