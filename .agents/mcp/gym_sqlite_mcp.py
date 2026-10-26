#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Gym SQLite & Domain Analytics MCP Server
Provides tools for inspecting Room SQLite databases, validating workout schema,
and calculating strength training metrics (1RM, volume, superset grouping).
"""

import sys
import io
import json
import sqlite3
import os
from typing import Dict, Any, List

# Windows UTF-8 stdio handling
sys.stdin = io.TextIOWrapper(sys.stdin.buffer, encoding='utf-8')
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8')

DEFAULT_DB_PATH = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))), "gym_test.db")

def tool_gym_db_schema(db_path: str = "") -> Dict[str, Any]:
    """Inspect schema and tables of a Room/SQLite database."""
    target_path = db_path.strip() if db_path.strip() else DEFAULT_DB_PATH
    if not os.path.exists(target_path):
        return {
            "error": f"Database file not found at: {target_path}",
            "hint": "Database file is created when Room executes or test suite runs."
        }

    try:
        conn = sqlite3.connect(target_path)
        cursor = conn.cursor()
        cursor.execute("SELECT name, sql FROM sqlite_master WHERE type IN ('table', 'index', 'view') AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'android_metadata';")
        rows = cursor.fetchall()
        schema_objects = [{"name": r[0], "sql": r[1]} for r in rows]
        conn.close()
        return {
            "database_path": target_path,
            "objects_count": len(schema_objects),
            "objects": schema_objects
        }
    except Exception as e:
        return {"error": str(e)}

def tool_gym_db_query(sql: str, db_path: str = "") -> Dict[str, Any]:
    """Execute a query against Room/SQLite database."""
    target_path = db_path.strip() if db_path.strip() else DEFAULT_DB_PATH
    if not os.path.exists(target_path):
        return {"error": f"Database file not found at: {target_path}"}

    try:
        conn = sqlite3.connect(target_path)
        conn.row_factory = sqlite3.Row
        cursor = conn.cursor()
        cursor.execute(sql)
        if sql.strip().upper().startswith("SELECT") or sql.strip().upper().startswith("PRAGMA"):
            rows = cursor.fetchall()
            results = [dict(row) for row in rows]
            conn.close()
            return {"count": len(results), "data": results}
        else:
            conn.commit()
            affected = cursor.rowcount
            conn.close()
            return {"status": "success", "rows_affected": affected}
    except Exception as e:
        return {"error": str(e)}

def tool_gym_calculate_1rm(weight: float, reps: int) -> Dict[str, Any]:
    """Calculate Estimated 1RM (One-Rep Max) using Epley and Brzycki formulas."""
    if weight <= 0 or reps <= 0:
        return {"error": "Weight and reps must be positive numbers."}

    if reps == 1:
        epley = weight
        brzycki = weight
    else:
        epley = round(weight * (1.0 + reps / 30.0), 1)
        brzycki = round(weight * (36.0 / (37.0 - reps)), 1) if reps < 37 else None

    # Suggested training percentages of 1RM
    percentages = {}
    base_1rm = epley
    for pct in [95, 90, 85, 80, 75, 70, 65, 60]:
        percentages[f"{pct}%"] = round(base_1rm * (pct / 100.0), 1)

    return {
        "weight": weight,
        "reps": reps,
        "estimated_1rm_epley": epley,
        "estimated_1rm_brzycki": brzycki,
        "training_percentages_of_max": percentages
    }

TOOLS = [
    {
        "name": "gym_db_schema",
        "description": "Проверить схему таблиц, внешние ключи и индексы Room SQLite базы данных приложения Gym Tracker.",
        "inputSchema": {
            "type": "object",
            "properties": {
                "db_path": {"type": "string", "description": "Абсолютный путь к SQLite .db файлу. Если пустой, используется дефолтный gym_test.db"}
            }
        }
    },
    {
        "name": "gym_db_query",
        "description": "Выполнить SQL-запрос к Room SQLite базе данных приложения Gym Tracker (SELECT, PRAGMA и т.д.).",
        "inputSchema": {
            "type": "object",
            "properties": {
                "sql": {"type": "string", "description": "SQL запрос (например, 'SELECT * FROM exercises LIMIT 10')"},
                "db_path": {"type": "string", "description": "Абсолютный путь к базе данных. Опционально."}
            },
            "required": ["sql"]
        }
    },
    {
        "name": "gym_calculate_1rm",
        "description": "Рассчитать расчетный одноповторный максимум (Estimated 1RM) атлета по формулам Epley и Brzycki и тренировочные проценты.",
        "inputSchema": {
            "type": "object",
            "properties": {
                "weight": {"type": "number", "description": "Поднятый рабочий вес в кг или фунтах"},
                "reps": {"type": "integer", "description": "Количество повторений в подходе"}
            },
            "required": ["weight", "reps"]
        }
    }
]

def handle_request(req):
    method = req.get("method")
    req_id = req.get("id")

    if method == "initialize":
        return {
            "jsonrpc": "2.0",
            "id": req_id,
            "result": {
                "protocolVersion": "2024-11-05",
                "capabilities": {
                    "tools": {"listChanged": False}
                },
                "serverInfo": {
                    "name": "Gym SQLite & Domain Analytics",
                    "version": "1.0.0"
                },
                "instructions": "Инструменты для работы со схемой Room DB, запросами к базе данных и расчетами силовых метрик (1RM, Volume)."
            }
        }
    elif method == "notifications/initialized":
        return None
    elif method == "tools/list":
        return {
            "jsonrpc": "2.0",
            "id": req_id,
            "result": {"tools": TOOLS}
        }
    elif method == "tools/call":
        params = req.get("params", {})
        tname = params.get("name")
        args = params.get("arguments", {})
        try:
            if tname == "gym_db_schema":
                res = tool_gym_db_schema(args.get("db_path", ""))
            elif tname == "gym_db_query":
                res = tool_gym_db_query(args.get("sql", ""), args.get("db_path", ""))
            elif tname == "gym_calculate_1rm":
                res = tool_gym_calculate_1rm(args.get("weight", 0.0), args.get("reps", 0))
            else:
                return {
                    "jsonrpc": "2.0",
                    "id": req_id,
                    "error": {"code": -32601, "message": f"Method {tname} not found"}
                }
            return {
                "jsonrpc": "2.0",
                "id": req_id,
                "result": {
                    "content": [{"type": "text", "text": json.dumps(res, ensure_ascii=False, indent=2)}]
                }
            }
        except Exception as e:
            return {
                "jsonrpc": "2.0",
                "id": req_id,
                "error": {"code": -32000, "message": str(e)}
            }
    elif method == "ping":
        return {"jsonrpc": "2.0", "id": req_id, "result": {}}
    else:
        if req_id is not None:
            return {
                "jsonrpc": "2.0",
                "id": req_id,
                "error": {"code": -32601, "message": f"Unknown method: {method}"}
            }
        return None

def main():
    for line in sys.stdin:
        line = line.strip()
        if not line:
            continue
        try:
            req = json.loads(line)
        except Exception:
            continue
        resp = handle_request(req)
        if resp is not None:
            sys.stdout.write(json.dumps(resp, ensure_ascii=False) + "\n")
            sys.stdout.flush()

if __name__ == "__main__":
    main()
