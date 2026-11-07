package com.gym.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartProgressionEngineTest {

    @Test
    fun testDumbbellSnappingBelowAndAboveThreshold() {
        val profile = EquipmentWeightProfile.defaultFor(Equipment.DUMBBELL)

        // До 10 кг - шаг 1 кг
        assertEquals(7.0, profile.snapWeight(7.1), 0.01)
        assertEquals(8.0, profile.snapWeight(7.8), 0.01)
        assertEquals(10.0, profile.snapWeight(9.6), 0.01)

        // Выше 10 кг - шаг 2 кг (12, 14, 16...)
        assertEquals(12.0, profile.snapWeight(11.2), 0.01)
        assertEquals(14.0, profile.snapWeight(13.8), 0.01)
        assertEquals(16.0, profile.snapWeight(15.1), 0.01)
    }

    @Test
    fun testCableSnapping2_5KgSteps() {
        val profile = EquipmentWeightProfile(
            equipment = Equipment.CABLE,
            defaultStep = 2.5
        )

        assertEquals(22.5, profile.snapWeight(22.3), 0.01)
        assertEquals(25.0, profile.snapWeight(24.8), 0.01)
        assertEquals(27.5, profile.snapWeight(26.6), 0.01)
    }

    @Test
    fun testBarbellSnappingWithBarAndPlates() {
        val profile = EquipmentWeightProfile(
            equipment = Equipment.BARBELL,
            barWeight = 20.0,
            minPlatesPairWeight = 2.5 // 2 x 1.25 кг
        )

        assertEquals(20.0, profile.snapWeight(18.0), 0.01)
        assertEquals(22.5, profile.snapWeight(22.0), 0.01)
        assertEquals(50.0, profile.snapWeight(49.2), 0.01)
        assertEquals(52.5, profile.snapWeight(51.8), 0.01)
    }

    @Test
    fun testSuccessfulProgressionIncreasesWeightToNextAvailableStep() {
        val dumbbellProfile = EquipmentWeightProfile.defaultFor(Equipment.DUMBBELL)

        val sets = listOf(
            WorkoutSet(setNumber = 1, weight = 10.0, reps = 10, isCompleted = true),
            WorkoutSet(setNumber = 2, weight = 10.0, reps = 10, isCompleted = true),
            WorkoutSet(setNumber = 3, weight = 10.0, reps = 10, isCompleted = true)
        )

        val result = SmartProgressionEngine.calculateNextLoad(
            previousSets = sets,
            targetMinReps = 8,
            targetMaxReps = 10,
            equipmentProfile = dumbbellProfile
        )

        assertEquals(ProgressionDecision.INCREASE_WEIGHT, result.decision)
        assertEquals(12.0, result.suggestedWeight, 0.01)
    }

    @Test
    fun testHitMaxRepsWithFailureEffortMaintainsWeightForSafety() {
        val dumbbellProfile = EquipmentWeightProfile.defaultFor(Equipment.DUMBBELL)

        val sets = listOf(
            WorkoutSet(setNumber = 1, weight = 10.0, reps = 10, effort = PerceivedEffort.OPTIMAL, isCompleted = true),
            WorkoutSet(setNumber = 2, weight = 10.0, reps = 10, effort = PerceivedEffort.HARD, isCompleted = true),
            WorkoutSet(setNumber = 3, weight = 10.0, reps = 10, effort = PerceivedEffort.FAILURE, isCompleted = true)
        )

        val result = SmartProgressionEngine.calculateNextLoad(
            previousSets = sets,
            targetMinReps = 8,
            targetMaxReps = 10,
            equipmentProfile = dumbbellProfile
        )

        assertEquals(ProgressionDecision.MAINTAIN_WEIGHT, result.decision)
        assertEquals(10.0, result.suggestedWeight, 0.01)
    }

    @Test
    fun testHypertrophyGoalIncreasesWeightWhen12RepsReached() {
        val barbellProfile = EquipmentWeightProfile(equipment = Equipment.BARBELL, barWeight = 20.0, minPlatesPairWeight = 2.5)

        val sets = listOf(
            WorkoutSet(setNumber = 1, weight = 70.0, reps = 12, effort = PerceivedEffort.OPTIMAL, isCompleted = true),
            WorkoutSet(setNumber = 2, weight = 70.0, reps = 12, effort = PerceivedEffort.OPTIMAL, isCompleted = true),
            WorkoutSet(setNumber = 3, weight = 70.0, reps = 12, effort = PerceivedEffort.OPTIMAL, isCompleted = true)
        )

        val result = SmartProgressionEngine.calculateGoalAwareLoad(
            previousSets = sets,
            equipmentProfile = barbellProfile,
            goal = FitnessGoal.HYPERTROPHY
        )

        assertEquals(ProgressionDecision.INCREASE_WEIGHT, result.decision)
        assertEquals(72.5, result.suggestedWeight, 0.01)
        assertEquals("8", result.suggestedReps) // Возврат на нижнюю планку 8 повторений
        assertTrue(result.rationaleRu.contains("Набор массы"))
    }

    @Test
    fun testStrengthGoalIncreasesWeightWhen4to6RepsHitWithReserve() {
        val barbellProfile = EquipmentWeightProfile(equipment = Equipment.BARBELL, barWeight = 20.0, minPlatesPairWeight = 2.5)

        val sets = listOf(
            WorkoutSet(setNumber = 1, weight = 100.0, reps = 6, effort = PerceivedEffort.OPTIMAL, isCompleted = true),
            WorkoutSet(setNumber = 2, weight = 100.0, reps = 6, effort = PerceivedEffort.OPTIMAL, isCompleted = true),
            WorkoutSet(setNumber = 3, weight = 100.0, reps = 6, effort = PerceivedEffort.OPTIMAL, isCompleted = true)
        )

        val result = SmartProgressionEngine.calculateGoalAwareLoad(
            previousSets = sets,
            equipmentProfile = barbellProfile,
            goal = FitnessGoal.STRENGTH
        )

        assertEquals(ProgressionDecision.INCREASE_WEIGHT, result.decision)
        assertEquals(102.5, result.suggestedWeight, 0.01)
        assertTrue(result.rationaleRu.contains("Развитие силы"))
    }

    @Test
    fun testStrengthGoalMaintainsWeightOnFailureEffort() {
        val barbellProfile = EquipmentWeightProfile(equipment = Equipment.BARBELL, barWeight = 20.0, minPlatesPairWeight = 2.5)

        val sets = listOf(
            WorkoutSet(setNumber = 1, weight = 100.0, reps = 6, effort = PerceivedEffort.OPTIMAL, isCompleted = true),
            WorkoutSet(setNumber = 2, weight = 100.0, reps = 6, effort = PerceivedEffort.HARD, isCompleted = true),
            WorkoutSet(setNumber = 3, weight = 100.0, reps = 6, effort = PerceivedEffort.FAILURE, isCompleted = true)
        )

        val result = SmartProgressionEngine.calculateGoalAwareLoad(
            previousSets = sets,
            equipmentProfile = barbellProfile,
            goal = FitnessGoal.STRENGTH
        )

        // Защита ЦНС: при отказе вес не повышаем!
        assertEquals(ProgressionDecision.MAINTAIN_WEIGHT, result.decision)
        assertEquals(100.0, result.suggestedWeight, 0.01)
    }

    @Test
    fun testFatLossGoalMaintainsWeightUnlessEffortIsEasy() {
        val dumbbellProfile = EquipmentWeightProfile.defaultFor(Equipment.DUMBBELL)

        val sets = listOf(
            WorkoutSet(setNumber = 1, weight = 16.0, reps = 15, effort = PerceivedEffort.OPTIMAL, isCompleted = true),
            WorkoutSet(setNumber = 2, weight = 16.0, reps = 15, effort = PerceivedEffort.OPTIMAL, isCompleted = true)
        )

        val result = SmartProgressionEngine.calculateGoalAwareLoad(
            previousSets = sets,
            equipmentProfile = dumbbellProfile,
            goal = FitnessGoal.FAT_LOSS
        )

        // На сушке при OPTIMAL держим вес для сохранения мышц
        assertEquals(ProgressionDecision.MAINTAIN_WEIGHT, result.decision)
        assertEquals(16.0, result.suggestedWeight, 0.01)
    }
}
