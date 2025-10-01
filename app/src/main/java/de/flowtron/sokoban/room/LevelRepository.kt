package de.flowtron.sokoban.room

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LevelRepository @Inject constructor(private val roomLevelDao: RoomLevelDao) {

    //suspend fun insertLevel(level: Level) {}

    suspend fun fetchLevel(levelId: Int): RoomLevel? {
        return roomLevelDao.getLevelById(levelId)
    }

    fun observeAllLevelsFlow(): Flow<List<RoomLevel>> {
        return roomLevelDao.getAllLevelsFlow()
    }

    fun observeAllLevelsLiveData(): LiveData<List<RoomLevel>> {
        return roomLevelDao.getAllLevelsLiveData()
    }
}