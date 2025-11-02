package de.flowtron.sokoban.state

import android.util.Log
import de.flowtron.sokoban.game.MovementHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MovementSolutionStateFlow {
    private val mutableMovementSolutionStateFlow: MutableStateFlow<MovementHistory> =
        MutableStateFlow(MovementHistory(emptyList()))

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
        if (index >= 0 && index <= maxIndex) { // you can be on the initial pusher place, or at step #1 to #N inclusive
            mutableIndexStateFlow.value = index
        } else {
            mutableIndexStateFlow.value = index.coerceIn(0, maxIndex)
        }
    }

    val indexStateFlow = mutableIndexStateFlow.asStateFlow()

    fun partialSolution(): MovementHistory =
        MovementHistory(movementSolution.value.data.subList(0, indexStateFlow.value))

    fun showPartialSolution() {
        Log.d("StateFlowHolder", "partial solution = \n${partialSolution().toDirections()}")
    }
}