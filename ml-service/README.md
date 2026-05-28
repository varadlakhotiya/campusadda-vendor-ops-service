# Reworked ML Training Package for CampusAdda Vendor Ops

This package upgrades the original ML service into a **major-project-grade forecasting and decision-support pipeline** built around your validated `campusadda_vendor_ops_ml` database.

## What this package does

1. Builds a **dense daily panel** from `daily_item_sales` and joins:
   - `menu_items`
   - `menu_categories`
   - `calendar_events`

2. Trains and evaluates multiple model families:
   - **Global XGBoost** for dense series
   - **Prophet** benchmark for dense series
   - **Seasonal Naive / Rolling Mean** baseline
   - **Croston intermittent-demand fallback** for sparse series

3. Runs **time-based backtesting** and selects the best model **per series**.

4. Stores a registry and model artifacts under `artifacts/`.

5. Persists future forecasts into:
   - `forecast_runs`
   - `forecast_values`

6. Generates inventory-aware reorder recommendations into:
   - `reorder_recommendations`

7. Generates demand anomalies into:
   - `anomaly_records`

## Package layout

```text
reworked_ml_training_package/
├── README.md
├── requirements.txt
├── config/
│   └── ml_training_config.example.json
└── app/
    ├── __init__.py
    ├── db.py
    ├── schemas.py
    ├── main.py
    ├── service.py
    ├── mlflow_utils.py
    ├── registry.py
    ├── features.py
    ├── training_data.py
    ├── backtesting.py
    ├── models/
    │   ├── __init__.py
    │   ├── baseline.py
    │   ├── intermittent.py
    │   ├── prophet_model.py
    │   └── xgboost_global.py
    └── jobs/
        ├── __init__.py
        ├── train_and_select.py
        ├── predict_and_persist.py
        ├── generate_reorder_recommendations.py
        └── generate_anomalies.py
```

## Recommended workflow

### 1. Copy and edit config

Copy:

```text
config/ml_training_config.example.json
```

to:

```text
config/ml_training_config.json
```

Then add your DB password and artifact paths.

### 2. Install requirements

```bash
pip install -r requirements.txt
```

### 3. Train and select models

```bash
python -m app.jobs.train_and_select --config config/ml_training_config.json
```

This will:
- build the dense panel
- identify eligible series
- backtest XGBoost / Prophet / baseline / Croston
- choose the best model per series
- save registry + artifacts under `artifacts/`

### 4. Generate and persist forecasts

```bash
python -m app.jobs.predict_and_persist --config config/ml_training_config.json --horizon 7
```

This will populate:
- `forecast_runs`
- `forecast_values`

### 5. Generate reorder recommendations

```bash
python -m app.jobs.generate_reorder_recommendations --config config/ml_training_config.json
```

This will populate:
- `reorder_recommendations`

### 6. Generate anomalies

```bash
python -m app.jobs.generate_anomalies --config config/ml_training_config.json
```

This will populate:
- `anomaly_records`

## Model strategy

### Dense series
Use **Global XGBoost** as the primary model. This is the main strong model for the project.

### Benchmark
Use **Prophet** as a benchmark for dense, seasonal series.

### Sparse series
Use **Croston fallback** for intermittent demand.

### Baseline
Use a strong baseline (`seasonal_naive_7`) and rolling mean for sanity comparison.

## Strong-project upgrades compared to the original scaffold

- Training is no longer done inside the API request.
- Feature engineering is much richer.
- Time-based backtesting is used.
- Series-level model selection is used.
- Event features are actively modeled.
- Forecasts are persisted back into your ML database.
- Reorder and anomaly jobs are included.

## Important notes

- This package is built for the **ML clone database**, not the live DB.
- It keeps your **current business-day logic** by consuming the already-built ETL output (`daily_item_sales`, `daily_vendor_sales`, `hourly_vendor_sales`).
- It expects your ML DB to already contain the validated 110-day synthetic dataset.

## Suggested first run settings

- top 5 vendors by demand
- top 8 items per vendor
- 7-day forecast horizon
- 3 backtest folds
- dense-series threshold: at least 45 calendar days and at least 20 non-zero sales days

## FastAPI

A small FastAPI wrapper is included for operational triggering. Use it only after offline scripts are working.

Run:

```bash
uvicorn app.main:app --reload --port 8010
```
