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
        mutableIndexStateFlow.value = index.coerceIn(0, maxIndex) // 0, (1, …, N)
    }

    val indexStateFlow = mutableIndexStateFlow.asStateFlow()

    fun partialSolution(): MovementHistory =
        MovementHistory(movementSolution.value.data.subList(0, indexStateFlow.value))

    fun showPartialSolution() {
        Log.d("StateFlowHolder", "partial solution = \n${partialSolution().toDirections()}")
    }
}