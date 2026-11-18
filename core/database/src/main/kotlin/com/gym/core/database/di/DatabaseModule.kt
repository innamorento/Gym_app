package com.gym.core.database.di

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gym.core.database.DefaultTemplates
import com.gym.core.database.GymDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single<GymDatabase> {
        val context = androidContext()
        var databaseInstance: GymDatabase? = null
        val callback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val exDao = databaseInstance?.exerciseDao()
                    val tmplDao = databaseInstance?.templateDao()
                    val planDao = databaseInstance?.workoutPlanDao()
                    if (exDao != null && tmplDao != null && planDao != null) {
                        DefaultTemplates.seedDefaultDataIfEmpty(context, exDao, tmplDao, planDao)
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val exDao = databaseInstance?.exerciseDao()
                    val tmplDao = databaseInstance?.templateDao()
                    val planDao = databaseInstance?.workoutPlanDao()
                    if (exDao != null && tmplDao != null && planDao != null) {
                        DefaultTemplates.seedDefaultDataIfEmpty(context, exDao, tmplDao, planDao)
                    }
                }
            }
        }

        val db = Room.databaseBuilder(
            context,
            GymDatabase::class.java,
            "gym_tracker.db"
        )
            .addCallback(callback)
            .fallbackToDestructiveMigration()
            .build()
        databaseInstance = db

        // Гарантированный посев данных на IO-потоке сразу после создания инстанса
        CoroutineScope(Dispatchers.IO).launch {
            DefaultTemplates.seedDefaultDataIfEmpty(context, db.exerciseDao(), db.templateDao(), db.workoutPlanDao())
            seedDemoCoachClientsIfEmpty(db.coachClientDao())
        }

        db
    }

    single { get<GymDatabase>().exerciseDao() }
    single { get<GymDatabase>().templateDao() }
    single { get<GymDatabase>().workoutDao() }
    single { get<GymDatabase>().equipmentSettingsDao() }
    single { get<GymDatabase>().coachAndReviewDao() }
    single { get<GymDatabase>().userProfileDao() }
    single { get<GymDatabase>().coachClientDao() }
    single { get<GymDatabase>().workoutPlanDao() }
    single { com.gym.core.database.backup.BackupManager(get(), get(), get(), get()) }
}

private suspend fun seedDemoCoachClientsIfEmpty(dao: com.gym.core.database.dao.CoachClientDao) {
    if (dao.getClientCount() == 0) {
        val now = System.currentTimeMillis()
        dao.insertClients(
            listOf(
                com.gym.core.database.entity.CoachClientEntity(
                    id = "client_1",
                    name = "Алексей Смирнов",
                    phone = "+7 (999) 111-22-33",
                    goal = "Набор массы + Жим 100 кг",
                    notes = "Без резких движений в плече, умеренный RPE",
                    totalSessions = 10,
                    completedSessions = 8,
                    assignedProgramTitle = "Грудь + Трицепс (Push)",
                    createdAt = now - 14 * 86400000L,
                    updatedAt = now - 86400000L
                ),
                com.gym.core.database.entity.CoachClientEntity(
                    id = "client_2",
                    name = "Екатерина Васильева",
                    phone = "+7 (916) 444-55-66",
                    goal = "Снижение жировой массы и рельеф",
                    notes = "Колени здоровы, акцент на ягодичные мосты и присед",
                    totalSessions = 12,
                    completedSessions = 5,
                    assignedProgramTitle = "Сплит Верх / Низ (Upper / Lower)",
                    createdAt = now - 20 * 86400000L,
                    updatedAt = now - 2 * 86400000L
                ),
                com.gym.core.database.entity.CoachClientEntity(
                    id = "client_3",
                    name = "Дмитрий Орлов",
                    phone = "+7 (903) 777-88-99",
                    goal = "Пауэрлифтинг / Силовой прогресс",
                    notes = "Подготовка к нормативам, тяжелая база",
                    totalSessions = 8,
                    completedSessions = 8,
                    assignedProgramTitle = "Силовой фулбоди 3x5",
                    createdAt = now - 30 * 86400000L,
                    updatedAt = now - 3 * 86400000L
                )
            )
        )
    }
}
