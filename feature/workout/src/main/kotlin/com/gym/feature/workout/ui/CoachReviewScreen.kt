package com.gym.feature.workout.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.core.database.entity.SetVideoReviewEntity
import com.gym.core.ui.theme.GymAccentAmber
import com.gym.core.ui.theme.GymAccentBlue
import com.gym.core.ui.theme.GymAccentGreen
import com.gym.core.ui.theme.GymDarkBackground
import com.gym.core.ui.theme.GymDarkSurface
import com.gym.core.ui.theme.GymDarkSurfaceVariant
import com.gym.core.ui.theme.GymTextPrimary
import com.gym.core.ui.theme.GymTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachReviewScreen(
    reviews: List<SetVideoReviewEntity>,
    onBackClick: () -> Unit,
    onSubmitFeedback: (reviewId: Long, setId: Long, uri: String, comment: String, rating: Int) -> Unit
) {
    var activeReview by remember { mutableStateOf<SetVideoReviewEntity?>(null) }
    var feedbackText by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(5) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Видеоотчеты подопечных",
                        fontWeight = FontWeight.Bold,
                        color = GymTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = GymTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GymDarkBackground)
            )
        },
        containerColor = GymDarkBackground
    ) { paddingValues ->
        if (activeReview != null) {
            // Форма оценки выбранного видео
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.PlayCircleOutline,
                                contentDescription = "Воспроизвести",
                                tint = GymAccentGreen,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Видео подхода (до 60 сек)", color = GymTextPrimary)
                            Text(activeReview!!.localVideoUri.substringAfterLast("/"), color = GymTextSecondary, fontSize = 11.sp)
                        }
                    }
                }

                Text("Оценка техники выполнения:", color = GymTextPrimary, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..5).forEach { star ->
                        IconButton(onClick = { rating = star }) {
                            Icon(
                                imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$star звезд",
                                tint = GymAccentAmber,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    label = { Text("Замечания и рекомендации тренера") },
                    placeholder = { Text("Например: локти держи ближе к корпусу, темп отличный") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GymTextPrimary,
                        unfocusedTextColor = GymTextPrimary,
                        focusedBorderColor = GymAccentGreen,
                        unfocusedBorderColor = GymDarkSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = {
                        onSubmitFeedback(
                            activeReview!!.id,
                            activeReview!!.setId,
                            activeReview!!.localVideoUri,
                            feedbackText,
                            rating
                        )
                        activeReview = null
                        feedbackText = ""
                        rating = 5
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GymAccentGreen)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = GymDarkBackground)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Отправить замечание атлету", color = GymDarkBackground, fontWeight = FontWeight.Bold)
                }
            }
        } else if (reviews.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Все видеоотчеты проверены! Новых видео нет.", color = GymTextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reviews, key = { it.id }) { review ->
                    ReviewListItemCard(
                        review = review,
                        onClick = {
                            activeReview = review
                            feedbackText = review.feedbackComment.orEmpty()
                            rating = review.coachRating ?: 5
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewListItemCard(
    review: SetVideoReviewEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(GymAccentBlue.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = null, tint = GymAccentBlue)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Подход #${review.setId}",
                        fontWeight = FontWeight.Bold,
                        color = GymTextPrimary,
                        fontSize = 15.sp
                    )
                    Text(
                        text = if (review.feedbackComment == null) "Ожидает проверки тренером" else "Проверено: ${review.coachRating}/5",
                        color = if (review.feedbackComment == null) GymAccentAmber else GymAccentGreen,
                        fontSize = 12.sp
                    )
                }
            }

            Text("Проверить >", color = GymAccentGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
