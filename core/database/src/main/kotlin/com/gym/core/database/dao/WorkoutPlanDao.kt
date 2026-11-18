package com.gym.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.gym.core.database.entity.WorkoutPlanDayEntity
import com.gym.core.database.entity.WorkoutPlanEntity
import com.gym.core.database.entity.WorkoutPlanWithDays
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutPlanDao {
    @Query("SELECT * FROM workout_plans ORDER BY id ASC")
    fun getAllPlansFlow(): Flow<List<WorkoutPlanEntity>>

    @Transaction
    @Query("SELECT * FROM workout_plans WHERE isActive = 1 LIMIT 1")
    fun getActivePlanWithDaysFlow(): Flow<WorkoutPlanWithDays?>

    @Transaction
    @Query("SELECT * FROM workout_plans WHERE isActive = 1 LIMIT 1")
    suspend fun getActivePlanWithDays(): WorkoutPlanWithDays?

    @Transaction
    @Query("SELECT * FROM workout_plans ORDER BY id ASC")
    fun getAllPlansWithDaysFlow(): Flow<List<WorkoutPlanWithDays>>

    @Transaction
    @Query("SELECT * FROM workout_plans WHERE id = :planId LIMIT 1")
    suspend fun getPlanWithDays(planId: Long): WorkoutPlanWithDays?

    @Query("SELECT COUNT(*) FROM workout_plans")
    suspend fun getPlansCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: WorkoutPlanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanDays(days: List<WorkoutPlanDayEntity>)

    @Query("UPDATE workout_plans SET isActive = 0")
    suspend fun clearAllActivePlans()

    @Query("UPDATE workout_plans SET isActive = 1 WHERE id = :planId")
    suspend fun activatePlan(planId: Long)

    @Transaction
    suspend fun setActivePlan(planId: Long) {
        clearAllActivePlans()
        activatePlan(planId)
    }

    @Query("DELETE FROM workout_plans WHERE id = :planId")
    suspend fun deletePlan(planId: Long)

    @Query("DELETE FROM workout_plans")
    suspend fun deleteAllPlans()

    @Transaction
    suspend fun insertPlanWithDays(plan: WorkoutPlanEntity, days: List<WorkoutPlanDayEntity>): Long {
        val planId = insertPlan(plan)
        val daysWithPlanId = days.map { it.copy(planId = planId) }
        insertPlanDays(daysWithPlanId)
        return planId
    }
}
