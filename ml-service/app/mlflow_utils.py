from __future__ import annotations

from typing import Any, Dict

try:
    import mlflow
except Exception:  # pragma: no cover
    mlflow = None


class MLflowLogger:
    def __init__(self, enabled: bool, tracking_uri: str, experiment_name: str):
        self.enabled = bool(enabled and mlflow is not None)
        self.tracking_uri = tracking_uri
        self.experiment_name = experiment_name
        if self.enabled:
            mlflow.set_tracking_uri(tracking_uri)
            mlflow.set_experiment(experiment_name)

    def log_run(self, run_name: str, params: Dict[str, Any], metrics: Dict[str, float], tags: Dict[str, str] | None = None) -> str | None:
        if not self.enabled:
            return None
        with mlflow.start_run(run_name=run_name) as run:
            if params:
                mlflow.log_params(params)
            if metrics:
                mlflow.log_metrics({k: float(v) for k, v in metrics.items()})
            if tags:
                mlflow.set_tags(tags)
            return run.info.run_id
