/*package de.flowtron.sokoban.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface RoomConfigDao {
    @Query("SELECT * FROM configString")
    suspend fun getAllStrings(): Map<String, String>

    @Query("SELECT * FROM configString WHERE name = :name")
    suspend fun getStringByName(name: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertString(name: String, value: String)

    @Update
    suspend fun updateString(name: String, value: String)

    @Query("SELECT * FROM configInt")
    suspend fun getAllInts(): Map<String, Int>

    @Query("SELECT * FROM configInt WHERE name = :name")
    suspend fun getIntByName(name: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInt(name: String, value: Int)

    @Update
    suspend fun updateInt(name: String, value: Int)
}
*/