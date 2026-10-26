package com.gym.core.model

import kotlin.math.round

/**
 * Профиль настройки шага весов для тренажера или снаряда.
 * Учитывает реальную дискретность оборудования в зале.
 */
data class EquipmentWeightProfile(
    val equipment: Equipment,
    val smallWeightThreshold: Double = 10.0,
    val smallWeightStep: Double = 1.0,      // Напр. для гантелей <= 10 кг шаг 1 кг
    val defaultStep: Double = 2.0,           // Напр. для гантелей > 10 кг шаг 2 кг
    val barWeight: Double = 20.0,            // Вес грифа для штанги
    val minPlatesPairWeight: Double = 2.5    // Минимальный шаг блинов на штанге (2 x 1.25 кг = 2.5 кг)
) {

    /**
     * Округляет теоретический вес до физически достижимого на данном оборудовании.
     */
    fun snapWeight(targetWeight: Double): Double {
        if (targetWeight <= 0.0) return 0.0

        return when (equipment) {
            Equipment.DUMBBELL -> {
                if (targetWeight <= smallWeightThreshold) {
                    val step = if (smallWeightStep > 0) smallWeightStep else 1.0
                    (round(targetWeight / step) * step)
                } else {
                    val step = if (defaultStep > 0) defaultStep else 2.0
                    // Округляем относительно порога
                    val excess = targetWeight - smallWeightThreshold
                    val snappedExcess = round(excess / step) * step
                    smallWeightThreshold + snappedExcess
                }
            }

            Equipment.CABLE, Equipment.MACHINE -> {
                val step = if (defaultStep > 0) defaultStep else 2.5
                round(targetWeight / step) * step
            }

            Equipment.BARBELL -> {
                if (targetWeight <= barWeight) {
                    barWeight
                } else {
                    val platesWeight = targetWeight - barWeight
                    val step = if (minPlatesPairWeight > 0) minPlatesPairWeight else 2.5
                    val snappedPlates = round(platesWeight / step) * step
                    barWeight + snappedPlates
                }
            }

            else -> {
                // По умолчанию шаг 1 кг или 0.5 кг
                round(targetWeight * 2.0) / 2.0
            }
        }
    }

    companion object {
        fun defaultFor(equipment: Equipment): EquipmentWeightProfile {
            return when (equipment) {
                Equipment.DUMBBELL -> EquipmentWeightProfile(
                    equipment = equipment,
                    smallWeightThreshold = 10.0,
                    smallWeightStep = 1.0,
                    defaultStep = 2.0
                )
                Equipment.CABLE -> EquipmentWeightProfile(
                    equipment = equipment,
                    defaultStep = 2.5
                )
                Equipment.MACHINE -> EquipmentWeightProfile(
                    equipment = equipment,
                    defaultStep = 5.0
                )
                Equipment.BARBELL -> EquipmentWeightProfile(
                    equipment = equipment,
                    barWeight = 20.0,
                    minPlatesPairWeight = 2.5
                )
                else -> EquipmentWeightProfile(
                    equipment = equipment,
                    defaultStep = 1.0
                )
            }
        }
    }
}
