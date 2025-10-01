package de.flowtron.sokoban.state

import android.util.Log
import de.flowtron.sokoban.game.MovementHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max

class MovementSolutionStateFlow {
    private val mutableMovementSolutionStateFlow: MutableStateFlow<MovementHistory> = MutableStateFlow(MovementHistory(emptyList()))
    fun setMovementSolution(movementHistory: MovementHistory) {
        mutableMovementSolutionStateFlow.value = movementHistory
    }

    val movementSolution = mutableMovementSolutionStateFlow.asStateFlow()
    fun showMovementSolution() {
        Log.d("StateFlowHolder", "MovementSolution = \n${movementSolution.value.toDirections()}")
    }

    private val mutableIndexStateFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    fun setIndex(index: Int) {
        val maxIndex = movementSolution.value.data.size
        if(index>=0 && index<maxIndex){
            mutableIndexStateFlow.value = index
        }else{
            if(index<0) mutableIndexStateFlow.value = 0
            if(index>=maxIndex) mutableIndexStateFlow.value = maxIndex - 1
        }
    }
    val indexStateFlow = mutableIndexStateFlow.asStateFlow()

    fun partialSolution() : MovementHistory = MovementHistory(movementSolution.value.data.subList(0, indexStateFlow.value))
    fun showPartialSolution() {
        Log.d("StateFlowHolder", "partial solution = \n${partialSolution().toDirections()}")
    }
}