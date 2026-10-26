package com.gym.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gym.core.database.entity.EquipmentSettingsEntity
import com.gym.core.model.Equipment
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipmentSettingsDao {

    @Query("SELECT * FROM equipment_settings")
    fun getAllSettings(): Flow<List<EquipmentSettingsEntity>>

    @Query("SELECT * FROM equipment_settings WHERE equipment = :equipment")
    suspend fun getSettingsForEquipment(equipment: Equipment): EquipmentSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: EquipmentSettingsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAllSettings(settingsList: List<EquipmentSettingsEntity>)
}
