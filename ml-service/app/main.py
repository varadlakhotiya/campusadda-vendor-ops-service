from fastapi import FastAPI
from app.schemas import MlForecastRequest, MlForecastResponse
from app.service import run_forecast_logic

app = FastAPI(title="CampusAdda ML Service")

@app.get("/health")
def health():
    return {"status": "UP"}

@app.post("/forecast/run", response_model=MlForecastResponse)
def run_forecast(request: MlForecastRequest):
    result = run_forecast_logic(request)
    return result