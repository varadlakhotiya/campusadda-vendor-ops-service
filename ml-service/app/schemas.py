from __future__ import annotations

from pydantic import BaseModel
from pydantic import BaseModel, Field, ConfigDict


class TrainAndSelectRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True)
    config_path: str | None = Field(default=None, alias="configPath")


class PredictPersistRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True)
    config_path: str | None = Field(default=None, alias="configPath")
    horizon_days: int = 7


class ReorderRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True)
    config_path: str | None = Field(default=None, alias="configPath")



class AnomalyRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True)
    config_path: str | None = Field(default=None, alias="configPath")
    vendor_id: int | None = Field(default=None, alias="vendorId")

