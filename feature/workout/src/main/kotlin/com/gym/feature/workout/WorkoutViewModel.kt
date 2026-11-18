package com.gym.feature.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.core.database.dao.CoachAndReviewDao
import com.gym.core.database.dao.EquipmentSettingsDao
import com.gym.core.database.dao.ExerciseDao
import com.gym.core.database.dao.TemplateDao
import com.gym.core.database.dao.UserProfileDao
import com.gym.core.database.dao.WorkoutDao
import com.gym.core.model.Equipment
import com.gym.core.model.EquipmentWeightProfile
import com.gym.core.model.ProgressionRecommendation
import com.gym.core.model.SmartProgressionEngine
import com.gym.core.model.UserProfile
import kotlinx.coroutines.Dispatchers
import com.gym.core.database.entity.SetVideoReviewEntity
import com.gym.core.database.entity.WorkoutEntity
import com.gym.core.database.entity.WorkoutExerciseEntity
import com.gym.core.database.entity.WorkoutSetEntity
import com.gym.core.database.mapper.toDomain
import com.gym.core.model.Exercise
import com.gym.core.model.PerceivedEffort
import com.gym.core.model.SetType
import com.gym.core.model.Workout
import com.gym.core.model.WorkoutExercise
import com.gym.core.model.WorkoutSet
import com.gym.core.model.WorkoutStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class RestTimerState(
    val isRunning: Boolean = false,
    val totalSeconds: Int = 90,
    val remainingSeconds: Int = 90
)

sealed interface WorkoutUiState {
    object Idle : WorkoutUiState
    data class Active(val workout: Workout) : WorkoutUiState
}

class WorkoutViewModel(
    private val workoutDao: WorkoutDao,
    private val templateDao: TemplateDao,
    private val coachAndReviewDao: CoachAndReviewDao,
    private val userProfileDao: UserProfileDao? = null,
    private val context: android.content.Context? = null,
    private val equipmentSettingsDao: EquipmentSettingsDao? = null,
    private val exerciseDao: ExerciseDao? = null
) : ViewModel() {

    val uiState: StateFlow<WorkoutUiState> = workoutDao.getActiveWorkout()
        .map { workoutWithDetails ->
            if (workoutWithDetails != null) {
                WorkoutUiState.Active(workoutWithDetails.toDomain())
            } else {
                WorkoutUiState.Idle
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WorkoutUiState.Idle
        )

    val completedWorkouts: StateFlow<List<Workout>> = workoutDao.getCompletedWorkouts()
        .map { list -> list.map { it.toDomain() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _workoutDurationSeconds = MutableStateFlow(0L)
    val workoutDurationSeconds: StateFlow<Long> = _workoutDurationSeconds.asStateFlow()

    private val _restTimerState = MutableStateFlow(RestTimerState())
    val restTimerState: StateFlow<RestTimerState> = _restTimerState.asStateFlow()

    private val _progressionRecommendations = MutableStateFlow<Map<Long, ProgressionRecommendation>>(emptyMap())
    val progressionRecommendations: StateFlow<Map<Long, ProgressionRecommendation>> = _progressionRecommendations.asStateFlow()

    private val _isAutoProgressionEnabled = MutableStateFlow(false)
    val isAutoProgressionEnabled: StateFlow<Boolean> = _isAutoProgressionEnabled.asStateFlow()

    fun toggleAutoProgression() {
        _isAutoProgressionEnabled.update { enabled ->
            val next = !enabled
            if (next) {
                // Мгновенно применяем все рассчитанные рекомендации ИИ ко всем упражнениям текущей тренировки
                val recs = _progressionRecommendations.value
                recs.forEach { (exerciseId, rec) ->
                    applyProgressionRecommendation(exerciseId, rec)
                }
            }
            next
        }
    }

    val equipmentProfiles: StateFlow<Map<Equipment, EquipmentWeightProfile>> = (equipmentSettingsDao?.getAllSettings() ?: flowOf(emptyList()))
        .map { list ->
            val map = Equipment.entries.associateWith { EquipmentWeightProfile.defaultFor(it) }.toMutableMap()
            list.forEach { entity ->
                map[entity.equipment] = EquipmentWeightProfile(
                    equipment = entity.equipment,
                    smallWeightThreshold = 10.0,
                    smallWeightStep = entity.smallWeightStep,
                    defaultStep = entity.defaultStep,
                    barWeight = entity.barWeight,
                    minPlatesPairWeight = entity.minPlatesPairWeight
                )
            }
            map
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = Equipment.entries.associateWith { EquipmentWeightProfile.defaultFor(it) }
        )

    val allActiveExercises: StateFlow<List<Exercise>> = (exerciseDao?.getActiveExercises() ?: flowOf(emptyList()))
        .map { list -> list.map { it.toDomain() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var durationTimerJob: Job? = null
    private var restTimerJob: Job? = null

    init {
        if (context != null) {
            viewModelScope.launch {
                com.gym.feature.workout.service.RestTimerService.serviceTimerState.collect { state ->
                    _restTimerState.value = state
                }
            }
        }
        // Запуск и синхронизация секундомера тренировки при наличии активной тренировки
        viewModelScope.launch {
            uiState.collectLatest { state ->
                when (state) {
                    is WorkoutUiState.Active -> {
                        startDurationTicker(state.workout.startTime)
                        loadProgressionRecommendations(state.workout)
                    }
                    is WorkoutUiState.Idle -> {
                        durationTimerJob?.cancel()
                        _workoutDurationSeconds.value = 0L
                        stopRestTimer()
                        _progressionRecommendations.value = emptyMap()
                    }
                }
            }
        }
    }

    private fun startDurationTicker(startTime: Long) {
        durationTimerJob?.cancel()
        durationTimerJob = viewModelScope.launch {
            while (isActive) {
                val elapsed = (System.currentTimeMillis() - startTime) / 1000
                _workoutDurationSeconds.value = elapsed.coerceAtLeast(0)
                delay(1000)
            }
        }
    }

    fun startRestTimer(seconds: Int = 90) {
        val safeSeconds = seconds.coerceAtLeast(1)
        if (context != null) {
            com.gym.feature.workout.service.RestTimerService.start(context, safeSeconds)
        }
        restTimerJob?.cancel()
        _restTimerState.value = RestTimerState(isRunning = true, totalSeconds = safeSeconds, remainingSeconds = safeSeconds)
        restTimerJob = viewModelScope.launch {
            while (isActive && _restTimerState.value.remainingSeconds > 0) {
                delay(1000)
                _restTimerState.update { current ->
                    if (current.remainingSeconds > 1) {
                        current.copy(remainingSeconds = current.remainingSeconds - 1)
                    } else {
                        current.copy(isRunning = false, remainingSeconds = 0)
                    }
                }
            }
        }
    }

    fun adjustRestTimer(deltaSeconds: Int) {
        if (context != null) {
            com.gym.feature.workout.service.RestTimerService.adjust(context, deltaSeconds)
        }
        _restTimerState.update { current ->
            val newRemaining = (current.remainingSeconds + deltaSeconds).coerceAtLeast(0)
            val newTotal = (current.totalSeconds + deltaSeconds).coerceAtLeast(newRemaining)
            current.copy(
                totalSeconds = newTotal,
                remainingSeconds = newRemaining,
                isRunning = newRemaining > 0
            )
        }
        if (_restTimerState.value.isRunning && restTimerJob?.isActive != true) {
            // Перезапуск корутины если оставалось 0
            startRestTimer(_restTimerState.value.remainingSeconds)
        }
    }

    fun setRestTimer(seconds: Int) {
        startRestTimer(seconds)
    }

    fun stopRestTimer() {
        if (context != null) {
            com.gym.feature.workout.service.RestTimerService.stop(context)
        }
        restTimerJob?.cancel()
        _restTimerState.value = RestTimerState(isRunning = false, remainingSeconds = 0)
    }

    fun attachVideoToSet(setId: Long, uriString: String) {
        viewModelScope.launch {
            coachAndReviewDao.attachVideoToSet(
                SetVideoReviewEntity(
                    setId = setId,
                    localVideoUri = uriString,
                    durationSeconds = 60
                )
            )
        }
    }

    fun getEquipmentProfile(equipment: Equipment): EquipmentWeightProfile {
        return equipmentProfiles.value[equipment] ?: EquipmentWeightProfile.defaultFor(equipment)
    }

    // [AI-TASK:TASK-018] Instant override weights/reps support for 1RM live recalculation
    private suspend fun computeRecommendationForExercise(
        exercise: WorkoutExercise,
        overrideSetId: Long? = null,
        overrideIsCompleted: Boolean? = null,
        effort: PerceivedEffort? = null,
        overrideWeight: Double? = null,
        overrideReps: Int? = null
    ): ProgressionRecommendation {
        val userProfile = userProfileDao?.getUserProfileSync()?.toDomain() ?: UserProfile()
        val currentSets = exercise.sets.map { set ->
            var s = set
            if (set.id == overrideSetId) {
                if (overrideIsCompleted != null) s = s.copy(isCompleted = overrideIsCompleted)
                if (effort != null) s = s.copy(effort = effort)
                if (overrideWeight != null) s = s.copy(weight = overrideWeight)
                if (overrideReps != null) s = s.copy(reps = overrideReps)
            }
            s
        }
        val currentCompleted = currentSets.filter { it.isCompleted && it.setType != SetType.WARMUP }
        val setsToAnalyze = if (currentCompleted.isNotEmpty()) {
            currentCompleted
        } else {
            workoutDao.getLatestCompletedSetsForExercise(exercise.exercise.id, userProfile.id)
                .map { it.toDomain() }
        }

        val profile = getEquipmentProfile(exercise.exercise.equipment)
        return SmartProgressionEngine.calculateGoalAwareLoad(
            previousSets = setsToAnalyze,
            equipmentProfile = profile,
            goal = userProfile.primaryGoal,
            experience = userProfile.experience
        )
    }

    fun loadProgressionRecommendations(workout: Workout) {
        viewModelScope.launch(Dispatchers.IO) {
            val recs = mutableMapOf<Long, ProgressionRecommendation>()
            for (exercise in workout.exercises) {
                recs[exercise.id] = computeRecommendationForExercise(exercise)
            }
            _progressionRecommendations.value = recs
        }
    }

    fun applyProgressionRecommendation(workoutExerciseId: Long, recommendation: ProgressionRecommendation) {
        viewModelScope.launch(Dispatchers.IO) {
            val activeWorkout = (uiState.value as? WorkoutUiState.Active)?.workout ?: return@launch
            val exercise = activeWorkout.exercises.find { it.id == workoutExerciseId } ?: return@launch
            val safeWeight = recommendation.suggestedWeight.coerceAtLeast(0.0)
            val parsedReps = recommendation.suggestedReps.split("-").firstOrNull()?.toIntOrNull() ?: 10

            for (set in exercise.sets) {
                if (!set.isCompleted) {
                    workoutDao.updateSetWeightAndReps(
                        setId = set.id,
                        weight = safeWeight,
                        reps = parsedReps
                    )
                }
            }
        }
    }

    fun submitCoachFeedback(reviewId: Long, setId: Long, uri: String, comment: String, rating: Int, coachName: String) {
        viewModelScope.launch {
            coachAndReviewDao.submitCoachReview(
                SetVideoReviewEntity(
                    id = reviewId,
                    setId = setId,
                    localVideoUri = uri,
                    feedbackComment = comment,
                    coachRating = rating,
                    coachName = coachName,
                    reviewedAt = System.currentTimeMillis()
                )
            )
        }
    }

    private suspend fun getCurrentUserId(): String {
        return kotlinx.coroutines.withContext(Dispatchers.IO) {
            userProfileDao?.getUserProfileSync()?.id ?: "default_user"
        }
    }

    fun startWorkoutFromTemplate(templateId: Long) {
        viewModelScope.launch {
            val templateWithExercises = templateDao.getTemplateWithExercises(templateId) ?: return@launch
            val template = templateWithExercises.toDomain()
            val currentUserId = getCurrentUserId()

            val workoutId = workoutDao.insertWorkout(
                WorkoutEntity(
                    userId = currentUserId,
                    templateId = template.id,
                    title = template.title,
                    startTime = System.currentTimeMillis(),
                    status = WorkoutStatus.IN_PROGRESS
                )
            )

            template.exercises.forEachIndexed { index, templateExercise ->
                val workoutExerciseId = workoutDao.insertWorkoutExercise(
                    WorkoutExerciseEntity(
                        workoutId = workoutId,
                        exerciseId = templateExercise.exercise.id,
                        orderIndex = index,
                        supersetId = templateExercise.supersetId
                    )
                )

                // Запрашиваем историю последних подходов этого упражнения для предзаполнения
                val prevSets = workoutDao.getLatestCompletedSetsForExercise(templateExercise.exercise.id, currentUserId)
                val sets = (1..templateExercise.targetSetsCount).map { setNumber ->
                    val prevForSet = prevSets.getOrNull(setNumber - 1) ?: prevSets.firstOrNull()
                    val defaultWeight = prevForSet?.weight ?: 0.0
                    val defaultReps = prevForSet?.reps ?: 0
                    WorkoutSetEntity(
                        workoutExerciseId = workoutExerciseId,
                        setNumber = setNumber,
                        setType = SetType.NORMAL,
                        weight = defaultWeight,
                        reps = defaultReps,
                        isCompleted = false
                    )
                }
                workoutDao.insertWorkoutSets(sets)
            }
        }
    }

    fun toggleSetCompletion(setId: Long, currentIsCompleted: Boolean, defaultRestSeconds: Int = 90) {
        viewModelScope.launch {
            val newCompleted = !currentIsCompleted
            val completedTime = if (newCompleted) System.currentTimeMillis() else null
            workoutDao.updateSetCompletion(setId, newCompleted, completedTime)
            if (newCompleted) {
                startRestTimer(defaultRestSeconds)
            }

            // Пересчитываем рекомендацию для упражнения с учетом фактически выполненного сета
            val active = (uiState.value as? WorkoutUiState.Active)?.workout
            if (active != null) {
                val exercise = active.exercises.find { ex -> ex.sets.any { it.id == setId } }
                if (exercise != null) {
                    val updatedRec = computeRecommendationForExercise(
                        exercise = exercise,
                        overrideSetId = setId,
                        overrideIsCompleted = newCompleted
                    )
                    _progressionRecommendations.update { current ->
                        current + (exercise.id to updatedRec)
                    }
                    if (newCompleted && _isAutoProgressionEnabled.value) {
                        applyProgressionRecommendation(exercise.id, updatedRec)
                    }
                }
            }
        }
    }

    fun updateSetWeightAndReps(setId: Long, weight: Double, reps: Int) {
        viewModelScope.launch {
            val safeWeight = weight.coerceAtLeast(0.0)
            val safeReps = reps.coerceAtLeast(0)
            workoutDao.updateSetWeightAndReps(
                setId = setId,
                weight = safeWeight,
                reps = safeReps
            )

            // Умное каскадирование: переносим введенные вес и повторы на последующие еще не завершенные подходы
            val activeWorkout = (uiState.value as? WorkoutUiState.Active)?.workout ?: return@launch
            for (exercise in activeWorkout.exercises) {
                val setIndex = exercise.sets.indexOfFirst { it.id == setId }
                if (setIndex != -1) {
                    val subsequentSets = exercise.sets.drop(setIndex + 1)
                    for (nextSet in subsequentSets) {
                        if (!nextSet.isCompleted) {
                            workoutDao.updateSetWeightAndReps(
                                setId = nextSet.id,
                                weight = safeWeight,
                                reps = safeReps
                            )
                        }
                    }
                    val updatedRec = computeRecommendationForExercise(
                        exercise = exercise,
                        overrideSetId = setId,
                        overrideWeight = safeWeight,
                        overrideReps = safeReps
                    )
                    _progressionRecommendations.update { current ->
                        current + (exercise.id to updatedRec)
                    }
                    // [AI-TASK:TASK-022] Do NOT auto-overwrite user manual weight edits with AI recommendations
                    break
                }
            }
        }
    }

    fun updateSetEffort(setId: Long, effort: PerceivedEffort) {
        viewModelScope.launch {
            workoutDao.updateSetEffort(setId, effort)
            val active = (uiState.value as? WorkoutUiState.Active)?.workout
            if (active != null) {
                val exercise = active.exercises.find { ex -> ex.sets.any { it.id == setId } }
                if (exercise != null) {
                    val updatedRec = computeRecommendationForExercise(
                        exercise = exercise,
                        overrideSetId = setId,
                        effort = effort
                    )
                    _progressionRecommendations.update { current ->
                        current + (exercise.id to updatedRec)
                    }
                    if (_isAutoProgressionEnabled.value) {
                        applyProgressionRecommendation(exercise.id, updatedRec)
                    }
                }
            }
        }
    }

    fun replaceExerciseInActiveWorkout(workoutExerciseId: Long, newExercise: Exercise) {
        viewModelScope.launch(Dispatchers.IO) {
            val activeWorkout = (uiState.value as? WorkoutUiState.Active)?.workout ?: return@launch
            val workoutExercise = activeWorkout.exercises.find { it.id == workoutExerciseId } ?: return@launch

            workoutDao.updateWorkoutExerciseExerciseId(workoutExerciseId, newExercise.id)

            val currentUserId = getCurrentUserId()
            val prevSets = workoutDao.getLatestCompletedSetsForExercise(newExercise.id, currentUserId)
            val profile = getEquipmentProfile(newExercise.equipment)
            val rec = SmartProgressionEngine.calculateGoalAwareLoad(
                previousSets = prevSets.map { it.toDomain() },
                equipmentProfile = profile
            )

            val defaultWeight = if (prevSets.isNotEmpty()) prevSets.first().weight else rec.suggestedWeight
            for (set in workoutExercise.sets) {
                if (!set.isCompleted) {
                    workoutDao.updateSetWeightAndReps(set.id, defaultWeight, set.reps)
                }
            }

            _progressionRecommendations.update { current ->
                current + (workoutExerciseId to rec)
            }
            if (_isAutoProgressionEnabled.value) {
                applyProgressionRecommendation(workoutExerciseId, rec)
            }
        }
    }

    fun removeExerciseFromActiveWorkout(workoutExerciseId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            workoutDao.removeWorkoutExercise(workoutExerciseId)
            _progressionRecommendations.update { current ->
                current - workoutExerciseId
            }
        }
    }

    fun addSetToExercise(workoutExerciseId: Long, currentSetsCount: Int) {
        viewModelScope.launch {
            val activeWorkout = (uiState.value as? WorkoutUiState.Active)?.workout
            val exercise = activeWorkout?.exercises?.find { it.id == workoutExerciseId }
            val lastSet = exercise?.sets?.lastOrNull()
            val defaultWeight = lastSet?.weight ?: 0.0
            val defaultReps = lastSet?.reps ?: 0

            workoutDao.insertWorkoutSet(
                WorkoutSetEntity(
                    workoutExerciseId = workoutExerciseId,
                    setNumber = currentSetsCount + 1,
                    setType = SetType.NORMAL,
                    weight = defaultWeight,
                    reps = defaultReps,
                    isCompleted = false
                )
            )
        }
    }

    fun removeSet(setId: Long) {
        viewModelScope.launch {
            workoutDao.deleteWorkoutSet(setId)
        }
    }

    fun addExerciseToActiveWorkout(exercise: Exercise) {
        viewModelScope.launch {
            val current = (uiState.value as? WorkoutUiState.Active)?.workout ?: return@launch
            val nextOrder = current.exercises.size
            val workoutExerciseId = workoutDao.insertWorkoutExercise(
                WorkoutExerciseEntity(
                    workoutId = current.id,
                    exerciseId = exercise.id,
                    orderIndex = nextOrder,
                    supersetId = null
                )
            )

            // Предзаполняем веса и повторы из предыдущей тренировки пользователя
            val prevSets = workoutDao.getLatestCompletedSetsForExercise(exercise.id, current.userId)
            val initialSets = (1..3).map { setNumber ->
                val prevForSet = prevSets.getOrNull(setNumber - 1) ?: prevSets.firstOrNull()
                WorkoutSetEntity(
                    workoutExerciseId = workoutExerciseId,
                    setNumber = setNumber,
                    setType = SetType.NORMAL,
                    weight = prevForSet?.weight ?: 0.0,
                    reps = prevForSet?.reps ?: 0,
                    isCompleted = false
                )
            }
            workoutDao.insertWorkoutSets(initialSets)
        }
    }

    fun startEmptyWorkout(title: String = "Свободная тренировка") {
        viewModelScope.launch {
            val currentUserId = getCurrentUserId()
            workoutDao.insertWorkout(
                WorkoutEntity(
                    userId = currentUserId,
                    templateId = null,
                    title = title,
                    startTime = System.currentTimeMillis(),
                    status = WorkoutStatus.IN_PROGRESS
                )
            )
        }
    }

    fun discardWorkout(workoutId: Long) {
        viewModelScope.launch {
            stopRestTimer()
            durationTimerJob?.cancel()
            _workoutDurationSeconds.value = 0L
            _progressionRecommendations.value = emptyMap()
            workoutDao.deleteWorkout(workoutId)
        }
    }

    fun deleteCompletedWorkout(workoutId: Long) {
        viewModelScope.launch {
            workoutDao.deleteWorkout(workoutId)
        }
    }

    fun repeatWorkout(sourceWorkout: Workout) {
        viewModelScope.launch {
            val currentUserId = getCurrentUserId()
            val newWorkoutId = workoutDao.insertWorkout(
                WorkoutEntity(
                    userId = currentUserId,
                    templateId = sourceWorkout.templateId,
                    title = sourceWorkout.title,
                    startTime = System.currentTimeMillis(),
                    status = WorkoutStatus.IN_PROGRESS
                )
            )
            sourceWorkout.exercises.forEachIndexed { index, exercise ->
                val workoutExerciseId = workoutDao.insertWorkoutExercise(
                    WorkoutExerciseEntity(
                        workoutId = newWorkoutId,
                        exerciseId = exercise.exercise.id,
                        orderIndex = index,
                        supersetId = exercise.supersetId
                    )
                )
                val sets = exercise.sets.mapIndexed { setIdx, prevSet ->
                    WorkoutSetEntity(
                        workoutExerciseId = workoutExerciseId,
                        setNumber = setIdx + 1,
                        setType = prevSet.setType,
                        weight = prevSet.weight,
                        reps = prevSet.reps,
                        isCompleted = false
                    )
                }
                workoutDao.insertWorkoutSets(sets)
            }
        }
    }

    suspend fun getWorkoutDetails(workoutId: Long): Workout? {
        return withContext(Dispatchers.IO) {
            workoutDao.getWorkoutById(workoutId)?.toDomain()
        }
    }

    fun finishWorkout(workoutId: Long, onFinished: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val currentWorkout = workoutDao.getWorkoutById(workoutId) ?: return@launch
            val now = System.currentTimeMillis()
            val duration = (now - currentWorkout.workout.startTime) / 1000

            workoutDao.updateWorkout(
                currentWorkout.workout.copy(
                    endTime = now,
                    durationSeconds = duration,
                    status = WorkoutStatus.COMPLETED
                )
            )
            stopRestTimer()
            _progressionRecommendations.value = emptyMap()
            onFinished(workoutId)
        }
    }
}
