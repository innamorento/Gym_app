package com.gym.core.ui.icon

import androidx.annotation.DrawableRes
import com.gym.core.model.Equipment
import com.gym.core.model.FitnessGoal
import com.gym.core.model.MuscleCategory
import com.gym.core.model.MuscleGroup
import com.gym.core.ui.R

/**
 * Фирменный набор иконок в стилистике Tactical Iron Hex (The Iron Apex).
 * Строгие контурные линии, 45-градусные скосы и шестигранная геометрия зала.
 */
object GymIcons {
    // Заглавный Hero-баннер бренда
    @DrawableRes val BannerHero: Int = R.drawable.banner_iron_apex_hero
    // [AI-TASK:TASK-021] Brand App Icon
    @DrawableRes val GymLogo: Int = R.drawable.ic_gym_logo

    // Нижняя панель навигации (Bottom Navigation)
    @DrawableRes val NavWorkout: Int = R.drawable.ic_nav_workout
    @DrawableRes val NavHistory: Int = R.drawable.ic_nav_history
    @DrawableRes val NavCatalog: Int = R.drawable.ic_nav_catalog
    @DrawableRes val NavAnalytics: Int = R.drawable.ic_nav_analytics
    @DrawableRes val NavCoach: Int = R.drawable.ic_nav_coach

    // Ключевые действия тренировки (Workout Actions)
    @DrawableRes val Timer: Int = R.drawable.ic_action_timer
    @DrawableRes val Swap: Int = R.drawable.ic_action_swap
    @DrawableRes val PlateCalc: Int = R.drawable.ic_action_plate_calc
    @DrawableRes val Bolt: Int = R.drawable.ic_action_bolt
    @DrawableRes val CheckHex: Int = R.drawable.ic_action_check_hex
    @DrawableRes val Videocam: Int = R.drawable.ic_action_videocam
    // [AI-TASK:TASK-021] Tactical Iron Hex Action Badges
    @DrawableRes val SupersetLink: Int = R.drawable.ic_action_superset_link
    @DrawableRes val EquipmentTune: Int = R.drawable.ic_action_tune
    @DrawableRes val DatabaseBackup: Int = R.drawable.ic_action_backup
    @DrawableRes val ShareRecord: Int = R.drawable.ic_action_share
    // [AI-TASK:TASK-023] Tactical Athlete Profile Badge
    @DrawableRes val Profile: Int = R.drawable.ic_action_profile
    // [AI-TASK:TASK-022] Tactical AI Lightbulb & Equipment Badges
    @DrawableRes val AiBulb: Int = R.drawable.ic_action_ai_bulb
    @DrawableRes val EquipmentBarbell: Int = R.drawable.ic_equipment_barbell
    @DrawableRes val EquipmentDumbbell: Int = R.drawable.ic_equipment_dumbbell
    @DrawableRes val EquipmentMachine: Int = R.drawable.ic_equipment_machine
    @DrawableRes val EquipmentCable: Int = R.drawable.ic_equipment_cable
    @DrawableRes val EquipmentBodyweight: Int = R.drawable.ic_equipment_bodyweight
    @DrawableRes val EquipmentWeightedBodyweight: Int = R.drawable.ic_equipment_weighted_bodyweight
    @DrawableRes val EquipmentKettlebell: Int = R.drawable.ic_equipment_kettlebell
    @DrawableRes val EquipmentBand: Int = R.drawable.ic_equipment_band
    @DrawableRes val EquipmentOther: Int = R.drawable.ic_equipment_other

    // Зал Славы (PR Trophies)
    @DrawableRes val TrophyHex: Int = R.drawable.ic_trophy_hex

    // Главные тренировочные цели (Fitness Goals)
    @DrawableRes val GoalHypertrophy: Int = R.drawable.ic_goal_hypertrophy
    @DrawableRes val GoalStrength: Int = R.drawable.ic_goal_strength
    @DrawableRes val GoalFatLoss: Int = R.drawable.ic_goal_fat_loss
    @DrawableRes val GoalHealth: Int = R.drawable.ic_goal_health

    // Сплиты и комбинированные группы тренировок (Workout Splits)
    @DrawableRes val SplitBackBiceps: Int = R.drawable.ic_split_back_biceps
    @DrawableRes val SplitChestTriceps: Int = R.drawable.ic_split_chest_triceps
    @DrawableRes val SplitLegsShoulders: Int = R.drawable.ic_split_legs_shoulders
    @DrawableRes val SplitChestBack: Int = R.drawable.ic_split_chest_back
    @DrawableRes val SplitShouldersArms: Int = R.drawable.ic_split_shoulders_arms
    @DrawableRes val SplitUpperBody: Int = R.drawable.ic_split_upper_body
    @DrawableRes val SplitLowerBody: Int = R.drawable.ic_split_lower_body
    @DrawableRes val SplitFullBody: Int = R.drawable.ic_split_full_body

    // Анатомические группы мышц (Muscle Groups)
    @DrawableRes val MuscleChest: Int = R.drawable.ic_muscle_chest
    @DrawableRes val MuscleBack: Int = R.drawable.ic_muscle_back
    @DrawableRes val MuscleLegs: Int = R.drawable.ic_muscle_legs
    @DrawableRes val MuscleShoulders: Int = R.drawable.ic_muscle_shoulders
    @DrawableRes val MuscleBiceps: Int = R.drawable.ic_muscle_biceps
    @DrawableRes val MuscleTriceps: Int = R.drawable.ic_muscle_triceps
    @DrawableRes val MuscleCore: Int = R.drawable.ic_muscle_core
    @DrawableRes val MuscleOther: Int = R.drawable.ic_muscle_other

    /**
     * Возвращает иконку цели атлета.
     */
    @DrawableRes
    fun iconForGoal(goal: FitnessGoal): Int {
        return when (goal) {
            FitnessGoal.HYPERTROPHY -> GoalHypertrophy
            FitnessGoal.STRENGTH -> GoalStrength
            FitnessGoal.FAT_LOSS -> GoalFatLoss
            FitnessGoal.HEALTH_FITNESS -> GoalHealth
        }
    }

    /**
     * Возвращает иконку для названия сплита тренировки.
     */
    @DrawableRes
    fun iconForSplitTitle(title: String): Int {
        val lower = title.lowercase()
        return when {
            lower.contains("спина") && lower.contains("бицепс") -> SplitBackBiceps
            lower.contains("грудь") && lower.contains("трицепс") -> SplitChestTriceps
            lower.contains("ноги") && lower.contains("плечи") -> SplitLegsShoulders
            lower.contains("грудь") && lower.contains("спина") -> SplitChestBack
            lower.contains("плечи") && lower.contains("руки") -> SplitShouldersArms
            lower.contains("верх") || lower.contains("upper") -> SplitUpperBody
            lower.contains("низ") || lower.contains("lower") -> SplitLowerBody
            lower.contains("фулбоди") || lower.contains("full") -> SplitFullBody
            lower.contains("грудь") -> MuscleChest
            lower.contains("спина") -> MuscleBack
            lower.contains("плечи") -> MuscleShoulders
            lower.contains("бицепс") -> MuscleBiceps
            lower.contains("трицепс") -> MuscleTriceps
            lower.contains("пресс") || lower.contains("кор") -> MuscleCore
            else -> SplitFullBody
        }
    }

    /**
     * Возвращает фирменную векторную иконку Tactical Iron Hex для категории мышц.
     */
    @DrawableRes
    fun iconForCategory(category: MuscleCategory): Int {
        return when (category) {
            MuscleCategory.CHEST -> MuscleChest
            MuscleCategory.BACK -> MuscleBack
            MuscleCategory.LEGS -> MuscleLegs
            MuscleCategory.SHOULDERS -> MuscleShoulders
            MuscleCategory.BICEPS -> MuscleBiceps
            MuscleCategory.TRICEPS -> MuscleTriceps
            MuscleCategory.CORE -> MuscleCore
            MuscleCategory.OTHER -> MuscleOther
        }
    }

    /**
     * Возвращает иконку для детальной мышечной группы MuscleGroup.
     */
    @DrawableRes
    fun iconForMuscleGroup(group: MuscleGroup): Int {
        return iconForCategory(MuscleCategory.categoryFor(group))
    }

    // [AI-TASK:TASK-022] Tactical Iron Hex Equipment Icons
    @DrawableRes
    fun iconForEquipment(equipment: Equipment): Int {
        return when (equipment) {
            Equipment.BARBELL -> EquipmentBarbell
            Equipment.DUMBBELL -> EquipmentDumbbell
            Equipment.MACHINE -> EquipmentMachine
            Equipment.CABLE -> EquipmentCable
            Equipment.BODYWEIGHT -> EquipmentBodyweight
            Equipment.WEIGHTED_BODYWEIGHT -> EquipmentWeightedBodyweight
            Equipment.KETTLEBELL -> EquipmentKettlebell
            Equipment.RESISTANCE_BAND -> EquipmentBand
            Equipment.OTHER -> EquipmentOther
        }
    }
}
