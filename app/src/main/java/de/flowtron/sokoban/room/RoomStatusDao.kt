package de.flowtron.sokoban.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomStatusDao {
    @Query("SELECT * FROM status WHERE id = 1")
    fun getConfiguration(): Flow<RoomStatus?> // Use Flow for reactive updates

    @Query("SELECT * FROM status WHERE id = 1")
    suspend fun getConfigurationSnapshot(): RoomStatus? // For one-time reads

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfiguration(configuration: RoomStatus)

    @Update
    suspend fun updateConfiguration(configuration: RoomStatus)

    // Helper to quickly check if setup is done
    @Query("SELECT isInitialSetupDone FROM status WHERE id = 1")
    suspend fun isInitialSetupDone(): Boolean?
}