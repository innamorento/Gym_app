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

CATALOG_PATH = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))), "core", "database", "src", "main", "assets", "exercises_catalog.json")

def tool_gym_exercise_catalog(action: str = "stats", query: str = "", muscle: str = "", equipment: str = "", exercise_id: int = 0) -> Dict[str, Any]:
    """Inspect and search the comprehensive offline exercise catalog with GIFs and instructions."""
    if not os.path.exists(CATALOG_PATH):
        return {"error": f"Catalog not found at: {CATALOG_PATH}"}

    try:
        with open(CATALOG_PATH, "r", encoding="utf-8") as f:
            catalog = json.load(f)
    except Exception as e:
        return {"error": f"Failed to load catalog: {e}"}

    if action == "stats":
        muscles = {}
        equipments = {}
        has_gif = 0
        for x in catalog:
            m = x.get("primaryMuscleGroup", "UNKNOWN")
            e = x.get("equipment", "UNKNOWN")
            muscles[m] = muscles.get(m, 0) + 1
            equipments[e] = equipments.get(e, 0) + 1
            if x.get("gifUrl"):
                has_gif += 1
        return {
            "total_exercises": len(catalog),
            "exercises_with_gif": has_gif,
            "gif_coverage_pct": round(has_gif / len(catalog) * 100, 1) if catalog else 0,
            "muscles_breakdown": muscles,
            "equipments_breakdown": equipments
        }
    elif action == "get":
        for x in catalog:
            if x.get("id") == exercise_id:
                return {"exercise": x}
        return {"error": f"Exercise with id {exercise_id} not found."}
    elif action == "search":
        q = query.strip().lower()
        m_filter = muscle.strip().upper()
        e_filter = equipment.strip().upper()
        matches = []
        for x in catalog:
            if m_filter and x.get("primaryMuscleGroup") != m_filter:
                continue
            if e_filter and x.get("equipment") != e_filter:
                continue
            if q:
                name_ru = x.get("name", "").lower()
                name_en = x.get("nameEn", "").lower()
                if q not in name_ru and q not in name_en:
                    continue
            matches.append({
                "id": x.get("id"),
                "name": x.get("name"),
                "nameEn": x.get("nameEn"),
                "primaryMuscleGroup": x.get("primaryMuscleGroup"),
                "equipment": x.get("equipment"),
                "has_gif": bool(x.get("gifUrl")),
                "gifUrl": x.get("gifUrl"),
                "instructions_preview": (x.get("instructions", "")[:120] + "...") if x.get("instructions") else ""
            })
            if len(matches) >= 50:
                break
        return {"count": len(matches), "matches": matches}
    else:
        return {"error": f"Unknown action: {action}. Use 'stats', 'search', or 'get'."}

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
    },
    {
        "name": "gym_exercise_catalog",
        "description": "Поиск, фильтрация и инспекция базы из 1324 упражнений (GIF-анимации, русские методические инструкции, группы мышц, снаряды).",
        "inputSchema": {
            "type": "object",
            "properties": {
                "action": {"type": "string", "enum": ["stats", "search", "get"], "description": "Действие: 'stats' (статистика каталога), 'search' (поиск по имени/фильтрам), 'get' (получить по ID)"},
                "query": {"type": "string", "description": "Поисковый запрос (русский или английский, напр. 'жим лежа', 'sissy', 'тейта', 'arm')"},
                "muscle": {"type": "string", "description": "Фильтр по группе мышц: CHEST, BACK, LEGS_QUADS, LEGS_HAMSTRINGS, CALVES, SHOULDERS, BICEPS, TRICEPS, CORE, FULL_BODY"},
                "equipment": {"type": "string", "description": "Фильтр по снаряду: BARBELL, DUMBBELL, MACHINE, CABLE, BODYWEIGHT, KETTLEBELL, RESISTANCE_BAND, OTHER"},
                "exercise_id": {"type": "integer", "description": "ID упражнения для действия 'get'"}
            }
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
            elif tname == "gym_exercise_catalog":
                res = tool_gym_exercise_catalog(
                    action=args.get("action", "stats"),
                    query=args.get("query", ""),
                    muscle=args.get("muscle", ""),
                    equipment=args.get("equipment", ""),
                    exercise_id=args.get("exercise_id", 0)
                )
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
