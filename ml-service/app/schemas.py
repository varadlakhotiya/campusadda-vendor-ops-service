from __future__ import annotations

from pydantic import BaseModel


class TrainAndSelectRequest(BaseModel):
    config_path: str | None = None


class PredictPersistRequest(BaseModel):
    config_path: str | None = None
    horizon_days: int = 7


class ReorderRequest(BaseModel):
    config_path: str | None = None


class AnomalyRequest(BaseModel):
    config_path: str | None = None
