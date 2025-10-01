package de.flowtron.sokoban.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomLevelDao {
    @Query("SELECT * FROM level")
    //suspend fun getAllLevels(): List<RoomLevel?>
    suspend fun getAllLevels(): List<RoomLevel>

    @Query("SELECT * FROM level")
    fun getAllLevelsFlow(): Flow<List<RoomLevel>>

    // TODO / FIXME -- if this is the only LiveData ..
    // .. could we not ditch the ambiguity and dependency and only use Flows instead?
    @Query("SELECT * FROM level")
    fun getAllLevelsLiveData(): LiveData<List<RoomLevel>>

    @Query("SELECT * FROM level WHERE id = :levelId")
    suspend fun getLevelById(levelId: Int): RoomLevel?

    @Query("SELECT * FROM level WHERE combo = :combo AND world = :world AND level = :level")
    suspend fun getLevelByPath(combo: String, world: String, level: String): RoomLevel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLevel(level: RoomLevel)

    @Update
    suspend fun updateLevel(level: RoomLevel)
}