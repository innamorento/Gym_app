package com.gym.core.model

data class SetVideoReview(
    val id: Long = 0,
    val setId: Long,
    val localVideoUri: String,
    val thumbnailUri: String? = null,
    val durationSeconds: Int = 0,
    val feedbackComment: String? = null,
    val coachRating: Int? = null, // Оценка техники 1..5
    val coachName: String? = null,
    val reviewedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isReviewed: Boolean
        get() = !feedbackComment.isNullOrBlank() || coachRating != null
}

data class CoachAssignment(
    val id: Long = 0,
    val coachId: String,
    val coachName: String,
    val athleteId: String,
    val athleteName: String,
    val templateId: Long,
    val targetRpe: Float? = null,
    val coachNotes: String? = null,
    val assignedDate: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
)
