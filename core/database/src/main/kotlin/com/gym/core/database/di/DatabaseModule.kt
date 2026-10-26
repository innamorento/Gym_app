package com.gym.core.database.di

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gym.core.database.DefaultExercises
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
                    databaseInstance?.exerciseDao()?.insertExercises(DefaultExercises.list)
                }
            }
        }

        databaseInstance = Room.databaseBuilder(
            androidContext(),
            GymDatabase::class.java,
            "gym_tracker.db"
        )
            .addCallback(callback)
            .fallbackToDestructiveMigration()
            .build()

        databaseInstance
    }

    single { get<GymDatabase>().exerciseDao() }
    single { get<GymDatabase>().templateDao() }
    single { get<GymDatabase>().workoutDao() }
    single { get<GymDatabase>().equipmentSettingsDao() }
    single { get<GymDatabase>().coachAndReviewDao() }
}
