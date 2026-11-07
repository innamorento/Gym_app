package com.gym.feature.workout.ui

import android.net.Uri
import android.view.ViewGroup
import androidx.annotation.OptIn
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.gym.core.database.entity.SetVideoReviewEntity
import com.gym.core.ui.theme.GymAccentAmber
import com.gym.core.ui.theme.GymAccentBlue
import com.gym.core.ui.theme.GymAccentGreen
import com.gym.core.ui.theme.GymDarkBackground
import com.gym.core.ui.theme.GymDarkSurface
import com.gym.core.ui.theme.GymDarkSurfaceVariant
import com.gym.core.ui.theme.GymTextPrimary
import com.gym.core.ui.theme.GymTextSecondary

@OptIn(UnstableApi::class)
@kotlin.OptIn(ExperimentalMaterial3Api::class)
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
                    Column {
                        Text(
                            text = "Кабинет тренера: видеоотчеты",
                            fontWeight = FontWeight.Bold,
                            color = GymTextPrimary,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Аудит техники выполнения подходов",
                            fontSize = 11.sp,
                            color = GymTextSecondary
                        )
                    }
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
            val review = activeReview!!
            val context = LocalContext.current
            var isPlaying by remember { mutableStateOf(false) }
            var playbackError by remember { mutableStateOf<String?>(null) }

            // Инициализация ExoPlayer для воспроизведения видео подхода
            val exoPlayer = remember(review.id) {
                ExoPlayer.Builder(context).build().apply {
                    try {
                        val uri = Uri.parse(review.localVideoUri)
                        setMediaItem(MediaItem.fromUri(uri))
                        prepare()
                        playWhenReady = true
                    } catch (e: Exception) {
                        playbackError = e.localizedMessage
                    }

                    addListener(object : Player.Listener {
                        override fun onIsPlayingChanged(playing: Boolean) {
                            isPlaying = playing
                        }

                        override fun onPlayerError(error: PlaybackException) {
                            playbackError = "Видеофайл недоступен или поврежден (${error.errorCodeName})"
                        }
                    })
                }
            }

            DisposableEffect(review.id) {
                onDispose {
                    exoPlayer.release()
                }
            }

            // Форма оценки выбранного видео со встроенным плеером Media3
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Видеопроигрыватель ExoPlayer
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (playbackError == null) {
                            AndroidView(
                                factory = { ctx ->
                                    PlayerView(ctx).apply {
                                        player = exoPlayer
                                        useController = true
                                        layoutParams = ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT
                                        )
                                    }
                                },
                                update = { playerView ->
                                    playerView.player = exoPlayer
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // Фоллбек при отсутствии реального медиафайла на эмуляторе
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Videocam,
                                    contentDescription = null,
                                    tint = GymAccentBlue,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Видеозапись подхода #${review.setId}",
                                    fontWeight = FontWeight.Bold,
                                    color = GymTextPrimary,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = review.localVideoUri.substringAfterLast("/"),
                                    color = GymTextSecondary,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = {
                                            if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                                        },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(GymDarkSurfaceVariant, RoundedCornerShape(8.dp))
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = "Воспроизведение",
                                            tint = GymAccentGreen
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            exoPlayer.seekTo(0)
                                            exoPlayer.play()
                                        },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(GymDarkSurfaceVariant, RoundedCornerShape(8.dp))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Replay,
                                            contentDescription = "Сначала",
                                            tint = GymAccentBlue
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Оценка техники (1-5 звезд)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Оценка техники выполнения:",
                        color = GymTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        (1..5).forEach { star ->
                            IconButton(
                                onClick = { rating = star },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "$star звезд",
                                    tint = if (star <= rating) GymAccentAmber else GymTextSecondary,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (rating) {
                                5 -> "Идеально (5/5)"
                                4 -> "Хорошо (4/5)"
                                3 -> "Удовлетворительно (3/5)"
                                2 -> "Много ошибок (2/5)"
                                else -> "Опасно для суставов (1/5)"
                            },
                            color = GymAccentAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Текстовый комментарий тренера
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Замечания и рекомендации тренера:",
                        color = GymTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    OutlinedTextField(
                        value = feedbackText,
                        onValueChange = { feedbackText = it },
                        placeholder = {
                            Text(
                                text = "Например: локти держи ближе к корпусу, глубина седа отличная, держи темп 2-0-1",
                                color = GymTextSecondary,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GymTextPrimary,
                            unfocusedTextColor = GymTextPrimary,
                            focusedBorderColor = GymAccentGreen,
                            unfocusedBorderColor = GymDarkSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Быстрые шаблоны комментариев тренера
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Отличная техника! 💪", "Держи локти ближе", "Не округляй поясницу").forEach { template ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GymDarkSurfaceVariant)
                                .clickable { feedbackText = template }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(text = template, fontSize = 11.sp, color = GymTextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Кнопка отправки замечания
                Button(
                    onClick = {
                        onSubmitFeedback(
                            review.id,
                            review.setId,
                            review.localVideoUri,
                            feedbackText,
                            rating
                        )
                        activeReview = null
                        feedbackText = ""
                        rating = 5
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GymAccentGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = GymDarkBackground)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Сохранить и отправить отзыв",
                        color = GymDarkBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        } else if (reviews.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        tint = GymTextSecondary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Все видеоотчеты проверены!",
                        color = GymTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Новые видео подходов от атлетов появятся здесь автоматически.",
                        color = GymTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
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
                        text = if (review.feedbackComment == null) "Ожидает проверки тренером" else "Проверено: ${review.coachRating}/5 ⭐",
                        color = if (review.feedbackComment == null) GymAccentAmber else GymAccentGreen,
                        fontSize = 12.sp
                    )
                }
            }

            Text("Проверить >", color = GymAccentGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
