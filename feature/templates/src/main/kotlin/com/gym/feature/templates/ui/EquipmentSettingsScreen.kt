package com.gym.feature.templates.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.runtime.collectAsState
import com.gym.core.database.dao.UserProfileDao
import com.gym.core.model.UserRole
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.gym.core.database.backup.BackupManager
import org.koin.compose.koinInject
import kotlinx.coroutines.launch
import android.content.Intent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gym.core.model.Equipment
import com.gym.core.model.EquipmentWeightProfile
import com.gym.core.ui.theme.GymAccentBlue
import com.gym.core.ui.theme.GymAccentGreen
import com.gym.core.ui.theme.GymDarkBackground
import com.gym.core.ui.theme.GymDarkSurface
import com.gym.core.ui.theme.GymDarkSurfaceVariant
import com.gym.core.ui.theme.GymTextPrimary
import com.gym.core.ui.theme.GymTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentSettingsScreen(
    onBackClick: () -> Unit,
    onSaveClick: (EquipmentWeightProfile) -> Unit = {}
) {
    // Настройки гантелей
    var dumbbellSmallStep by remember { mutableDoubleStateOf(1.0) }
    var dumbbellThreshold by remember { mutableDoubleStateOf(10.0) }
    var dumbbellHeavyStep by remember { mutableDoubleStateOf(2.0) }

    // Настройки блоков
    var cableStep by remember { mutableDoubleStateOf(2.5) }

    // Настройки штанги
    var barWeight by remember { mutableDoubleStateOf(20.0) }
    var minPlatesPair by remember { mutableDoubleStateOf(2.5) }

    // Резервное копирование и экспорт
    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonInput by remember { mutableStateOf("") }
    var importStatusMessage by remember { mutableStateOf<String?>(null) }

    // Правовые документы (Спринт 17)
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val backupManager: BackupManager = koinInject()
    val userProfileDao: UserProfileDao = koinInject()
    val profileEntity by userProfileDao.getUserProfile().collectAsState(initial = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Настройки и профиль",
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
                actions = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Сохранить",
                            tint = GymAccentGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GymDarkBackground)
            )
        },
        containerColor = GymDarkBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Настройте реальную дискретность снарядов вашего зала для точной автопрогрессии весов.",
                    fontSize = 14.sp,
                    color = GymTextSecondary
                )
            }

            // Гантели
            item {
                EquipmentConfigCard(
                    title = "Гантельный ряд",
                    subtitle = "Градация шага веса легких и тяжелых гантелей"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Порог легких гантелей: ${dumbbellThreshold.toInt()} кг", color = GymTextPrimary)
                        }
                        Slider(
                            value = dumbbellThreshold.toFloat(),
                            onValueChange = { dumbbellThreshold = it.toDouble() },
                            valueRange = 6f..14f,
                            steps = 3,
                            colors = SliderDefaults.colors(
                                thumbColor = GymAccentGreen,
                                activeTrackColor = GymAccentGreen
                            )
                        )

                        Text("Шаг до ${dumbbellThreshold.toInt()} кг: $dumbbellSmallStep кг (1, 2, 3...)", color = GymTextSecondary, fontSize = 13.sp)
                        Text("Шаг свыше ${dumbbellThreshold.toInt()} кг: $dumbbellHeavyStep кг (12, 14, 16...)", color = GymTextSecondary, fontSize = 13.sp)
                    }
                }
            }

            // Блочные тренажеры и кроссоверы
            item {
                EquipmentConfigCard(
                    title = "Блочные тренажеры (Cables / Stacks)",
                    subtitle = "Шаг пластин в грузоблочных тренажерах"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Текущий шаг грузоблока: $cableStep кг", color = GymTextPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StepChip(label = "2.5 кг", isSelected = cableStep == 2.5) { cableStep = 2.5 }
                            StepChip(label = "5.0 кг", isSelected = cableStep == 5.0) { cableStep = 5.0 }
                            StepChip(label = "1.25 кг (с микроплиткой)", isSelected = cableStep == 1.25) { cableStep = 1.25 }
                        }
                    }
                }
            }

            // Штанги и блины
            item {
                EquipmentConfigCard(
                    title = "Штанги и диски (Barbell)",
                    subtitle = "Базовый вес грифа и минимальный шаг блинов"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Вес грифа: ${barWeight.toInt()} кг", color = GymTextPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StepChip(label = "20 кг (Мужской)", isSelected = barWeight == 20.0) { barWeight = 20.0 }
                            StepChip(label = "15 кг (Женский)", isSelected = barWeight == 15.0) { barWeight = 15.0 }
                            StepChip(label = "10 кг (EZ-гриф)", isSelected = barWeight == 10.0) { barWeight = 10.0 }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Минимальный шаг блинов на сторону: ${minPlatesPair / 2.0} кг (шаг штанги $minPlatesPair кг)", color = GymTextSecondary, fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StepChip(label = "2 x 1.25 кг (шаг 2.5)", isSelected = minPlatesPair == 2.5) { minPlatesPair = 2.5 }
                            StepChip(label = "2 x 0.5 кг (микродиски, шаг 1.0)", isSelected = minPlatesPair == 1.0) { minPlatesPair = 1.0 }
                        }
                    }
                }
            }

            // Карточка управления данными и бэкапами
            item(key = "backup_restore_card") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Backup, contentDescription = null, tint = GymAccentGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Резервное копирование и экспорт",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GymTextPrimary
                                )
                            }
                            Text(
                                text = "Сохранение всех тренировок и шаблонов на телефон или в облако",
                                fontSize = 12.sp,
                                color = GymTextSecondary
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            val json = backupManager.exportBackupJson()
                                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_SUBJECT, "The_Iron_Apex_Backup.json")
                                                putExtra(Intent.EXTRA_TEXT, json)
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "Сохранить бэкап (JSON)"))
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = GymDarkSurfaceVariant),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.FileUpload, contentDescription = null, tint = GymAccentGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Бэкап JSON", fontSize = 12.sp, color = GymTextPrimary)
                                }

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            val csv = backupManager.exportWorkoutsCsv()
                                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_SUBJECT, "The_Iron_Apex_Workouts.csv")
                                                putExtra(Intent.EXTRA_TEXT, csv)
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "Экспорт тренировок (CSV)"))
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = GymDarkSurfaceVariant),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.FileDownload, contentDescription = null, tint = GymAccentBlue, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Таблица CSV", fontSize = 12.sp, color = GymTextPrimary)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { showRestoreDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = GymAccentGreen.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Восстановить данные из бэкапа", color = GymAccentGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // Карточка профиля и авторизации (Спринт 15 RBAC)
                item(key = "account_auth_card") {
                    val isGuest = profileEntity?.isGuest != false
                    val currentRole = profileEntity?.role ?: UserRole.ATHLETE

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = GymAccentGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Аккаунт и авторизация",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GymTextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (isGuest) "Гостевой режим (Офлайн)" else "Google Аккаунт",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = GymTextPrimary
                                    )
                                    Text(
                                        text = if (isGuest) "Локальная база данных (SQLite Room)" else (profileEntity?.email ?: "apex.athlete@gmail.com"),
                                        fontSize = 12.sp,
                                        color = if (isGuest) GymTextSecondary else GymAccentGreen
                                    )
                                }

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            val cur = userProfileDao.getUserProfileSync()
                                            if (cur != null) {
                                                if (isGuest) {
                                                    userProfileDao.saveUserProfile(
                                                        cur.copy(
                                                            isGuest = false,
                                                            email = "apex.athlete@gmail.com"
                                                        )
                                                    )
                                                } else {
                                                    userProfileDao.saveUserProfile(
                                                        cur.copy(
                                                            isGuest = true,
                                                            email = null
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isGuest) GymAccentGreen else GymDarkSurfaceVariant
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (isGuest) "Войти с Google" else "Выйти",
                                        color = if (isGuest) GymDarkBackground else GymTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Текущая роль в приложении:",
                                fontSize = 13.sp,
                                color = GymTextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StepChip(
                                    label = "🏋️ Атлет",
                                    isSelected = currentRole == UserRole.ATHLETE
                                ) {
                                    coroutineScope.launch {
                                        val cur = userProfileDao.getUserProfileSync()
                                        if (cur != null) {
                                            userProfileDao.saveUserProfile(cur.copy(role = UserRole.ATHLETE))
                                        }
                                    }
                                }

                                StepChip(
                                    label = "👔 Тренер (CRM)",
                                    isSelected = currentRole == UserRole.COACH
                                ) {
                                    coroutineScope.launch {
                                        val cur = userProfileDao.getUserProfileSync()
                                        if (cur != null) {
                                            userProfileDao.saveUserProfile(cur.copy(role = UserRole.COACH))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Карточка правовой информации и релиза 1.0 (Спринт 17)
                item(key = "legal_info_card") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = GymAccentBlue)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "The Iron Apex v1.0.0 (Release)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GymTextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Безопасный трекинг тренировок. Все данные принадлежат вам и хранятся локально.",
                                fontSize = 12.sp,
                                color = GymTextSecondary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { showPrivacyDialog = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = GymDarkSurfaceVariant),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    Text("Конфиденциальность", fontSize = 11.sp, color = GymTextPrimary)
                                }

                                Button(
                                    onClick = { showTermsDialog = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = GymDarkSurfaceVariant),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    Text("Условия сервиса", fontSize = 11.sp, color = GymTextPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showRestoreDialog) {
            AlertDialog(
                onDismissRequest = {
                    showRestoreDialog = false
                    importStatusMessage = null
                },
                containerColor = GymDarkSurface,
                title = {
                    Text("Восстановление из JSON", fontWeight = FontWeight.Bold, color = GymTextPrimary)
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Вставьте содержимое JSON-файла резервной копии для восстановления базы тренировок:",
                            fontSize = 13.sp,
                            color = GymTextSecondary
                        )

                        OutlinedTextField(
                            value = restoreJsonInput,
                            onValueChange = { restoreJsonInput = it },
                            placeholder = { Text("Вставьте JSON здесь...", color = GymTextSecondary) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = GymTextPrimary,
                                unfocusedTextColor = GymTextPrimary,
                                focusedBorderColor = GymAccentGreen,
                                unfocusedBorderColor = GymDarkSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        importStatusMessage?.let { status ->
                            Text(status, color = GymAccentGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (restoreJsonInput.isNotBlank()) {
                                coroutineScope.launch {
                                    val result = backupManager.importBackupJson(restoreJsonInput.trim())
                                    result.onSuccess { summary ->
                                        importStatusMessage = "Успешно: шаблонов: ${summary.templatesRestored}, тренировок: ${summary.workoutsRestored}"
                                        restoreJsonInput = ""
                                    }.onFailure { err ->
                                        importStatusMessage = "Ошибка импорта: ${err.message}"
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GymAccentGreen)
                    ) {
                        Text("Восстановить", color = GymDarkBackground, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showRestoreDialog = false
                        importStatusMessage = null
                    }) {
                        Text("Закрыть", color = GymTextSecondary)
                    }
                }
            )
        }

        // Диалог политики конфиденциальности (Спринт 17)
        if (showPrivacyDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacyDialog = false },
                containerColor = GymDarkSurface,
                title = {
                    Text("Политика конфиденциальности", color = GymTextPrimary, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "1. Принцип Offline-First",
                            fontWeight = FontWeight.Bold,
                            color = GymAccentGreen,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "The Iron Apex разработан по принципу нулевого сбора персональных данных. Все ваши тренировочные логи, шаблоны, рабочие веса и профили хранятся исключительно на вашем устройстве в локальной базе Room SQLite.",
                            fontSize = 12.sp,
                            color = GymTextSecondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "2. Безопасность и бэкапы",
                            fontWeight = FontWeight.Bold,
                            color = GymAccentGreen,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Экспорт данных (JSON/CSV) осуществляется только по вашей прямой команде через системный Android Share Sheet. Мы не передаем тренировочные дневники сторонним серверам или рекламным сетям.",
                            fontSize = 12.sp,
                            color = GymTextSecondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "3. Видеозаписи подходов",
                            fontWeight = FontWeight.Bold,
                            color = GymAccentGreen,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Медиафайлы разбора техники сохраняются в изолированном защищенном хранилище приложения на вашем телефоне.",
                            fontSize = 12.sp,
                            color = GymTextSecondary
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showPrivacyDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = GymAccentGreen)
                    ) {
                        Text("Понятно", color = GymDarkBackground, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // Диалог условий использования (Спринт 17)
        if (showTermsDialog) {
            AlertDialog(
                onDismissRequest = { showTermsDialog = false },
                containerColor = GymDarkSurface,
                title = {
                    Text("Условия использования (Terms)", color = GymTextPrimary, fontWeight = FontWeight.Bold)
                },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "1. Ответственность за здоровье",
                            fontWeight = FontWeight.Bold,
                            color = GymAccentGreen,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Силовые тренировки с тяжелыми снарядами сопряжены с риском травм. Перед началом высокоинтенсивных тренировок проконсультируйтесь с врачом. Всегда соблюдайте правила безопасности и используйте страховочные упоры.",
                            fontSize = 12.sp,
                            color = GymTextSecondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "2. Алгоритмы автопрогрессии и RPE",
                            fontWeight = FontWeight.Bold,
                            color = GymAccentGreen,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Калькуляторы 1RM, подбора блинов и рекомендации шага весов носят вспомогательный расчетный характер. Окончательное решение о рабочем весе принимает атлет по самочувствию.",
                            fontSize = 12.sp,
                            color = GymTextSecondary
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showTermsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = GymAccentGreen)
                    ) {
                        Text("Принимаю", color = GymDarkBackground, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }

@Composable
fun EquipmentConfigCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GymDarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = GymAccentBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GymTextPrimary)
            }
            Text(text = subtitle, fontSize = 12.sp, color = GymTextSecondary)
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
fun StepChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    androidx.compose.material3.FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label, fontSize = 12.sp) },
        colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
            selectedContainerColor = GymAccentGreen,
            selectedLabelColor = GymDarkBackground,
            containerColor = GymDarkSurfaceVariant,
            labelColor = GymTextPrimary
        )
    )
}
