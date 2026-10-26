package com.gym.core.database.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.gym.core.database.entity.ExerciseEntity
import com.gym.core.database.entity.WorkoutEntity
import com.gym.core.database.entity.WorkoutExerciseEntity
import com.gym.core.database.entity.WorkoutSetEntity
import com.gym.core.model.WorkoutStatus
import kotlinx.coroutines.flow.Flow

data class WorkoutExerciseWithSets(
    @Embedded
    val workoutExercise: WorkoutExerciseEntity,
    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "id"
    )
    val exercise: ExerciseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "workoutExerciseId"
    )
    val sets: List<WorkoutSetEntity>
)

data class WorkoutWithDetails(
    @Embedded
    val workout: WorkoutEntity,
    @Relation(
        entity = WorkoutExerciseEntity::class,
        parentColumn = "id",
        entityColumn = "workoutId"
    )
    val exercises: List<WorkoutExerciseWithSets>
)

@Dao
interface WorkoutDao {

    @Transaction
    @Query("SELECT * FROM workouts WHERE status = 'IN_PROGRESS' LIMIT 1")
    fun getActiveWorkout(): Flow<WorkoutWithDetails?>

    @Transaction
    @Query("SELECT * FROM workouts WHERE status = 'COMPLETED' ORDER BY startTime DESC")
    fun getCompletedWorkouts(): Flow<List<WorkoutWithDetails>>

    @Transaction
    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    suspend fun getWorkoutById(workoutId: Long): WorkoutWithDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutExercise(workoutExercise: WorkoutExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutSets(sets: List<WorkoutSetEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutSet(set: WorkoutSetEntity): Long

    @Update
    suspend fun updateWorkoutSet(set: WorkoutSetEntity)

    @Query("DELETE FROM workout_sets WHERE id = :setId")
    suspend fun deleteWorkoutSet(setId: Long)

    @Query("DELETE FROM workouts WHERE id = :workoutId")
    suspend fun deleteWorkout(workoutId: Long)
}
