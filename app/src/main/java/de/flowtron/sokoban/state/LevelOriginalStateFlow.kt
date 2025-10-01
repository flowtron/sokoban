package de.flowtron.sokoban.state

import android.util.Log
import de.flowtron.sokoban.game.LevelData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LevelOriginalStateFlow {
    private val mutableLevelOriginalStateFlow: MutableStateFlow<LevelData?> = MutableStateFlow(null)
    fun setLevelOriginal(levelData: LevelData?) {
        mutableLevelOriginalStateFlow.value = levelData
    }

    val levelOriginal = mutableLevelOriginalStateFlow.asStateFlow()
    fun showLevelOriginal() {
        Log.d("StateFlowHolder", "LevelOriginal = \n${levelOriginal.value}")
    }
}