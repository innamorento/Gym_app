package com.gym.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gym.core.database.entity.CoachAssignmentEntity
import com.gym.core.database.entity.SetVideoReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CoachAndReviewDao {

    @Query("SELECT * FROM set_video_reviews WHERE setId = :setId LIMIT 1")
    fun getVideoReviewForSet(setId: Long): Flow<SetVideoReviewEntity?>

    @Query("SELECT * FROM set_video_reviews WHERE feedbackComment IS NULL ORDER BY createdAt DESC")
    fun getPendingReviewsForCoach(): Flow<List<SetVideoReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun attachVideoToSet(review: SetVideoReviewEntity): Long

    @Update
    suspend fun submitCoachReview(review: SetVideoReviewEntity)

    @Query("SELECT * FROM coach_assignments WHERE athleteId = :athleteId ORDER BY assignedDate DESC")
    fun getAssignmentsForAthlete(athleteId: String): Flow<List<CoachAssignmentEntity>>

    @Query("SELECT * FROM coach_assignments WHERE coachId = :coachId ORDER BY assignedDate DESC")
    fun getAssignmentsByCoach(coachId: String): Flow<List<CoachAssignmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createAssignment(assignment: CoachAssignmentEntity): Long

    @Update
    suspend fun updateAssignment(assignment: CoachAssignmentEntity)
}
