# Gym Workout Tracker — Project Index & AI Context

> **Version:** 0.1.0  
> **Role:** Fullstack Android Developer & Systems/Product Analyst  
> **Status:** Discovery & Architecture Phase  
> **Target Platform:** Android (Offline-First Mobile App) + Sync/Backend Service  

---

## 1. Project Overview & Vision
Разрабатываемый продукт — современное, быстрое и надежное приложение для планирования, создания и отслеживания силовых тренировок в тренажёрном зале (Gym Workout Tracker / Logger).

**Ключевая ценность для пользователя:**
- Минимальное трение во время тренировки (quick logging: 1-2 клика на запись подхода).
- Полноценная работа офлайн прямо в зале (подвальные залы без связи).
- Гибкий конструктор тренировок, шаблонов, упражнений и суперсетов.
- Встроенный таймер отдыха с гибкими уведомлениями.
- Наглядная аналитика прогресса: рабочий объём (Volume), расчетный 1RM (одноповторный максимум), частота, графики.

---

## 2. Core Principles & AI Agent Guidelines
Каждый агент и разработчик на проекте обязан следовать правилам:
1. **Clarify Before Build:** Если требование, сценарий или техническое решение неоднозначно или не хватает вводных данных — **всегда задавать уточняющие вопросы пользователю**, не делая слепых предположений.
2. **Document Decisions & Logic:** Любое архитектурное решение, изменение схемы данных или смена приоритетов фиксируется в `docs/decisions/` и `docs/notes/DEV_LOG.md`.
3. **Ergonomics First:** Любой UI/UX сценарий оценивается с позиции атлета в зале (усталость, 1 рука, пот/магнезия, плохой свет, отсутствие сети).

---

## 3. AI Agent Navigation Map (Knowledge Base Index)
Каждый агент или разработчик может ориентироваться по структуре:

| Файл / Директория | Назначение |
|---|---|
| [`PROJECT_INDEX.md`](./PROJECT_INDEX.md) | Главная точка входа, статус проекта, общие соглашения |
| [`AGENTS.md`](./AGENTS.md) | Системные правила и регламент для AI-агентов (всегда в контексте) |
| [`.agents/skills/gym-architecture-and-standards/SKILL.md`](./.agents/skills/gym-architecture-and-standards/SKILL.md) | Навык и стандарты разработки фичей, MVI и Room |
| [`.agents/mcp/gym_sqlite_mcp.py`](./.agents/mcp/gym_sqlite_mcp.py) | Локальный MCP-сервер для Room/SQLite инспекции и 1RM расчетов |
| [`docs/analysis/PRD.md`](./docs/analysis/PRD.md) | Продуктовые требования, User Stories, анализ предметной области |
| [`docs/decisions/`](./docs/decisions/) | Architecture Decision Records (ADR) — логика и обоснование каждого решения |
| [`docs/decisions/ADR-001-tech-stack-and-architecture.md`](./docs/decisions/ADR-001-tech-stack-and-architecture.md) | Выбор Kotlin, Jetpack Compose, Room, Clean Architecture + MVI |
| [`docs/decisions/ADR-002-modular-architecture-and-mvp-scope.md`](./docs/decisions/ADR-002-modular-architecture-and-mvp-scope.md) | Модульная архитектура Gradle, скоуп MVP и конструктор суперсетов |
| [`docs/decisions/ADR-003-coach-platform-video-and-smart-progression.md`](./docs/decisions/ADR-003-coach-platform-video-and-smart-progression.md) | Платформа Тренер-Клиент, видеоотчеты, автопрогрессия и дискретность снарядов |
| [`docs/architecture/domain-model.md`](./docs/architecture/domain-model.md) | Доменная модель сущностей (Workout, Exercise, Set, Template) |
| [`docs/notes/DEV_LOG.md`](./docs/notes/DEV_LOG.md) | Инженерный журнал, рассуждения, открытые вопросы и TODO |

---

## 4. Core Tech Stack (Approved Baseline)
- **Client (Android):**
  - Language: Kotlin 2.x
  - UI: Jetpack Compose + Material 3 (Dark theme optimized for gym lighting & battery)
  - Architecture: Clean Architecture + MVI (Model-View-Intent) / UDF (Unidirectional Data Flow)
  - Local Storage (Offline-First): Room DB (SQLite) + Jetpack DataStore (Preferences)
  - DI: Koin or Hilt (Koin для лёгкости и скорости сборки, либо Hilt)
  - Async/Concurrency: Kotlin Coroutines & Flow
  - Notifications / Background Work: Android Foreground Services + WorkManager (для таймеров отдыха и синхронизации)
- **Backend / Sync Layer (Phase 2):**
  - Architecture: Offline-first with eventual consistency sync
  - Tech Options: Ktor / Supabase (PostgreSQL + Row-Level Security + Auth)

---

## 5. Current Phase & Next Actions
- [x] Инициализация структуры документации и AI-индекса.
- [x] Подготовка Product Requirements Document (PRD) с анализом функционала.
- [x] Формирование ADR-001 (обоснование стека и архитектуры).
- [x] Доменное моделирование сущностей (Упражнение, Подход, Тренировка, Шаблон).
- [ ] Согласование с пользователем приоритетов MVP и фичей первой версии.
- [ ] Инициализация структуры Android-проекта (Gradle, модули).
