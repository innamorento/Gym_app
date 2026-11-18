package com.gym.feature.workout.ui

import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.core.ui.icon.GymIcons
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.gym.core.database.dao.CoachClientDao
import com.gym.core.database.dao.UserProfileDao
import com.gym.core.database.entity.CoachClientEntity
import com.gym.core.database.entity.SetVideoReviewEntity
import com.gym.core.database.mapper.toDomain
import com.gym.core.database.mapper.toEntity
import com.gym.core.model.UserRole
import com.gym.core.ui.theme.GymAccentAmber
import com.gym.core.ui.theme.GymAccentBlue
import com.gym.core.ui.theme.GymAccentGreen
import com.gym.core.ui.theme.GymAccentRed
import com.gym.core.ui.theme.GymDarkBackground
import com.gym.core.ui.theme.GymDarkSurface
import com.gym.core.ui.theme.GymDarkSurfaceVariant
import com.gym.core.ui.theme.GymSurfaceBorder
import com.gym.core.ui.theme.GymTextPrimary
import com.gym.core.ui.theme.GymTextSecondary
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.util.UUID

@OptIn(UnstableApi::class)
@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachReviewScreen(
    reviews: List<SetVideoReviewEntity>,
    onBackClick: () -> Unit,
    onSubmitFeedback: (reviewId: Long, setId: Long, uri: String, comment: String, rating: Int) -> Unit
) {
    val coachClientDao: CoachClientDao = koinInject()
    val userProfileDao: UserProfileDao = koinInject()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val profileEntity by userProfileDao.getUserProfile().collectAsState(initial = null)
    val userProfile = profileEntity?.toDomain()
    val isCoach = userProfile?.role == UserRole.COACH

    val clients by coachClientDao.getClientsFlow().collectAsState(initial = emptyList())

    // Подвкладки тренера: 0 - Подопечные (CRM), 1 - Видеоотчеты
    var coachSelectedTab by remember { mutableIntStateOf(0) }

    // Состояние для активного видеоотчета
    var activeReview by remember { mutableStateOf<SetVideoReviewEntity?>(null) }
    var feedbackText by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(5) }

    // Состояние диалога добавления подопечного
    var showAddClientDialog by remember { mutableStateOf(false) }
    var newClientName by remember { mutableStateOf("") }
    var newClientPhone by remember { mutableStateOf("") }
    var newClientGoal by remember { mutableStateOf("") }
    var newClientNotes by remember { mutableStateOf("") }
    var newClientTotalSessions by remember { mutableIntStateOf(10) }
    var newClientProgram by remember { mutableStateOf("Фулбоди (Full Body)") }

    // Состояние для ввода инвайт-кода атлетом
    var inviteCodeInput by remember { mutableStateOf("") }
    var inviteFeedbackMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isCoach) "Кабинет тренера: The Iron Apex" else "Тренерский портал атлета",
                            fontWeight = FontWeight.Bold,
                            color = GymTextPrimary,
                            fontSize = 17.sp
                        )
                        Text(
                            text = if (isCoach) "CRM подопечных и аудит техники" else "Взаимодействие с персональным наставником",
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
                actions = {
                    // Кнопка мгновенного переключения роли Атлет <-> Тренер (Спринт 15 RBAC)
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val current = userProfileDao.getUserProfileSync()
                                if (current != null) {
                                    val newRole = if (current.role == UserRole.COACH) UserRole.ATHLETE else UserRole.COACH
                                    userProfileDao.saveUserProfile(current.copy(role = newRole))
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCoach) GymAccentGreen.copy(alpha = 0.2f) else GymDarkSurfaceVariant
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = if (isCoach) GymAccentGreen else GymTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCoach) "Тренер" else "Атлет",
                            color = if (isCoach) GymAccentGreen else GymTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GymDarkBackground)
            )
        },
        containerColor = GymDarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isCoach) {
                // Вкладки кабинета тренера (CRM vs Видеоотчеты)
                TabRow(
                    selectedTabIndex = coachSelectedTab,
                    containerColor = GymDarkSurface,
                    contentColor = GymAccentGreen,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[coachSelectedTab]),
                            color = GymAccentGreen,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = coachSelectedTab == 0,
                        onClick = { coachSelectedTab = 0 },
                        text = {
                            Text(
                                text = "Подопечные CRM (${clients.size})",
                                fontWeight = if (coachSelectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                    Tab(
                        selected = coachSelectedTab == 1,
                        onClick = { coachSelectedTab = 1 },
                        text = {
                            Text(
                                text = "Видеоотчеты (${reviews.size})",
                                fontWeight = if (coachSelectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                }

                if (coachSelectedTab == 0) {
                    // ЭКРАН CRM ТРЕНЕРА (Спринт 16)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Карточка инвайт-кода тренера (Спринт 15)
                        item {
                            val coachCode = userProfile?.coachInviteCode ?: "APEX77"
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Ваш инвайт-код тренера",
                                                color = GymTextSecondary,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = coachCode,
                                                color = GymAccentGreen,
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = 2.sp
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(coachCode))
                                                },
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .background(GymDarkSurfaceVariant, CircleShape)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "Копировать",
                                                    tint = GymTextPrimary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                                        type = "text/plain"
                                                        putExtra(
                                                            Intent.EXTRA_TEXT,
                                                            "Тренируйся под моим руководством в The Iron Apex! Мой код тренера: $coachCode"
                                                        )
                                                    }
                                                    context.startActivity(Intent.createChooser(sendIntent, "Пригласить подопечного"))
                                                },
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .background(GymAccentGreen.copy(alpha = 0.2f), CircleShape)
                                            ) {
                                                // [AI-TASK:TASK-023] Custom Iron Hex Share Record Icon
                                                Icon(
                                                    painter = painterResource(GymIcons.ShareRecord),
                                                    contentDescription = "Поделиться",
                                                    tint = Color.Unspecified,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Передайте этот 6-значный код подопечным для привязки их профилей к вашей CRM-панели.",
                                        color = GymTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // Заголовок списка и кнопка добавления
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Активные подопечные (${clients.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = GymTextPrimary
                                )

                                Button(
                                    onClick = { showAddClientDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = GymAccentGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = GymDarkBackground, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Добавить", color = GymDarkBackground, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Список карточек подопечных
                        items(clients, key = { it.id }) { client ->
                            val remaining = (client.totalSessions - client.completedSessions).coerceAtLeast(0)
                            val isExpiring = remaining in 1..2
                            val isDepleted = remaining == 0
                            val progress = if (client.totalSessions > 0) client.completedSessions.toFloat() / client.totalSessions.toFloat() else 1f

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(CircleShape)
                                                    .background(GymDarkSurfaceVariant),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Person, contentDescription = null, tint = GymAccentGreen)
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column {
                                                Text(
                                                    text = client.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = GymTextPrimary
                                                )
                                                if (client.phone.isNotBlank()) {
                                                    Text(
                                                        text = client.phone,
                                                        fontSize = 12.sp,
                                                        color = GymTextSecondary
                                                    )
                                                }
                                            }
                                        }

                                        // Бейдж оставшихся тренировок
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    when {
                                                        isDepleted -> GymAccentRed.copy(alpha = 0.2f)
                                                        isExpiring -> GymAccentAmber.copy(alpha = 0.2f)
                                                        else -> GymAccentGreen.copy(alpha = 0.2f)
                                                    }
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = when {
                                                    isDepleted -> "Завершен"
                                                    isExpiring -> "Осталось: $remaining"
                                                    else -> "Осталось: $remaining"
                                                },
                                                color = when {
                                                    isDepleted -> GymAccentRed
                                                    isExpiring -> GymAccentAmber
                                                    else -> GymAccentGreen
                                                },
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Цель и программа
                                    if (client.goal.isNotBlank()) {
                                        Text(
                                            text = "Цель: ${client.goal}",
                                            fontSize = 13.sp,
                                            color = GymTextPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Text(
                                        text = "Программа: ${client.assignedProgramTitle}",
                                        fontSize = 12.sp,
                                        color = GymAccentBlue
                                    )
                                    if (client.notes.isNotBlank()) {
                                        Text(
                                            text = "Заметки: ${client.notes}",
                                            fontSize = 11.sp,
                                            color = GymTextSecondary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Прогресс абонемента
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Абонемент: ${client.completedSessions} / ${client.totalSessions} занятий",
                                            fontSize = 12.sp,
                                            color = GymTextSecondary
                                        )
                                        Text(
                                            text = "${(progress * 100).toInt()}%",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GymTextPrimary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    LinearProgressIndicator(
                                        progress = { progress.coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = when {
                                            isDepleted -> GymAccentRed
                                            isExpiring -> GymAccentAmber
                                            else -> GymAccentGreen
                                        },
                                        trackColor = GymDarkSurfaceVariant
                                    )

                                    if (isExpiring) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Warning, contentDescription = null, tint = GymAccentAmber, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Внимание: осталось мало занятий! Напомните о продлении.", color = GymAccentAmber, fontSize = 11.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Кнопки управления занятиями (-1 тренировка / продлить)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                coroutineScope.launch {
                                                    coachClientDao.incrementCompletedSession(client.id)
                                                }
                                            },
                                            enabled = remaining > 0,
                                            modifier = Modifier.weight(1.3f),
                                            colors = ButtonDefaults.buttonColors(containerColor = GymAccentGreen.copy(alpha = 0.2f)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(vertical = 8.dp)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = GymAccentGreen, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("-1 Тренировка", color = GymAccentGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                coroutineScope.launch {
                                                    coachClientDao.updateClient(
                                                        client.copy(
                                                            totalSessions = client.totalSessions + 10,
                                                            updatedAt = System.currentTimeMillis()
                                                        )
                                                    )
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = GymDarkSurfaceVariant),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(vertical = 8.dp)
                                        ) {
                                            Text("+10 Занятий", color = GymTextPrimary, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // ЭКРАН ВИДЕООТЧЕТОВ ДЛЯ ТРЕНЕРА
                    VideoReviewsContent(
                        reviews = reviews,
                        activeReview = activeReview,
                        feedbackText = feedbackText,
                        rating = rating,
                        onSelectReview = { rev ->
                            activeReview = rev
                            feedbackText = rev.feedbackComment.orEmpty()
                            rating = rev.coachRating ?: 5
                        },
                        onFeedbackChange = { feedbackText = it },
                        onRatingChange = { rating = it },
                        onSubmit = {
                            if (activeReview != null) {
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
                            }
                        }
                    )
                }
            } else {
                // ЭКРАН АТЛЕТА (ПОРТАЛ ВЗАИМОДЕЙСТВИЯ С ТРЕНЕРОМ)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        val linkedCode = userProfile?.linkedCoachCode
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = GymAccentGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Ваш персональный тренер",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = GymTextPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                if (linkedCode.isNullOrBlank()) {
                                    Text(
                                        text = "Привяжите аккаунт вашего фитнес-тренера по 6-значному инвайт-коду, чтобы получать индивидуальные программы тренировок и видеоразбор техники подходов.",
                                        fontSize = 13.sp,
                                        color = GymTextSecondary
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = inviteCodeInput,
                                        onValueChange = { inviteCodeInput = it.uppercase() },
                                        placeholder = { Text("Например: APEX77", color = GymTextSecondary) },
                                        label = { Text("Инвайт-код тренера", color = GymAccentGreen) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = GymTextPrimary,
                                            unfocusedTextColor = GymTextPrimary,
                                            focusedBorderColor = GymAccentGreen,
                                            unfocusedBorderColor = GymDarkSurfaceVariant
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    inviteFeedbackMessage?.let { msg ->
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(msg, color = GymAccentGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            if (inviteCodeInput.isNotBlank()) {
                                                coroutineScope.launch {
                                                    val cur = userProfileDao.getUserProfileSync()
                                                    if (cur != null) {
                                                        userProfileDao.saveUserProfile(cur.copy(linkedCoachCode = inviteCodeInput))
                                                        inviteFeedbackMessage = "Тренер с кодом $inviteCodeInput успешно подключен!"
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = GymAccentGreen),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Привязать тренера", color = GymDarkBackground, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    // Тренер уже подключен
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Статус: Активен", color = GymAccentGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("Код тренера: $linkedCode", color = GymTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                        }

                                        TextButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    val cur = userProfileDao.getUserProfileSync()
                                                    if (cur != null) {
                                                        userProfileDao.saveUserProfile(cur.copy(linkedCoachCode = null))
                                                    }
                                                }
                                            }
                                        ) {
                                            Text("Отвязать", color = GymAccentRed, fontSize = 12.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Назначенная программа
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = GymDarkSurfaceVariant),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("Назначенная программа тренировок:", fontSize = 12.sp, color = GymTextSecondary)
                                            Text("Сплит Верх / Низ (Upper / Lower)", fontWeight = FontWeight.Bold, color = GymAccentGreen, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Целевой RPE: 8.0 - 8.5 (1-2 повтора в запасе)", fontSize = 12.sp, color = GymTextPrimary)
                                            Text("Заметка тренера: 'Соблюдай паузу 1 секунду в нижней точке жима'", fontSize = 11.sp, color = GymTextSecondary)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Videocam, contentDescription = null, tint = GymAccentBlue)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Видеоаудит подходов",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = GymTextPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Вы можете прикреплять видеозаписи подходов прямо во время тренировки. Тренер проверит углы в суставах, траекторию снаряда и выставит оценку с голосовыми или текстовыми подсказками.",
                                    fontSize = 13.sp,
                                    color = GymTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Диалог добавления нового подопечного в CRM
        if (showAddClientDialog) {
            AlertDialog(
                onDismissRequest = { showAddClientDialog = false },
                containerColor = GymDarkSurface,
                title = {
                    Text("Новый подопечный", color = GymTextPrimary, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = newClientName,
                            onValueChange = { newClientName = it },
                            label = { Text("Имя и фамилия", color = GymAccentGreen) },
                            placeholder = { Text("Алексей Смирнов", color = GymTextSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = GymTextPrimary,
                                unfocusedTextColor = GymTextPrimary,
                                focusedBorderColor = GymAccentGreen,
                                unfocusedBorderColor = GymDarkSurfaceVariant
                            )
                        )

                        OutlinedTextField(
                            value = newClientPhone,
                            onValueChange = { newClientPhone = it },
                            label = { Text("Телефон / Telegram", color = GymAccentGreen) },
                            placeholder = { Text("+7 (999) 000-00-00", color = GymTextSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = GymTextPrimary,
                                unfocusedTextColor = GymTextPrimary,
                                focusedBorderColor = GymAccentGreen,
                                unfocusedBorderColor = GymDarkSurfaceVariant
                            )
                        )

                        OutlinedTextField(
                            value = newClientGoal,
                            onValueChange = { newClientGoal = it },
                            label = { Text("Цель тренировок", color = GymAccentGreen) },
                            placeholder = { Text("Набор массы, жим 100 кг", color = GymTextSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = GymTextPrimary,
                                unfocusedTextColor = GymTextPrimary,
                                focusedBorderColor = GymAccentGreen,
                                unfocusedBorderColor = GymDarkSurfaceVariant
                            )
                        )

                        OutlinedTextField(
                            value = newClientNotes,
                            onValueChange = { newClientNotes = it },
                            label = { Text("Травмы и ограничения", color = GymAccentGreen) },
                            placeholder = { Text("Без нагрузки на левое плечо", color = GymTextSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = GymTextPrimary,
                                unfocusedTextColor = GymTextPrimary,
                                focusedBorderColor = GymAccentGreen,
                                unfocusedBorderColor = GymDarkSurfaceVariant
                            )
                        )

                        Text("Количество тренировок в пакете:", color = GymTextSecondary, fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(8, 10, 12, 16).forEach { count ->
                                val selected = newClientTotalSessions == count
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) GymAccentGreen else GymDarkSurfaceVariant)
                                        .clickable { newClientTotalSessions = count }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "$count",
                                        color = if (selected) GymDarkBackground else GymTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newClientName.isNotBlank()) {
                                coroutineScope.launch {
                                    val now = System.currentTimeMillis()
                                    coachClientDao.insertClient(
                                        CoachClientEntity(
                                            id = UUID.randomUUID().toString(),
                                            name = newClientName.trim(),
                                            phone = newClientPhone.trim(),
                                            goal = newClientGoal.trim(),
                                            notes = newClientNotes.trim(),
                                            totalSessions = newClientTotalSessions,
                                            completedSessions = 0,
                                            assignedProgramTitle = newClientProgram,
                                            createdAt = now,
                                            updatedAt = now
                                        )
                                    )
                                    showAddClientDialog = false
                                    newClientName = ""
                                    newClientPhone = ""
                                    newClientGoal = ""
                                    newClientNotes = ""
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GymAccentGreen)
                    ) {
                        Text("Создать", color = GymDarkBackground, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddClientDialog = false }) {
                        Text("Отмена", color = GymTextSecondary)
                    }
                }
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun VideoReviewsContent(
    reviews: List<SetVideoReviewEntity>,
    activeReview: SetVideoReviewEntity?,
    feedbackText: String,
    rating: Int,
    onSelectReview: (SetVideoReviewEntity) -> Unit,
    onFeedbackChange: (String) -> Unit,
    onRatingChange: (Int) -> Unit,
    onSubmit: () -> Unit
) {
    if (activeReview != null) {
        val review = activeReview
        val context = LocalContext.current
        var isPlaying by remember { mutableStateOf(false) }
        var playbackError by remember { mutableStateOf<String?>(null) }

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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = null, tint = GymAccentBlue, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Видеозапись подхода #${review.setId}", fontWeight = FontWeight.Bold, color = GymTextPrimary, fontSize = 15.sp)
                            Text(review.localVideoUri.substringAfterLast("/"), color = GymTextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Оценка
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Оценка техники выполнения:", color = GymTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..5).forEach { star ->
                        IconButton(
                            onClick = { onRatingChange(star) },
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
                            else -> "Опасно (1/5)"
                        },
                        color = GymAccentAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Комментарий
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Замечания и рекомендации тренера:", color = GymTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = onFeedbackChange,
                    placeholder = { Text("Например: держи темп 2-0-1, отличная траектория", color = GymTextSecondary, fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GymTextPrimary,
                        unfocusedTextColor = GymTextPrimary,
                        focusedBorderColor = GymAccentGreen,
                        unfocusedBorderColor = GymDarkSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GymAccentGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = GymDarkBackground)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Сохранить и отправить отзыв", color = GymDarkBackground, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    } else if (reviews.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Videocam, contentDescription = null, tint = GymTextSecondary, modifier = Modifier.size(56.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Все видеоотчеты проверены!", color = GymTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Новые видео подходов от атлетов появятся здесь автоматически.", color = GymTextSecondary, fontSize = 12.sp)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(reviews, key = { it.id }) { review ->
                ReviewListItemCard(
                    review = review,
                    onClick = { onSelectReview(review) }
                )
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
