from pathlib import Path
from contextlib import contextmanager
import json
import pandas as pd
import pymysql


def load_config(path: str):
    return json.loads(Path(path).read_text(encoding="utf-8"))


class Db:
    def __init__(self, cfg):
        self.cfg = cfg["mysql"]

    @contextmanager
    def connect(self):
        conn = pymysql.connect(
            host=self.cfg["host"],
            port=int(self.cfg.get("port", 3306)),
            user=self.cfg["user"],
            password=self.cfg["password"],
            database=self.cfg["database"],
            cursorclass=pymysql.cursors.DictCursor,
            autocommit=False,
        )
        try:
            yield conn
            conn.commit()
        except Exception:
            conn.rollback()
            raise
        finally:
            conn.close()

    def read_sql(self, sql: str, params=None) -> pd.DataFrame:
        with self.connect() as conn:
            with conn.cursor() as cur:
                cur.execute(sql, params or ())
                rows = cur.fetchall()
        return pd.DataFrame(rows)

    def execute(self, sql: str, params=None):
        with self.connect() as conn:
            with conn.cursor() as cur:
                return cur.execute(sql, params or ())

    def executemany(self, sql: str, params_list):
        with self.connect() as conn:
            with conn.cursor() as cur:
                return cur.executemany(sql, params_list)