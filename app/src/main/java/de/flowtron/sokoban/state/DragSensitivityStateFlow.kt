package de.flowtron.sokoban.state

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class DragSensitivityStateFlow {
    // hardcoded default 250L, we want it to use the RoomDB - is this initialised here yet?
    private val mutableDragSensitivityStateFlow: MutableStateFlow<Long> = MutableStateFlow(250L)
    fun setDragSensitivity(sensitivity: Long) {
        mutableDragSensitivityStateFlow.value = sensitivity
    }

    val dragSensitivity = mutableDragSensitivityStateFlow.asStateFlow()
    fun showDragSensitivity() {
        Log.d("StateFlowHolder", "DragSensitivity = ${dragSensitivity.value}")
    }
}