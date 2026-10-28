package com.gym.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gym.core.database.converter.GymTypeConverters
import com.gym.core.database.dao.CoachAndReviewDao
import com.gym.core.database.dao.EquipmentSettingsDao
import com.gym.core.database.dao.ExerciseDao
import com.gym.core.database.dao.TemplateDao
import com.gym.core.database.dao.UserProfileDao
import com.gym.core.database.dao.WorkoutDao
import com.gym.core.database.entity.CoachAssignmentEntity
import com.gym.core.database.entity.EquipmentSettingsEntity
import com.gym.core.database.entity.ExerciseEntity
import com.gym.core.database.entity.SetVideoReviewEntity
import com.gym.core.database.entity.TemplateExerciseEntity
import com.gym.core.database.entity.UserProfileEntity
import com.gym.core.database.entity.WorkoutEntity
import com.gym.core.database.entity.WorkoutExerciseEntity
import com.gym.core.database.entity.WorkoutSetEntity
import com.gym.core.database.entity.WorkoutTemplateEntity

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutTemplateEntity::class,
        TemplateExerciseEntity::class,
        WorkoutEntity::class,
        WorkoutExerciseEntity::class,
        WorkoutSetEntity::class,
        EquipmentSettingsEntity::class,
        SetVideoReviewEntity::class,
        CoachAssignmentEntity::class,
        UserProfileEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(GymTypeConverters::class)
abstract class GymDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun templateDao(): TemplateDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun equipmentSettingsDao(): EquipmentSettingsDao
    abstract fun coachAndReviewDao(): CoachAndReviewDao
    abstract fun userProfileDao(): UserProfileDao
}
