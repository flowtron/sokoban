package de.flowtron.sokoban.state

import android.util.Log
import de.flowtron.sokoban.state.Renderer.DRAW
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MapFinishedStateFlow {
    private val mutableMapFinishedStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    fun setMapFinished(finished: Boolean) {
        mutableMapFinishedStateFlow.value = finished
    }

    val finished = mutableMapFinishedStateFlow.asStateFlow()
    fun showMapFinished() {
        Log.d("StateFlowHolder", "MapFinished = ${finished.value}")
    }
}