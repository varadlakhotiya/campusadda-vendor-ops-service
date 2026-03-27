from pydantic import BaseModel
from typing import List, Optional, Dict, Any

class ForecastPoint(BaseModel):
    forecastDate: str
    predictedQuantity: float
    lowerBoundQty: Optional[float] = None
    upperBoundQty: Optional[float] = None
    confidenceLevelPct: Optional[float] = None

class MlForecastRequest(BaseModel):
    vendorId: int
    menuItemId: int
    modelName: str
    horizonDays: int
    trainingStartDate: str
    trainingEndDate: str
    salesSeries: List[Dict[str, Any]]
    calendarEvents: List[Dict[str, Any]]

class MlForecastResponse(BaseModel):
    modelVersion: str
    mlflowRunId: Optional[str] = None
    metrics: Dict[str, Any]
    forecasts: List[ForecastPoint]