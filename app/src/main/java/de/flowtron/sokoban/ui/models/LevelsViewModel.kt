package de.flowtron.sokoban.ui.models

import android.content.res.AssetManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.flowtron.sokoban.game.LevelLoader
import de.flowtron.sokoban.room.LevelRepository
import de.flowtron.sokoban.room.RoomLevel
import de.flowtron.sokoban.room.RoomLevelDao
import de.flowtron.sokoban.safeLaunch
import de.flowtron.sokoban.state.GameDataInfo
import javax.inject.Inject

@HiltViewModel
class LevelsViewModel @Inject constructor(
    levelRepository: LevelRepository,
    private val levelLoader: LevelLoader,
    private val assetManager: AssetManager,
    private val roomLevelDao: RoomLevelDao,
) : ViewModel() {

    val allLevels: LiveData<List<RoomLevel>> = levelRepository.observeAllLevelsLiveData()

    fun loadLevel(id: Long, combo: String, world: String, level: String) {
        loadLevel(GameDataInfo(id, combo, world, level))
    }

    fun loadLevel(gameDataInfo: GameDataInfo) {
        Log.i("LevelsViewModel", "loadLevel(${gameDataInfo.id})")
        viewModelScope.safeLaunch {
            levelLoader.loadMap(gameDataInfo, assetManager, roomLevelDao)
        }
    }
}