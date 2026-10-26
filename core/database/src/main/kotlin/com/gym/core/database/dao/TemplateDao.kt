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
import com.gym.core.database.entity.TemplateExerciseEntity
import com.gym.core.database.entity.WorkoutTemplateEntity
import kotlinx.coroutines.flow.Flow

data class TemplateExerciseWithDetails(
    @Embedded
    val templateExercise: TemplateExerciseEntity,
    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "id"
    )
    val exercise: ExerciseEntity
)

data class WorkoutTemplateWithExercises(
    @Embedded
    val template: WorkoutTemplateEntity,
    @Relation(
        entity = TemplateExerciseEntity::class,
        parentColumn = "id",
        entityColumn = "templateId"
    )
    val exercises: List<TemplateExerciseWithDetails>
)

@Dao
interface TemplateDao {

    @Transaction
    @Query("SELECT * FROM workout_templates ORDER BY lastUsedAt DESC, createdAt DESC")
    fun getAllTemplatesWithExercises(): Flow<List<WorkoutTemplateWithExercises>>

    @Transaction
    @Query("SELECT * FROM workout_templates WHERE id = :templateId")
    suspend fun getTemplateWithExercises(templateId: Long): WorkoutTemplateWithExercises?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: WorkoutTemplateEntity): Long

    @Update
    suspend fun updateTemplate(template: WorkoutTemplateEntity)

    @Query("DELETE FROM workout_templates WHERE id = :id")
    suspend fun deleteTemplate(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplateExercises(exercises: List<TemplateExerciseEntity>)

    @Query("DELETE FROM template_exercises WHERE templateId = :templateId")
    suspend fun deleteTemplateExercises(templateId: Long)

    @Transaction
    suspend fun saveTemplateWithExercises(
        template: WorkoutTemplateEntity,
        exercises: List<TemplateExerciseEntity>
    ): Long {
        val templateId = if (template.id == 0L) {
            insertTemplate(template)
        } else {
            updateTemplate(template)
            deleteTemplateExercises(template.id)
            template.id
        }

        val updatedExercises = exercises.mapIndexed { index, item ->
            item.copy(templateId = templateId, orderIndex = index)
        }
        insertTemplateExercises(updatedExercises)
        return templateId
    }
}
