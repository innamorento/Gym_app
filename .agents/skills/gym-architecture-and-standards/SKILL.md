---
name: gym-architecture-and-standards
description: >-
  Руководство по разработке, архитектурным паттернам и стандартам кодовой базы
  Android-приложения Gym Workout Tracker (Kotlin 2.x, Jetpack Compose, Room, MVI, Koin).
---

# Gym Workout Tracker — Engineering Guidelines & Patterns

## 1. MVI (Model-View-Intent) Pattern в Presentation Layer
Каждая фича следует строгому однонаправленному потоку данных (UDF):
- **UiState:** Неизменяемый `data class`, представляющий полное состояние экрана.
- **Intent / Action:** `sealed interface`, описывающий все действия пользователя (клики, ввод, свайпы).
- **ViewModel:** Обрабатывает `Intent`, вызывает UseCase/DAO и обновляет `MutableStateFlow`, который наружу отдается как `StateFlow<UiState>`.

Пример структуры:
```kotlin
data class FeatureUiState(
    val isLoading: Boolean = false,
    val items: List<Item> = emptyList(),
    val errorMessage: String? = null
)

sealed interface FeatureIntent {
    data class OnItemSelected(val id: Long) : FeatureIntent
    object Refresh : FeatureIntent
}
```

---

## 2. Работа с базой данных (Room Offline-First)
- **Изоляция:** Room Entities (`*Entity`) и DAOs изолированы в модуле `:core:database`. UI модули не знают о Room Entity — они получают чистые доменные модели из `:core:model` через мапперы `toDomain()`.
- **Транзакции:** Любые составные операции (например, создание тренировки вместе со списком упражнений и сетов) должны оборачиваться в `@Transaction`.
- **Каскадное удаление:** Удаление тренировки или шаблона каскадно удаляет связанные упражнения и подходы (`ForeignKey.CASCADE`).
- **Справочники:** Предустановленные упражнения `DefaultExercises` имеют `isCustom = false` и не могут быть удалены пользователем.

---

## 3. Требования к UI и эргономике в зале
1. **Кнопка завершения подхода (Check Set):**
   - Минимальный размер зоны нажатия: 40x40dp (с паддингом 48dp).
   - Четкий виброотклик (`VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)`).
   - Автозапуск таймера отдыха без блокировки интерфейса.
2. **Отображение суперсетов:**
   - Упражнения, связанные общим `supersetId`, группируются визуально и выделяются фиолетовым бейджем `SupersetBadge` и единой рамкой.
3. **Темная тема (AMOLED):**
   - Фон экрана `#121214`, карточки `#1E1E24`, акцентный зеленый `#22C55E`.

---

## 4. MCP Tools Интеграция
Для отладки и проверки состояния Room SQLite базы данных используется MCP сервер `gym-sqlite`:
- `gym_db_schema`: Инспекция схемы и индексов.
- `gym_db_query`: Выполнение контрольных SQL-запросов.
- `gym_calculate_1rm`: Расчет одноповторного максимума и тренировочных зон.
