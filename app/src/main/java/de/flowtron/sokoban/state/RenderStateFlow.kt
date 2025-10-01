package de.flowtron.sokoban.state

import android.util.Log
import de.flowtron.sokoban.state.Renderer.DRAW
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class RenderStateFlow {
    private val mutableRendererStateFlow: MutableStateFlow<Renderer> = MutableStateFlow(DRAW)
    fun setRenderer(renderer: Renderer) {
        mutableRendererStateFlow.value = renderer
    }

    val renderer = mutableRendererStateFlow.asStateFlow()
    fun showRenderer() {
        Log.d("StateFlowHolder", "Renderer = ${renderer.value}")
    }
}