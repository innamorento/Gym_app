package com.gym.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gym.core.database.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    fun getUserProfile(id: String = "default_user"): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    suspend fun getUserProfileSync(id: String = "default_user"): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profiles SET weightKg = :newWeight WHERE id = :id")
    suspend fun updateWeight(newWeight: Float, id: String = "default_user")

    @Query("SELECT COUNT(*) FROM user_profiles WHERE isCompleted = 1")
    suspend fun hasCompletedProfile(): Int
}
