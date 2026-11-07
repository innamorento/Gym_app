package com.gym.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyticsCalculatorTest {

    private val benchPress = Exercise(
        id = 1L,
        name = "Жим штанги лежа",
        primaryMuscleGroup = MuscleGroup.CHEST,
        equipment = Equipment.BARBELL
    )

    private val squat = Exercise(
        id = 2L,
        name = "Приседания со штангой",
        primaryMuscleGroup = MuscleGroup.LEGS_QUADS,
        equipment = Equipment.BARBELL
    )

    private val pullUp = Exercise(
        id = 3L,
        name = "Подтягивания",
        primaryMuscleGroup = MuscleGroup.BACK,
        equipment = Equipment.BODYWEIGHT
    )

    @Test
    fun `calculate1RmTrend detects all-time PR milestones chronologically`() {
        val now = 1700000000000L
        val workout1 = Workout(
            id = 1L,
            title = "Session 1",
            startTime = now,
            endTime = now + 3600000,
            status = WorkoutStatus.COMPLETED,
            exercises = listOf(
                WorkoutExercise(
                    id = 1L,
                    exercise = benchPress,
                    orderIndex = 0,
                    sets = listOf(
                        WorkoutSet(id = 1L, setNumber = 1, weight = 80.0, reps = 10, isCompleted = true) // 1RM ~ 106.7
                    )
                )
            )
        )

        val workout2 = Workout(
            id = 2L,
            title = "Session 2",
            startTime = now + 86400000,
            endTime = now + 86400000 + 3600000,
            status = WorkoutStatus.COMPLETED,
            exercises = listOf(
                WorkoutExercise(
                    id = 2L,
                    exercise = benchPress,
                    orderIndex = 0,
                    sets = listOf(
                        WorkoutSet(id = 2L, setNumber = 1, weight = 82.5, reps = 8, isCompleted = true) // 1RM ~ 104.5 -> not a PR
                    )
                )
            )
        )

        val workout3 = Workout(
            id = 3L,
            title = "Session 3",
            startTime = now + 172800000,
            endTime = now + 172800000 + 3600000,
            status = WorkoutStatus.COMPLETED,
            exercises = listOf(
                WorkoutExercise(
                    id = 3L,
                    exercise = benchPress,
                    orderIndex = 0,
                    sets = listOf(
                        WorkoutSet(id = 3L, setNumber = 1, weight = 90.0, reps = 8, isCompleted = true) // 1RM ~ 114.0 -> NEW PR!
                    )
                )
            )
        )

        val trend = AnalyticsCalculator.calculate1RmTrend(1L, listOf(workout1, workout2, workout3))

        assertEquals(3, trend.size)
        assertTrue("First workout is initial PR", trend[0].isPersonalRecord)
        assertEquals(106.7, trend[0].estimated1Rm, 0.5)

        assertFalse("Second workout is below peak, not PR", trend[1].isPersonalRecord)

        assertTrue("Third workout surpasses previous best, is PR", trend[2].isPersonalRecord)
        assertEquals(114.0, trend[2].estimated1Rm, 0.5)
    }

    @Test
    fun `calculateMuscleBalance detects Push and Legs volume correctly`() {
        val now = 1700000000000L
        val workout = Workout(
            id = 1L,
            title = "Legs and Push",
            startTime = now,
            endTime = now + 3600000,
            status = WorkoutStatus.COMPLETED,
            exercises = listOf(
                WorkoutExercise(
                    id = 1L,
                    exercise = benchPress, // CHEST -> Push
                    orderIndex = 0,
                    sets = listOf(
                        WorkoutSet(id = 1L, setNumber = 1, weight = 100.0, reps = 10, isCompleted = true) // 1000 kg
                    )
                ),
                WorkoutExercise(
                    id = 2L,
                    exercise = squat, // LEGS_QUADS -> Legs
                    orderIndex = 1,
                    sets = listOf(
                        WorkoutSet(id = 2L, setNumber = 1, weight = 150.0, reps = 10, isCompleted = true) // 1500 kg
                    )
                )
            )
        )

        val balance = AnalyticsCalculator.calculateMuscleBalance(listOf(workout))

        assertEquals(1000.0, balance.pushVolumeKg, 0.1)
        assertEquals(1500.0, balance.legsVolumeKg, 0.1)
        assertEquals(0.0, balance.pullVolumeKg, 0.1)
        assertEquals(40.0f, balance.pushPercentage, 0.5f)
        assertEquals(60.0f, balance.legsPercentage, 0.5f)
    }

    @Test
    fun `findCompoundTrophies discovers personal records for compound lifts`() {
        val now = 1700000000000L
        val workout = Workout(
            id = 1L,
            title = "Compounds",
            startTime = now,
            endTime = now + 3600000,
            status = WorkoutStatus.COMPLETED,
            exercises = listOf(
                WorkoutExercise(
                    id = 1L,
                    exercise = benchPress,
                    orderIndex = 0,
                    sets = listOf(
                        WorkoutSet(id = 1L, setNumber = 1, weight = 100.0, reps = 5, isCompleted = true),
                        WorkoutSet(id = 2L, setNumber = 2, weight = 90.0, reps = 10, isCompleted = true)
                    )
                ),
                WorkoutExercise(
                    id = 2L,
                    exercise = pullUp,
                    orderIndex = 1,
                    sets = listOf(
                        WorkoutSet(id = 3L, setNumber = 1, weight = 20.0, reps = 5, isCompleted = true)
                    )
                )
            )
        )

        val trophies = AnalyticsCalculator.findCompoundTrophies(listOf(workout))

        val benchTrophy = trophies.find { it.exerciseName == "Жим лежа" }
        assertNotNull(benchTrophy)
        assertEquals(100.0, benchTrophy!!.maxWeight, 0.1)
        assertEquals(100.0, benchTrophy.max5RepsWeight ?: 0.0, 0.1)
        assertEquals(90.0, benchTrophy.max10RepsWeight ?: 0.0, 0.1)

        val pullUpTrophy = trophies.find { it.exerciseName == "Подтягивания" }
        assertNotNull(pullUpTrophy)
        assertEquals(20.0, pullUpTrophy!!.maxWeight, 0.1)
    }
}
