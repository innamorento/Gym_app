package com.gym.core.model

import org.junit.Assert.assertEquals
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

        // Атлет сделал 3 подхода по 10 повторений с 10 кг при целевом диапазоне 8-10
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

        // Должен порекомендовать повышение до 12.0 кг (шаг 2 кг после 10 кг)
        assertEquals(ProgressionDecision.INCREASE_WEIGHT, result.decision)
        assertEquals(12.0, result.suggestedWeight, 0.01)
    }
}
