package de.flowtron.sokoban.state

import android.util.Log
import de.flowtron.sokoban.game.LevelData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LevelSolutionStateFlow {
    private val mutableLevelSolutionStateFlow: MutableStateFlow<LevelData?> = MutableStateFlow(null)
    fun setLevelSolution(levelData: LevelData?) {
        mutableLevelSolutionStateFlow.value = levelData
    }

    val levelSolution = mutableLevelSolutionStateFlow.asStateFlow()
    fun showLevelSolution() {
        Log.d("StateFlowHolder", "LevelSolution = \n${levelSolution.value}")
    }
}