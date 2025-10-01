package de.flowtron.sokoban.state

import android.util.Log
import de.flowtron.sokoban.game.LevelData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LevelDataStateFlow {
    private val mutableLevelDataStateFlow: MutableStateFlow<LevelData?> = MutableStateFlow(null)
    fun setLevelData(levelData: LevelData?) {
        //Log.d("LevelDataStateFlow", "level data has been changed")
        mutableLevelDataStateFlow.value = levelData
    }

    val levelData = mutableLevelDataStateFlow.asStateFlow()
    fun showLevelData() {
        Log.d("StateFlowHolder", "LevelData = \n${levelData.value}")
    }
}