package de.flowtron.sokoban.state

import android.util.Log
import de.flowtron.sokoban.game.Coordinates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class OffsetStateFlow {
    private val mutableOffsetStateFlow: MutableStateFlow<Coordinates?> = MutableStateFlow(null)
    fun setOffset(offset: Coordinates?) {
        mutableOffsetStateFlow.value = offset
    }

    val offset = mutableOffsetStateFlow.asStateFlow()
    fun showOffset() {
        Log.d("StateFlowHolder", "Offset = ${offset.value}")
    }
}




