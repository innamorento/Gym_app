# Task Backlog & Traceability Register

> **File Purpose:** Центральный реестр задач, доработок и архитектурных изменений проекта Gym Workout Tracker (`The Iron Apex`).
> **AI Usage:** Все AI-агенты и модели обращаются к этому файлу для сопоставления меток вида `[AI-TASK:TASK-xxx]` в исходном коде с контекстом требований и проверочными критериями.
> **Language Policy:** Описания задач ведутся на русском (для человека), а ключевые AI-резюме и метки в коде — на лаконичном английском для максимальной экономии токенов (`Token-Optimized English`).

---

## 1. Backlog Summary Table

| Task ID | Status | Module(s) | Summary (AI / EN) | Описание задачи (RU) |
|---|---|---|---|---|
| `TASK-023` | `DONE` | `:app`, `:core:ui`, `:feature:templates`, `:feature:exercises`, `:feature:workout` | Android Launcher Adaptive Icon, Tactical Profile Badge & Global Icon Scale Harmonization | Обновление системной адаптивной иконки приложения на рабочем столе (`ic_launcher`), замена иконки профиля в TopBar на тактический вектор `ic_action_profile` и масштабное увеличение читаемости иконок сплитов (30dp + бейдж), мышц и оборудования |
| `TASK-022` | `DONE` | `:core:ui`, `:core:model`, `:feature:workout`, `:feature:templates`, `:feature:exercises` | Unified Tactical Icon Polish, RPE Autoregulation & Ergonomics Fixes | Устранение дефектов читаемости и масштаба иконок (TopBar, BottomNav, Chips, Timer, Video, Calc), живая RPE-авторегуляция подходов, фикс автосворачивания с 0 кг веса и исключение перезаписи веса |
| `TASK-021` | `DONE` | `:core:ui`, `:feature:templates`, `:feature:workout` | Tactical Iron Hex Full Vector Asset Refresh | Полная замена векторных иконок приложения на высокодетализированную графику из `icon new` (37 ресурсов) |
| `TASK-017` | `DONE` | `:feature:workout` | Full-input card collapse UX | Сворачивание упражнения строго после полного ввода: чекбокс + вес + повторы + RPE |
| `TASK-018` | `DONE` | `:core:model`, `:feature:workout` | Dynamic 1RM Epley overload engine | Научный перерасчет рабочего веса по формуле 1ПМ (Epley) при превышении целевых повторов |
| `TASK-019` | `DONE` | `:core:database`, `:feature:workout` | Adaptive muscle plan balancing | Автоматическая корректировка (+1 подход) в шаблонах при выявлении мышечного дисбаланса |
| `TASK-020` | `DONE` | `:app` | Unified root APK deploy automation | Автоматическое сохранение `TheIronApex-v<version>-debug.apk` в корень проекта через Gradle |
| `TASK-016` | `DONE` | `:core:database`, `:core:model`, `:feature:templates` | Weekly Workout Plans routine engine | Недельные программы тренировок (PPL, Верх/Низ, Сплит Арнольда) и виджет «Фокус сегодня» |
| `TASK-015` | `DONE` | `:core:ui` | Tactical Iron Hex vector icons | Векторные иконки групп мышц, целей и сплитов в фирменном шестигранном стиле |

---

## 2. Detailed Task Cards

### [TASK-023] Android Launcher Adaptive Icon, Tactical Profile Badge & Global Icon Scale Harmonization
- **Status:** `DONE`
- **Module:** `:app`, `:core:ui`, `:feature:templates`, `:feature:exercises`, `:feature:workout`
- **AI Summary (EN):** Updated system Android Launcher adaptive icons (`ic_launcher_foreground.xml`, `ic_launcher_background.xml`, `ic_launcher_monochrome.xml`) to use the new tactical emblem with 72% safe-zone scale. Converted and wired user-provided `Gemini_Generated_Профиль.svg` -> `ic_action_profile.xml` replacing generic `Icons.Default.AccountCircle` in `TemplatesListScreen`. Scaled preset split chips in `TemplateEditorScreen` to 30dp inside a dedicated 36dp `#141720` contrast badge. Harmonized muscle category and equipment chips in `ExercisesScreen` (16/18dp -> 24dp) and `CreateExerciseDialog` (22/24dp). Upgraded `TrophyHex` in `AnalyticsScreen` (20dp -> 28dp) and `ShareRecord` in `WorkoutSummaryScreen` and `CoachReviewScreen` (20dp -> 24/26dp).
- **Problem (RU):** 
  1. На рабочем столе смартфона отображалась старая иконка-заглушка вместо нового титанового логотипа с молнией.
  2. Иконка профиля атлета в верхнем баре (`TemplatesListScreen.kt`) была стандартным круглым символом Material Design `AccountCircle`, выбиваясь из тактического стиля.
  3. Иконки сплитов в чипсах названий (`TemplateEditorScreen.kt`) были мелкими (22dp) и сливались с темным фоном чипса, делая силуэты мышц неразборчивыми.
  4. В каталоге упражнений (`ExercisesScreen.kt`), диалоге создания (`CreateExerciseDialog.kt`) и аналитике (`AnalyticsScreen.kt`) иконки мышц, оборудования и трофея рекордов имели маленький размер (16–20dp) и в некоторых местах были принудительно закрашены монохромным тинтом.
- **Solution (RU):**
  1. В `app/src/main/res/drawable/` обновлены `ic_launcher_foreground.xml` (215 путей нового логотипа с масштабом `0.72` в безопасную зону адаптивной маски), `ic_launcher_background.xml` (`#0A0B0E`) и `ic_launcher_monochrome.xml` (контур для Material You).
  2. Сконвертирован файл `Gemini_Generated_Профиль.svg` в `ic_action_profile.xml`, добавлена константа `GymIcons.Profile`, заменена иконка в TopBar на `26dp` без тинта (`tint = Color.Unspecified`).
  3. В `TemplateEditorScreen.kt` иконки сплитов увеличены до `30dp` и помещены в контрастный бейдж `36x36dp` цвета `#141720` со скруглением 8dp; отступы чипса расширены до 12x7dp, текст увеличен до 13sp SemiBold.
  4. В `ExercisesScreen.kt` и `CreateExerciseDialog.kt` иконки категорий и снарядов увеличены до `24dp` со снятием тинта. В `AnalyticsScreen.kt` кубок `TrophyHex` увеличен до `28dp` с возвратом авторской палитры золота и лайма. В `WorkoutSummaryScreen.kt` и `CoachReviewScreen.kt` иконка `ShareRecord` увеличена до `24–26dp`.
- **Affected Files:**
  - `app/src/main/res/drawable/ic_launcher_foreground.xml`, `ic_launcher_background.xml`, `ic_launcher_monochrome.xml` (`[AI-TASK:TASK-023]`)
  - `core/ui/src/main/res/drawable/ic_action_profile.xml` (`[AI-TASK:TASK-023]`)
  - `core/ui/src/main/kotlin/com/gym/core/ui/icon/GymIcons.kt` (`[AI-TASK:TASK-023]`)
  - `feature/templates/src/main/kotlin/com/gym/feature/templates/ui/TemplatesListScreen.kt` (`[AI-TASK:TASK-023]`)
  - `feature/templates/src/main/kotlin/com/gym/feature/templates/ui/TemplateEditorScreen.kt` (`[AI-TASK:TASK-023]`)
  - `feature/exercises/src/main/kotlin/com/gym/feature/exercises/ui/ExercisesScreen.kt` (`[AI-TASK:TASK-023]`)
  - `feature/exercises/src/main/kotlin/com/gym/feature/exercises/ui/CreateExerciseDialog.kt` (`[AI-TASK:TASK-023]`)
  - `feature/workout/src/main/kotlin/com/gym/feature/workout/ui/LiveWorkoutScreen.kt` (`[AI-TASK:TASK-023]`)
  - `feature/workout/src/main/kotlin/com/gym/feature/workout/analytics/AnalyticsScreen.kt` (`[AI-TASK:TASK-023]`)
  - `feature/workout/src/main/kotlin/com/gym/feature/workout/ui/WorkoutSummaryScreen.kt` (`[AI-TASK:TASK-023]`)
  - `feature/workout/src/main/kotlin/com/gym/feature/workout/ui/CoachReviewScreen.kt` (`[AI-TASK:TASK-023]`)

---

### [TASK-022] Unified Tactical Icon Polish, RPE Autoregulation & Ergonomics Fixes
- **Status:** `DONE`
- **Module:** `:core:ui`, `:core:model`, `:feature:workout`, `:feature:templates`, `:feature:exercises`
- **AI Summary (EN):** Fixed icon scale and contrast across all screens (TopBar, BottomNav, Chips, Video button, Plate Calculator, Rest Timer). Prevented monochrome tinting on multi-colored vector assets (`tint = Color.Unspecified`). Implemented live single-set RPE autoregulation (RTS standard). Fixed bodyweight (0.0kg) exercise card auto-collapse on full logging. Prevented AI weight recommendation from overwriting manual weight input before set logging. Added 10 tactical vector assets (9 equipment + 1 AI bulb) to replace missing assets and emojis.
- **Problem (RU):** 
  1. Векторные многоцветные иконки с тёмным карбоном при наложении монохромного `tint` превращались в сплошные зелёные / серые пятна (BottomNav, Plate Calculator, Rest Timer, Video).
  2. Иконки верхнего бара, нижнего бара, чипсов сплитов и групп мышц имели несогласованный и мелкий размер (13–14dp), не читались на экранах смартфонов.
  3. Текст "ВИДЕО" ломался на две строки ("ВИДЕ / О") в таблице подходов.
  4. При выполнении упражнения со свободным весом (0.0 кг) карточка не сворачивалась автоматически после отметки RPE.
  5. Ручной ввод веса в подходе перезаписывался рекомендацией ИИ до нажатия чекбокса завершения.
  6. Оценка сложности подхода (RPE: «Легко», «В цель», «Тяжело», «Отказ») игнорировалась на уровне текущей тренировки и не корректировала вес следующего сета.
  7. Отсутствовали тактические векторные иконки типов снарядов (штанга, гантели, тренажер, трос и т.д.) и иконка ИИ «лампочка» (использовались эмодзи `💡`, `🏋️`).
- **Solution (RU):**
  1. Созданы 10 векторных ресурсов в стиле Tactical Iron Hex: `ic_equipment_barbell`, `ic_equipment_dumbbell`, `ic_equipment_machine`, `ic_equipment_cable`, `ic_equipment_bodyweight`, `ic_equipment_weighted_bodyweight`, `ic_equipment_kettlebell`, `ic_equipment_band`, `ic_equipment_other`, `ic_action_ai_bulb`.
  2. В `GymNavHost` размер иконок увеличен до 28dp (26dp для неактивных с альфой), снят принудительный монохромный `tint` (`tint = Color.Unspecified`), что раскрыло многослойный стиль титана и неона.
  3. В `LiveWorkoutScreen`: калькулятор дисков увеличен до 28dp, таймер отдыха до 26dp, кнопка видео 28dp (в рамке 48x48dp), колонка видео оптимизирована иконкой 16dp вместо ломающегося текста "ВИДЕО", иконка лампочки ИИ и целей увеличены до 18dp.
  4. Добавлен `LaunchedEffect(isFullyLogged) { if (isFullyLogged) isManuallyExpanded = false }`, обеспечивший автоматическое схлопывание карточки после выставления RPE даже при весе 0.0 кг.
  5. В `WorkoutViewModel.kt` удален преждевременный вызов `applyProgressionRecommendation` при редактировании полей веса/повторов, предотвратив перезапись пользовательских данных.
  6. В `SmartProgressionEngine.kt` реализована научная RTS-авторегуляция: отметка «Тяжело» сохраняет вес без повышения, «Легко» повышает нагрузку, «Отказ» рекомендует продлить отдых и снизить вес при необходимости. Добавлены unit-тесты.
  7. В `TemplateEditorScreen` и `ExercisesScreen` заменены эмодзи на векторные чипы со снарядами и увеличены иконки групп мышц до 22dp.
- **Affected Files:**
  - `core/ui/src/main/res/drawable/ic_equipment_*.xml`, `ic_action_ai_bulb.xml` (`[AI-TASK:TASK-022]`)
  - `core/ui/src/main/kotlin/com/gym/core/ui/icon/GymIcons.kt` (`[AI-TASK:TASK-022]`)
  - `core/model/src/main/kotlin/com/gym/core/model/progression/SmartProgressionEngine.kt` (`[AI-TASK:TASK-022]`)
  - `core/model/src/test/kotlin/com/gym/core/model/progression/SmartProgressionEngineTest.kt` (`[AI-TASK:TASK-022]`)
  - `feature/workout/src/main/kotlin/com/gym/feature/workout/WorkoutViewModel.kt` (`[AI-TASK:TASK-022]`)
  - `feature/workout/src/main/kotlin/com/gym/feature/workout/ui/LiveWorkoutScreen.kt` (`[AI-TASK:TASK-022]`)
  - `feature/workout/src/main/kotlin/com/gym/feature/workout/ui/component/PerceivedEffortSelector.kt` (`[AI-TASK:TASK-022]`)
  - `feature/templates/src/main/kotlin/com/gym/feature/templates/ui/TemplatesListScreen.kt` (`[AI-TASK:TASK-022]`)
  - `feature/templates/src/main/kotlin/com/gym/feature/templates/ui/TemplateEditorScreen.kt` (`[AI-TASK:TASK-022]`)
  - `feature/templates/src/main/kotlin/com/gym/feature/templates/onboarding/OnboardingScreen.kt` (`[AI-TASK:TASK-022]`)
  - `feature/exercises/src/main/kotlin/com/gym/feature/exercises/ui/ExercisesScreen.kt` (`[AI-TASK:TASK-022]`)
  - `feature/exercises/src/main/kotlin/com/gym/feature/exercises/ui/CreateExerciseDialog.kt` (`[AI-TASK:TASK-022]`)
  - `app/src/main/kotlin/com/gym/navigation/GymNavHost.kt` (`[AI-TASK:TASK-022]`)

---

### [TASK-021] Tactical Iron Hex Full Vector Asset Refresh
- **Status:** `DONE`
- **Module:** `:core:ui`, `:feature:templates`, `:feature:workout`
- **AI Summary (EN):** Full replacement of all 37 vector assets with high-fidelity 2048x2048 vector icons generated in Recraft (`icon new`). Stripped opaque background rectangles (`M0 0l2048...`) to ensure transparent backdrop and dynamic Compose tinting compatibility. Added `SplitUpperBody`, `SupersetLink`, `EquipmentTune`, `DatabaseBackup`, `ShareRecord`, `GymLogo`. Replaced generic material icons in `TemplateEditorScreen`, `WorkoutSummaryScreen`, `CoachReviewScreen`, `TemplatesListScreen`, and `EquipmentSettingsScreen`.
- **Problem (RU):** Предыдущие векторные иконки были базовыми заглушками или имели неполный охват функционала (отсутствовали кастомные иконки для суперсетов, бэкапов, калибровки, расшаривания рекордов и сплита «Верх тела»).
- **Solution (RU):** Конвертированы 37 SVG-файлов из папки `icon new` в Android `VectorDrawable` XML. Фоновые непрозрачные подложки холста отсечены для бесшовной интеграции в тему. Добавлены новые константы в `GymIcons.kt`, обновлены экраны приложения и витрина `GymIconsShowcase.kt`.
- **Affected Files:**
  - `core/ui/src/main/res/drawable/ic_*.xml` (37 файлов) (`[AI-TASK:TASK-021]`)
  - `core/ui/src/main/kotlin/com/gym/core/ui/icon/GymIcons.kt` (`[AI-TASK:TASK-021]`)
  - `core/ui/src/main/kotlin/com/gym/core/ui/component/GymIconsShowcase.kt` (`[AI-TASK:TASK-021]`)
  - `feature/templates/src/main/kotlin/com/gym/feature/templates/ui/TemplateEditorScreen.kt` (`[AI-TASK:TASK-021]`)
  - `feature/templates/src/main/kotlin/com/gym/feature/templates/ui/TemplatesListScreen.kt` (`[AI-TASK:TASK-021]`)
  - `feature/templates/src/main/kotlin/com/gym/feature/templates/ui/EquipmentSettingsScreen.kt` (`[AI-TASK:TASK-021]`)
  - `feature/workout/src/main/kotlin/com/gym/feature/workout/ui/WorkoutSummaryScreen.kt` (`[AI-TASK:TASK-021]`)
  - `feature/workout/src/main/kotlin/com/gym/feature/workout/ui/CoachReviewScreen.kt` (`[AI-TASK:TASK-021]`)

---

### [TASK-017] Full-Input Card Collapse Ergonomics
- **Status:** `DONE`
- **Module:** `:feature:workout`
- **AI Summary (EN):** Ensure `WorkoutExerciseCard` stays expanded until all sets satisfy: `isCompleted && reps > 0 && effort != null`. Auto-collapses only after RPE effort chip is selected.
- **Problem (RU):** При завершении последнего подхода (тап по чекбоксу) карточка моментально схлопывалась до того, как атлет успевал оценить усилие (RPE: Легко / В цель / Тяжело / Отказ).
- **Solution (RU):** Заменено условие `allCompleted` на `isFullyLogged = sets.all { it.isCompleted && it.reps > 0 && it.effort != null }`. Ручное управление шевроном сохраняет приоритет.
- **Affected Files:**
  - `feature/workout/src/main/kotlin/com/gym/feature/workout/ui/LiveWorkoutScreen.kt` (`[AI-TASK:TASK-017]`)
  - `feature/workout/src/main/kotlin/com/gym/feature/workout/WorkoutViewModel.kt` (`[AI-TASK:TASK-017]`)

---

### [TASK-018] Dynamic 1RM Epley Overload Recalibration Engine
- **Status:** `DONE`
- **Module:** `:core:model`, `:feature:workout`
- **AI Summary (EN):** Dynamic 1RM calculation via Epley formula: $1\text{RM} = W \times (1 + R/30)$ and $W_{\text{target}} = 1\text{RM} / (1 + R_{\text{target}}/30)$. Recalibrates target weight with equipment discrete snapping when actual reps significantly exceed target rep range (e.g., 40kg x 12 reps -> ~47.5kg for 4-6 reps in Strength mode).
- **Problem (RU):** При плане 30 кг х 4 повт. атлет сделал 40 кг х 12 повт., а приложение предложило лишь 35 кг из-за примитивного линейного шага `lastWeight + step * 2`.
- **Solution (RU):** Добавлены `estimate1Rm` и `estimateTargetWeightForReps` в `SmartProgressionEngine`. При `lastReps >= maxReps + 2` целевой вес пересчитывается от 1RM и округляется через `EquipmentWeightProfile.snapWeight()`. В `WorkoutViewModel` свежие значения веса и повторов передаются в расчет немедленно (`overrideWeight`, `overrideReps`).
- **Affected Files:**
  - `core/model/src/main/kotlin/com/gym/core/model/SmartProgressionEngine.kt` (`[AI-TASK:TASK-018]`)
  - `core/model/src/test/kotlin/com/gym/core/model/SmartProgressionEngineTest.kt` (`[AI-TASK:TASK-018]`)
  - `feature/workout/src/main/kotlin/com/gym/feature/workout/WorkoutViewModel.kt` (`[AI-TASK:TASK-018]`)

---

### [TASK-019] Adaptive Muscle Group Balance Auto-Correction
- **Status:** `DONE`
- **Module:** `:core:database`, `:feature:workout`
- **AI Summary (EN):** One-tap auto-balance action in `MuscleGroupBalanceCard` on `AnalyticsScreen`. When `LEGS_DEFICIT`, `PULL_DEFICIT`, or `PUSH_DOMINANT` is detected, automatically increments `targetSetsCount` by +1 (up to 6) in `template_exercises` for lagging muscle groups.
- **Problem (RU):** Экран аналитики сообщал о дефиците объема на ноги или тяговые движения, но атлету приходилось вручную перенастраивать все свои шаблоны.
- **Solution (RU):** В `TemplateDao` добавлен запрос `incrementSetsForExercises()`. В `AnalyticsViewModel` добавлен метод `autoBalanceTemplatesForLaggingMuscles()`. В `AnalyticsScreen` встроена кнопка «⚡ Сбалансировать план на след. цикл» с анимированным подтверждением успеха.
- **Affected Files:**
  - `core/database/src/main/kotlin/com/gym/core/database/dao/TemplateDao.kt` (`[AI-TASK:TASK-019]`)
  - `feature/workout/src/main/kotlin/com/gym/feature/workout/analytics/AnalyticsViewModel.kt` (`[AI-TASK:TASK-019]`)
  - `feature/workout/src/main/kotlin/com/gym/feature/workout/analytics/AnalyticsScreen.kt` (`[AI-TASK:TASK-019]`)
  - `feature/workout/src/main/kotlin/com/gym/feature/workout/di/WorkoutModule.kt` (`[AI-TASK:TASK-019]`)

---

### [TASK-020] Unified Root APK Deploy Automation
- **Status:** `DONE`
- **Module:** `:app`
- **AI Summary (EN):** Automated post-assemble task `CopyApkToRootTask` configured with `doNotTrackState()` and `@get:Internal` properties to guarantee root APK delivery (`TheIronApex-v<version>-debug.apk`) without Gradle configuration cache issues on locked `.gradle/checksums.lock`.
- **Problem (RU):** APK сохранялся в разных местах под случайными именами (`app-debug.apk`), что нарушало эргономику тестирования и Правило 8.
- **Solution (RU):** В `app/build.gradle.kts` зарегистрирована кастомная задача `CopyApkToRootTask`, финализирующая `:app:assembleDebug`.
- **Affected Files:**
  - `app/build.gradle.kts` (`[AI-TASK:TASK-020]`)
  - `AGENTS.md` (Правило 8)

---

### [TASK-016] Weekly Workout Plans Routine Engine
- **Status:** `DONE`
- **Module:** `:core:database`, `:core:model`, `:feature:templates`
- **AI Summary (EN):** Weekly multi-day workout routines (Push/Pull/Legs, Upper/Lower, Arnold Split, Fullbody) with active day resolution (`LocalDate.now()`) and "Today Workout Focus" widget in template list.
- **Affected Files:**
  - `core/model/src/main/kotlin/com/gym/core/model/WorkoutPlan.kt`
  - `core/database/src/main/kotlin/com/gym/core/database/entity/Entities.kt`
  - `core/database/src/main/kotlin/com/gym/core/database/dao/WorkoutPlanDao.kt`
  - `core/database/src/main/kotlin/com/gym/core/database/DefaultTemplates.kt`
  - `feature/templates/src/main/kotlin/com/gym/feature/templates/ui/TemplatesListScreen.kt`

---

### [TASK-015] Tactical Iron Hex Vector Icons
- **Status:** `DONE`
- **Module:** `:core:ui`
- **AI Summary (EN):** Custom 1.8dp bevelled vector drawables replacing emojis across muscle anatomy, fitness goals, and training splits.
- **Affected Files:**
  - `core/ui/src/main/res/drawable/ic_muscle_*.xml`
  - `core/ui/src/main/res/drawable/ic_goal_*.xml`
  - `core/ui/src/main/res/drawable/ic_split_*.xml`
  - `core/ui/src/main/kotlin/com/gym/core/ui/theme/GymIcons.kt`
