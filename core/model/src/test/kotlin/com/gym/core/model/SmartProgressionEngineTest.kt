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

    @Test
    fun testInitialStartingWeightsDifferentiateByEquipment() {
        val dumbbellProfile = EquipmentWeightProfile.defaultFor(Equipment.DUMBBELL)
        val barbellProfile = EquipmentWeightProfile.defaultFor(Equipment.BARBELL)
        val cableProfile = EquipmentWeightProfile.defaultFor(Equipment.CABLE)

        val dumbbellRec = SmartProgressionEngine.calculateGoalAwareLoad(
            previousSets = emptyList(),
            equipmentProfile = dumbbellProfile,
            goal = FitnessGoal.HYPERTROPHY
        )
        // Гантели для гипертрофии должны стартовать с 8.0 кг, а не с 30 кг!
        assertEquals(8.0, dumbbellRec.suggestedWeight, 0.01)

        val barbellRec = SmartProgressionEngine.calculateGoalAwareLoad(
            previousSets = emptyList(),
            equipmentProfile = barbellProfile,
            goal = FitnessGoal.HYPERTROPHY
        )
        assertEquals(20.0, barbellRec.suggestedWeight, 0.01)

        val cableRec = SmartProgressionEngine.calculateGoalAwareLoad(
            previousSets = emptyList(),
            equipmentProfile = cableProfile,
            goal = FitnessGoal.HYPERTROPHY
        )
        assertEquals(12.5, cableRec.suggestedWeight, 0.01)
    }

    @Test
    fun testStrengthGoalHandlesOvershotReps() {
        val dumbbellProfile = EquipmentWeightProfile.defaultFor(Equipment.DUMBBELL)

        // Спортсмен сделал 12 повторений при силовой цели (цель 4-6)
        val sets = listOf(
            WorkoutSet(setNumber = 1, weight = 10.0, reps = 12, effort = PerceivedEffort.OPTIMAL, isCompleted = true)
        )

        val result = SmartProgressionEngine.calculateGoalAwareLoad(
            previousSets = sets,
            equipmentProfile = dumbbellProfile,
            goal = FitnessGoal.STRENGTH
        )

        assertEquals(ProgressionDecision.INCREASE_WEIGHT, result.decision)
        assertTrue(result.suggestedWeight > 10.0)
        assertTrue(result.rationaleRu.contains("12 повт."))
    }

    @Test
    fun testOneRepMaxCalculationAndTargetRecalibration() {
        // 40 кг на 12 повторений: 1ПМ = 40 * (1 + 12 / 30) = 56.0 кг
        val estimated1Rm = SmartProgressionEngine.estimate1Rm(40.0, 12)
        assertEquals(56.0, estimated1Rm, 0.01)

        // Целевой вес для 5 повторений: 56 / (1 + 5 / 30) = 48.0 кг
        val targetWeightFor5Reps = SmartProgressionEngine.estimateTargetWeightForReps(estimated1Rm, 5)
        assertEquals(48.0, targetWeightFor5Reps, 0.01)

        // Штанга с шагом 2.5 кг (гриф 20 кг + блины): 48.0 округляется до 47.5 кг
        val barbellProfile = EquipmentWeightProfile(equipment = Equipment.BARBELL, barWeight = 20.0, minPlatesPairWeight = 2.5)
        val sets = listOf(
            WorkoutSet(setNumber = 1, weight = 40.0, reps = 12, effort = PerceivedEffort.OPTIMAL, isCompleted = true)
        )

        val result = SmartProgressionEngine.calculateGoalAwareLoad(
            previousSets = sets,
            equipmentProfile = barbellProfile,
            goal = FitnessGoal.STRENGTH
        )

        assertEquals(ProgressionDecision.INCREASE_WEIGHT, result.decision)
        assertEquals(47.5, result.suggestedWeight, 0.01)
        assertEquals("4-6", result.suggestedReps)
        assertTrue(result.rationaleRu.contains("калибровка 1RM"))
    }

    // [AI-TASK:TASK-022] Test RPE live autoregulation on single set
    @Test
    fun testSingleSetHardEffortAutoregulationMaintainsWeight() {
        val barbellProfile = EquipmentWeightProfile(equipment = Equipment.BARBELL, barWeight = 20.0, minPlatesPairWeight = 2.5)
        val sets = listOf(
            WorkoutSet(setNumber = 1, weight = 0.0, reps = 4, effort = PerceivedEffort.HARD, isCompleted = true)
        )

        val result = SmartProgressionEngine.calculateGoalAwareLoad(
            previousSets = sets,
            equipmentProfile = barbellProfile,
            goal = FitnessGoal.STRENGTH
        )

        assertEquals(ProgressionDecision.MAINTAIN_WEIGHT, result.decision)
        assertTrue(result.rationaleRu.contains("Тяжело"))
    }

    @Test
    fun testSingleSetEasyEffortAutoregulationIncreasesWeight() {
        val barbellProfile = EquipmentWeightProfile(equipment = Equipment.BARBELL, barWeight = 20.0, minPlatesPairWeight = 2.5)
        val sets = listOf(
            WorkoutSet(setNumber = 1, weight = 50.0, reps = 6, effort = PerceivedEffort.EASY, isCompleted = true)
        )

        val result = SmartProgressionEngine.calculateGoalAwareLoad(
            previousSets = sets,
            equipmentProfile = barbellProfile,
            goal = FitnessGoal.STRENGTH
        )

        assertEquals(ProgressionDecision.INCREASE_WEIGHT, result.decision)
        assertEquals(52.5, result.suggestedWeight, 0.01)
        assertTrue(result.rationaleRu.contains("Легко"))
    }
}
