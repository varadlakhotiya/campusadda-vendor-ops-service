from __future__ import annotations

import json
from pathlib import Path
from typing import Any, Dict


def ensure_dir(path: Path) -> Path:
    path.mkdir(parents=True, exist_ok=True)
    return path


def save_registry(base_dir: str, payload: Dict[str, Any]) -> Path:
    registry_dir = ensure_dir(Path(base_dir) / "registry")
    file_path = registry_dir / "model_registry.json"
    file_path.write_text(json.dumps(payload, indent=2), encoding="utf-8")
    return file_path


def load_registry(base_dir: str) -> Dict[str, Any]:
    file_path = Path(base_dir) / "registry" / "model_registry.json"
    return json.loads(file_path.read_text(encoding="utf-8"))
