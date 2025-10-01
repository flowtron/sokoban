package de.flowtron.sokoban.state

import android.util.Log
import de.flowtron.sokoban.game.MovementHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max

class MovementHistoryStateFlow {
    private val mutableMovementHistoryStateFlow: MutableStateFlow<MovementHistory> =
        MutableStateFlow(MovementHistory(emptyList()))

    fun setMovementHistory(movementHistory: MovementHistory) {
        mutableMovementHistoryStateFlow.value = movementHistory
    }

    val movementHistory = mutableMovementHistoryStateFlow.asStateFlow()
    fun showMovementHistory() {
        Log.d("StateFlowHolder", "MovementHistory = \n${movementHistory.value.toDirections()}")
    }

    private val mutableIndexStateFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    fun setIndex(index: Int) {
        val maxIndex = movementHistory.value.data.size
        //Log.d("MovementHistory", "0 <= $index <= $maxIndex")
        if (index >= 0 && index <= maxIndex) {
            mutableIndexStateFlow.value = index
        } else {
            if (index < 0) mutableIndexStateFlow.value = 0
            if (index > maxIndex) mutableIndexStateFlow.value = maxIndex
        }
    }

    fun stepIndex(delta: Int) {
        setIndex(mutableIndexStateFlow.value + delta)
    }

    val indexStateFlow = mutableIndexStateFlow.asStateFlow()

    fun partialHistory(): MovementHistory =
        MovementHistory(movementHistory.value.data.subList(0, indexStateFlow.value))

    fun showPartialHistory() {
        Log.d("StateFlowHolder", "partial solution = \n${partialHistory().toDirections()}")
    }
}