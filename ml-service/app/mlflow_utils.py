import uuid

def log_run_stub(model_name: str, metrics: dict):
    return f"{model_name.lower()}-{uuid.uuid4()}"