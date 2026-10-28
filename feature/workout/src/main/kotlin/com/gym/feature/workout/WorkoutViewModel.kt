package com.gym.feature.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gym.core.database.dao.CoachAndReviewDao
import com.gym.core.database.dao.TemplateDao
import com.gym.core.database.dao.WorkoutDao
import com.gym.core.database.entity.SetVideoReviewEntity
import com.gym.core.database.entity.WorkoutEntity
import com.gym.core.database.entity.WorkoutExerciseEntity
import com.gym.core.database.entity.WorkoutSetEntity
import com.gym.core.database.mapper.toDomain
import com.gym.core.model.Exercise
import com.gym.core.model.PerceivedEffort
import com.gym.core.model.SetType
import com.gym.core.model.Workout
import com.gym.core.model.WorkoutStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

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
    private val context: android.content.Context? = null
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

    private val _workoutDurationSeconds = MutableStateFlow(0L)
    val workoutDurationSeconds: StateFlow<Long> = _workoutDurationSeconds.asStateFlow()

    private val _restTimerState = MutableStateFlow(RestTimerState())
    val restTimerState: StateFlow<RestTimerState> = _restTimerState.asStateFlow()

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
                    }
                    is WorkoutUiState.Idle -> {
                        durationTimerJob?.cancel()
                        _workoutDurationSeconds.value = 0L
                        stopRestTimer()
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

    fun startWorkoutFromTemplate(templateId: Long) {
        viewModelScope.launch {
            val templateWithExercises = templateDao.getTemplateWithExercises(templateId) ?: return@launch
            val template = templateWithExercises.toDomain()

            val workoutId = workoutDao.insertWorkout(
                WorkoutEntity(
                    userId = "default_user",
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
                val prevSets = workoutDao.getLatestCompletedSetsForExercise(templateExercise.exercise.id, "default_user")
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
                    break
                }
            }
        }
    }

    fun updateSetEffort(setId: Long, effort: PerceivedEffort) {
        viewModelScope.launch {
            workoutDao.updateSetEffort(setId, effort)
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

    fun finishWorkout(workoutId: Long) {
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
        }
    }
}
