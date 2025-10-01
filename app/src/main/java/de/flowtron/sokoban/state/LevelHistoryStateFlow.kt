package de.flowtron.sokoban.state

import android.util.Log
import de.flowtron.sokoban.game.LevelData
import de.flowtron.sokoban.game.MovementHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LevelHistoryStateFlow {
    private val mutableLevelHistoryStateFlow: MutableStateFlow<MovementHistory?> = MutableStateFlow(MovementHistory(listOf()))
    fun setLevelHistory(history: MovementHistory) {
        mutableLevelHistoryStateFlow.value = history
    }
    val levelHistory = mutableLevelHistoryStateFlow.asStateFlow()
    fun showLevelHistory() {
        Log.d("StateFlowHolder", "LevelHistory = \n${levelHistory.value}")
    }

    private val mutableIndexStateFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    fun setIndex(index: Int) {
        val maxIndex = levelHistory.value?.data?.size ?: 0
        if (index >= 0 && index < maxIndex) {
            mutableIndexStateFlow.value = index
        } else {
            if (index < 0) mutableIndexStateFlow.value = 0
            if (index >= maxIndex) mutableIndexStateFlow.value = maxIndex - 1
        }
    }
    val indexStateFlow = mutableIndexStateFlow.asStateFlow()

    fun partialHistory(): MovementHistory {
        //MovementHistory(levelHistory.value.data.subList(0, indexStateFlow.value))
        //requireNotNull(levelHistory.value)
        val curHistory = levelHistory.value
        if(curHistory != null && curHistory.data.isNotEmpty()){
            val newHistory = curHistory.data.subList(0, indexStateFlow.value)
            return MovementHistory(newHistory)
        } else {
            return MovementHistory(listOf())
        }
    }

    fun showPartialHistory() {
        Log.d("StateFlowHolder", "partial history = \n${partialHistory().toDirections()}")
    }
}