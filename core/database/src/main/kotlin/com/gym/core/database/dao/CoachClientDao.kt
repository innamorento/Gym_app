package com.gym.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gym.core.database.entity.CoachClientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CoachClientDao {

    @Query("SELECT * FROM coach_clients ORDER BY updatedAt DESC")
    fun getClientsFlow(): Flow<List<CoachClientEntity>>

    @Query("SELECT * FROM coach_clients WHERE id = :id")
    suspend fun getClientById(id: String): CoachClientEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: CoachClientEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClients(clients: List<CoachClientEntity>)

    @Update
    suspend fun updateClient(client: CoachClientEntity)

    @Query("DELETE FROM coach_clients WHERE id = :id")
    suspend fun deleteClient(id: String)

    @Query("UPDATE coach_clients SET completedSessions = completedSessions + 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun incrementCompletedSession(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM coach_clients")
    suspend fun getClientCount(): Int
}
