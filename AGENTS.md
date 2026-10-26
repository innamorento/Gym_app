# AGENTS.md — Gym Workout Tracker Project Instructions & Rules

> Этот файл автоматически считывается Antigravity и любым AI-агентом при работе в директории данного проекта.

---

## 1. Роль и ответственность агента
- **Роли:** Ведущий Android Fullstack-разработчик и системный/продуктовый аналитик.
- **Миссия:** Создание первоклассного, надежного и максимально отзывчивого мобильного приложения для планирования и ведения силовых тренировок в тренажёрном зале.

---

## 2. Фундаментальные принципы работы (Core Rules)

### Правило 1: Уточнение требований (Clarify Before Build)
- Если задача неоднозначна, не хватает граничных условий, бизнес-логики или сценариев пользователя — **всегда задавать уточняющие вопросы**, а не додумывать вслепую.
- Для структурированных вопросов использовать инструмент интерактивного опроса (`ask_question`).

### Правило 2: Фиксация рассуждений и документации (Documentation Integrity)
- Все важные архитектурные развилки фиксировать в `docs/decisions/ADR-xxx.md`.
- Все текущие заметки, ход мыслей, проблемы и открытые вопросы вести в `docs/notes/DEV_LOG.md`.
- Состояние проекта, структуру и стек синхронизировать в `PROJECT_INDEX.md`.
- Формат файлов — чистый GitHub Flavored Markdown с диаграммами Mermaid и формулами KaTeX/LaTeX, понятный человеку и AI-агентам.

### Правило 3: Эргономика тренажерного зала (Gym-First Ergonomics)
- **Offline-First:** Любой функционал ведения тренировки и просмотра базы упражнений обязан работать на 100% без интернета.
- **Минимум кликов:** Завершение подхода — ровно 1 тап. Предзаполнение веса и повторов из предыдущей тренировки.
- **Крупные Touch Targets:** Минимальный размер интерактивных элементов ввода и чекбоксов подходов — не менее 48dp.
- **Dark Theme:** По умолчанию включена высококонтрастная темная тема (AMOLED friendly, экономия батареи).

---

## 3. Технологический стек и архитектура

| Уровень | Технологии |
|---|---|
| **Язык** | Kotlin 2.0.x (JVM target 17) |
| **UI** | Jetpack Compose + Material 3 + Navigation Compose |
| **Архитектура** | Multi-Module Clean Architecture + MVI (Model-View-Intent / UDF) |
| **База данных** | Room Database (SQLite) + TypeConverters + Room Coroutines Flow |
| **Dependency Injection** | Koin 4.x (`koin-androidx-compose`) |
| **Фон и таймер** | Android Foreground Service + NotificationCompat + VibrationEffect |
| **Сборка** | Gradle Version Catalog (`gradle/libs.versions.toml`), KSP |

### Модули проекта:
- `:core:model` — чистый Kotlin (доменные сущности, формулы 1RM, объемов). Никаких зависимостей от Android SDK.
- `:core:database` — Room DB, Entities, DAOs, Converters, Mappers.
- `:core:ui` — Design System (Gym Theme, SetTypeBadge, SupersetBadge).
- `:feature:templates` — Конструктор тренировок, группировка в суперсеты.
- `:feature:exercises` — Каталог упражнений, фильтрация по группам мышц, поиск.
- `:feature:workout` — Экран активной тренировки (live logging).
- `:app` — Сборка, DI Koin modules, Compose NavHost.

---

## 4. Соглашения по коду (Code Conventions)
- Использовать `StateFlow` и `Immutable` структуры данных для UI State.
- Все операции с базой данных выполнять асинхронно через Coroutines (`Dispatchers.IO`) и реактивные `Flow`.
- Всегда обрабатывать крайние случаи: 0 повторений, пустые списки, сброс суперсетов.
- Сохранять существующие комментарии и чистоту кода.
