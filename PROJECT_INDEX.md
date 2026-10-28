# Gym Workout Tracker — Project Index & AI Context

> **Version:** 0.3.0  
> **Role:** Fullstack Android Developer & Systems/Product Analyst  
> **Status:** Phase 0 (Onboarding & Profile) & Phase 1 (Foreground Timer) Active  
> **Target Platform:** Android (Offline-First Mobile App) + Sync/Backend Service  
> **Current Workspace:** `C:\Users\Thinkpad X1\Downloads\Gym` (synced with `C:\Dev\Gym`)  
> **Remote Repository:** `https://github.com/innamorento/Gym_app` (branch `main`)

---

## 1. Project Overview & Vision
Разрабатываемый продукт — современное, быстрое и надежное приложение для планирования, создания и отслеживания силовых тренировок в тренажёрном зале (Gym Workout Tracker / Logger).

**Ключевая ценность для пользователя:**
- **Умный онбординг атлета (Phase 0):** сбор антропометрии (рост, вес, пол, возраст), расчет ИМТ, фиксация целей и стажа с автоматической рекомендацией оптимального сплита.
- **Минимальное трение во время тренировки:** quick logging ровно в 1 тап, умное каскадирование веса на последующие подходы.
- **Полноценная работа офлайн прямо в зале:** 100% Offline-First на Room SQLite.
- **Фоновый таймер отдыха с виброоткликом (Phase 1):** Android Foreground Service с Ongoing Notification в шторке, кнопками `+30с` и `Пропустить`, тройной вибросигнал в кармане.
- **Наглядная аналитика прогресса:** суммарный тоннаж, расчетный 1RM по формуле Эпли, динамика личных рекордов (PR), распределение объема по мышцам.
- **Платформа «Тренер-Клиент»:** видеорецензии подходов (до 60 секунд), оценка 1–5 звезд и умная автопрогрессия с дискретностью снарядов зала.

---

## 2. Core Principles & AI Agent Guidelines
Каждый агент и разработчик на проекте обязан следовать правилам:
1. **Clarify Before Build:** Если требование, сценарий или техническое решение неоднозначно или не хватает вводных данных — **всегда задавать уточняющие вопросы пользователю**, не делая слепых предположений.
2. **Document Decisions & Logic:** Любое архитектурное решение, изменение схемы данных или смена приоритетов фиксируется в `docs/decisions/` и `docs/notes/DEV_LOG.md`.
3. **Ergonomics First:** Любой UI/UX сценарий оценивается с позиции атлета в зале (усталость, 1 рука, пот/магнезия, плохой свет, отсутствие сети).
4. **Adaptive Scaling (FlowRow):** Все фильтры и чипы обязаны поддерживать нестандартный масштаб шрифтов и экранов с автоматическим переносом на новую строку.

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
| [`docs/analysis/COMPETITOR_ANALYSIS.md`](./docs/analysis/COMPETITOR_ANALYSIS.md) | Бенчмарк конкурентов: Strong, Hevy, FitNotes, Everfit, JEFIT |
| [`docs/decisions/`](./docs/decisions/) | Architecture Decision Records (ADR) — логика и обоснование каждого решения |
| [`docs/decisions/ADR-001-tech-stack-and-architecture.md`](./docs/decisions/ADR-001-tech-stack-and-architecture.md) | Выбор Kotlin, Jetpack Compose, Room, Clean Architecture + MVI |
| [`docs/decisions/ADR-002-modular-architecture-and-mvp-scope.md`](./docs/decisions/ADR-002-modular-architecture-and-mvp-scope.md) | Модульная архитектура Gradle, скоуп MVP и конструктор суперсетов |
| [`docs/decisions/ADR-003-coach-platform-video-and-smart-progression.md`](./docs/decisions/ADR-003-coach-platform-video-and-smart-progression.md) | Платформа Тренер-Клиент, видеоотчеты, автопрогрессия и дискретность снарядов |
| [`docs/decisions/ADR-004-smart-progression-rpe-and-exercise-clustering.md`](./docs/decisions/ADR-004-smart-progression-rpe-and-exercise-clustering.md) | Субъективная шкала RPE/RIR, 2-уровневый каталог мышц и Smart Swap |
| [`docs/architecture/domain-model.md`](./docs/architecture/domain-model.md) | Доменная модель сущностей (UserProfile, Workout, Exercise, Set, Template) |
| [`docs/notes/DEV_LOG.md`](./docs/notes/DEV_LOG.md) | Инженерный журнал, рассуждения, багфиксы, открытые вопросы и Roadmap |

---

## 4. Core Tech Stack (Approved Baseline)
- **Client (Android):**
  - Language: Kotlin 2.0.x (JVM target 17)
  - UI: Jetpack Compose + Material 3 (Dark theme optimized for gym lighting & battery)
  - Architecture: Multi-Module Clean Architecture + MVI (Model-View-Intent) / UDF
  - Modules: `:core:model`, `:core:database`, `:core:ui`, `:feature:templates`, `:feature:exercises`, `:feature:workout`, `:app`
  - Local Storage (Offline-First): Room DB (SQLite) + Flow Coroutines
  - DI: Koin 4.x (`koin-androidx-compose`)
  - Background Service: Android Foreground Service (`RestTimerService`) с типом `specialUse` + Ongoing Notification + Vibration
  - Video & Media: Android Intent Video Recording (лимит 60с) + FileProvider
- **Web Simulator & Preview:**
  - `preview/index.html` — интерактивный симулятор функционала и верстки

---

## 5. Current Phase & Next Actions
- [x] Инициализация структуры документации и AI-индекса.
- [x] Подготовка PRD и ADR (ADR-001 ... ADR-004).
- [x] Развертывание модульного Android-проекта (`:core:model`, `:core:database`, `:core:ui`, `:feature:templates`, `:feature:exercises`, `:feature:workout`, `:app`).
- [x] Реализация конструктора шаблонов и группировки в суперсеты.
- [x] Реализация экрана живой тренировки `LiveWorkoutScreen` с фиксацией подходов и оценкой RPE.
- [x] Видеофиксация техники атлета (Intent видеокамеры, 60 секунд) и кабинет тренера `CoachReviewScreen`.
- [x] Адаптивная верстка FlowRow для любых масштабов экрана.
- [x] Изоляция сессий редактора шаблонов и добавление упражнений в живую тренировку.
- [x] Каталог упражнений с иллюстрированными бейджами снарядов и подробным `ExerciseDetailSheet` (3D заготовка).
- [x] Анатомические группы мышц (Грудь, Спина, Ноги, Плечи, Бицепс, Трицепс, Руки, Пресс, Прочее).
- [x] Интерактивный ввод и изменение веса/повторений подхода (`SetEditDialog`, степперы, быстрое удаление сетов).
- [x] Конструктор шаблонов: управление подходами `[-] 3 [+]`, временем отдыха и быстрый запуск тренировки из редактора.
- [x] Секундомер тренировки в реальном времени и умный таймер отдыха между подходами с гибкой регулировкой на лету.
- [x] Восстановление Gradle-сборки (AGP 8.6.1, Kotlin 2.0.21, Gradle 8.9 wrapper jar) и успешная сборка debug APK.
- [x] Умное каскадирование веса и повторений (Smart Set Forwarding) на незавершенные подходы.
- [x] База истории тренировок по пользователям (`userId`), хранение всех подходов, предзаполнение весов из прошлой сессии.
- [x] Экран аналитики и прогресса (`AnalyticsScreen`):
  - Автоматический расчет расчетного 1ПМ (1RM) по формуле Эпли: $1\text{RM} = W \times (1 + R/30)$;
  - Графики/история динамики силового рекорда (PR);
  - Диаграмма распределения тоннажа и рабочих сетов по анатомическим группам мышц;
  - Фильтры по периодам (7 дней, 30 дней, Всё время) и суммарный тоннаж.
- [x] Автопосев базы данных (`DefaultTemplates.kt`): 4 стартовые программы и 28+ упражнений с защитой от сброса при миграции Room.
- [x] Быстрые пресеты названий шаблонов и софтверная клавиатура.
- [x] Фоновый сервис таймера отдыха (`RestTimerService`): Android Foreground Service с Ongoing Notification, кнопками `+30с` / `Пропустить` и виброоткликом.
- [x] Доменная модель и база данных профиля атлета (Фаза 0): `UserProfile`, `UserProfileEntity`, `UserProfileDao`, TypeConverters, мапперы, расчет ИМТ и автоподбор программ.
- [x] Обновлен Генеральный стратегический мастер-план `gym_tracker_master_plan.md` до версии 1.1.0 с Фазой 0 и графиком спринтов.
- [ ] UI интерактивного онбординга атлета (`OnboardingScreen` / `OnboardingViewModel`) и роутинг в `GymNavHost`.
- [ ] Алгоритм динамического изменения и подбора весов на следующих тренировках (Double Progression / RPE).
