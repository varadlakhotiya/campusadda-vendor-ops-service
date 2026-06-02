from __future__ import annotations

import json
import os
from pathlib import Path

from fastapi import FastAPI

from app.jobs.generate_anomalies import run_anomaly_job
from app.jobs.generate_reorder_recommendations import run_reorder_job
from app.jobs.predict_and_persist import run_predict_job
from app.jobs.train_and_select import run_training_job
from app.schemas import AnomalyRequest, PredictPersistRequest, ReorderRequest, TrainAndSelectRequest

BASE_DIR = Path(__file__).resolve().parents[1]
APP_DEFAULT_CONFIG = os.getenv("ML_TRAINING_CONFIG_PATH", "config/ml_training_config.json")

app = FastAPI(title="CampusAdda Strong ML Service", version="3.1")


def _load_config(path: str | None) -> dict:
    file_path = Path(path or APP_DEFAULT_CONFIG)
    if not file_path.is_absolute():
        file_path = BASE_DIR / file_path

    content = file_path.read_text(encoding="utf-8")

    replacements = {
        "${MYSQL_HOST}": os.getenv("MYSQL_HOST", ""),
        "${MYSQL_PORT}": os.getenv("MYSQL_PORT", "3306"),
        "${MYSQL_USER}": os.getenv("MYSQL_USER", ""),
        "${MYSQL_PASSWORD}": os.getenv("MYSQL_PASSWORD", ""),
        "${MYSQL_DATABASE}": os.getenv("MYSQL_DATABASE", "")
    }

    for placeholder, value in replacements.items():
        content = content.replace(placeholder, str(value))

    return json.loads(content)


@app.get("/health")
def health():
    return {"status": "ok", "service": "campusadda-strong-ml"}


@app.post("/jobs/train-and-select")
def train_and_select(request: TrainAndSelectRequest):
    return run_training_job(_load_config(request.config_path))


@app.post("/jobs/predict-and-persist")
def predict_and_persist(request: PredictPersistRequest):
    return run_predict_job(_load_config(request.config_path), request.horizon_days)


@app.post("/jobs/generate-reorder-recommendations")
@app.post("/jobs/reorder/generate")
def generate_reorder(request: ReorderRequest):
    return run_reorder_job(_load_config(request.config_path))


@app.post("/jobs/generate-anomalies")
@app.post("/jobs/anomalies/generate")
def generate_anomalies(request: AnomalyRequest):
    return run_anomaly_job(_load_config(request.config_path))
