package de.flowtron.sokoban.state

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameDataInfoStateFlow {
    private val mutableGameDataInfoStateFlow: MutableStateFlow<GameDataInfo?> =
        MutableStateFlow(null)

    fun setGameDataInfo(gameDataInfo: GameDataInfo?) {
        mutableGameDataInfoStateFlow.value = gameDataInfo
    }

    val gameDataInfo = mutableGameDataInfoStateFlow.asStateFlow()
    fun showGameDataInfo() {
        Log.d("StateFlowHolder", "GameDataInfo = ${gameDataInfo.value}")
    }
}