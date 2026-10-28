package com.gym.core.database.converter

import androidx.room.TypeConverter
import com.gym.core.model.Equipment
import com.gym.core.model.FitnessGoal
import com.gym.core.model.Gender
import com.gym.core.model.MuscleGroup
import com.gym.core.model.PerceivedEffort
import com.gym.core.model.SetType
import com.gym.core.model.TrainingExperience
import com.gym.core.model.WorkoutStatus

class GymTypeConverters {

    @TypeConverter
    fun fromGender(value: Gender): String = value.name

    @TypeConverter
    fun toGender(value: String): Gender = try { Gender.valueOf(value) } catch (e: Exception) { Gender.MALE }

    @TypeConverter
    fun fromExperience(value: TrainingExperience): String = value.name

    @TypeConverter
    fun toExperience(value: String): TrainingExperience = try { TrainingExperience.valueOf(value) } catch (e: Exception) { TrainingExperience.BEGINNER }

    @TypeConverter
    fun fromFitnessGoal(value: FitnessGoal): String = value.name

    @TypeConverter
    fun toFitnessGoal(value: String): FitnessGoal = try { FitnessGoal.valueOf(value) } catch (e: Exception) { FitnessGoal.HYPERTROPHY }

    @TypeConverter
    fun fromMuscleGroup(value: MuscleGroup): String = value.name

    @TypeConverter
    fun toMuscleGroup(value: String): MuscleGroup = MuscleGroup.valueOf(value)

    @TypeConverter
    fun fromEquipment(value: Equipment): String = value.name

    @TypeConverter
    fun toEquipment(value: String): Equipment = Equipment.valueOf(value)

    @TypeConverter
    fun fromSetType(value: SetType): String = value.name

    @TypeConverter
    fun toSetType(value: String): SetType = SetType.valueOf(value)

    @TypeConverter
    fun fromWorkoutStatus(value: WorkoutStatus): String = value.name

    @TypeConverter
    fun toWorkoutStatus(value: String): WorkoutStatus = WorkoutStatus.valueOf(value)

    @TypeConverter
    fun fromPerceivedEffort(value: PerceivedEffort?): String? = value?.name

    @TypeConverter
    fun toPerceivedEffort(value: String?): PerceivedEffort? = value?.let {
        try { PerceivedEffort.valueOf(it) } catch (e: Exception) { null }
    }
}
