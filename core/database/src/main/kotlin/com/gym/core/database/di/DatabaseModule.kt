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
        var databaseInstance: GymDatabase? = null
        val callback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val exDao = databaseInstance?.exerciseDao()
                    val tmplDao = databaseInstance?.templateDao()
                    if (exDao != null && tmplDao != null) {
                        DefaultTemplates.seedDefaultDataIfEmpty(exDao, tmplDao)
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val exDao = databaseInstance?.exerciseDao()
                    val tmplDao = databaseInstance?.templateDao()
                    if (exDao != null && tmplDao != null) {
                        DefaultTemplates.seedDefaultDataIfEmpty(exDao, tmplDao)
                    }
                }
            }
        }

        val db = Room.databaseBuilder(
            androidContext(),
            GymDatabase::class.java,
            "gym_tracker.db"
        )
            .addCallback(callback)
            .fallbackToDestructiveMigration()
            .build()
        databaseInstance = db

        // Гарантированный посев данных на IO-потоке сразу после создания инстанса
        CoroutineScope(Dispatchers.IO).launch {
            DefaultTemplates.seedDefaultDataIfEmpty(db.exerciseDao(), db.templateDao())
        }

        db
    }

    single { get<GymDatabase>().exerciseDao() }
    single { get<GymDatabase>().templateDao() }
    single { get<GymDatabase>().workoutDao() }
    single { get<GymDatabase>().equipmentSettingsDao() }
    single { get<GymDatabase>().coachAndReviewDao() }
}
