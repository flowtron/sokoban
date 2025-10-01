package de.flowtron.sokoban.state

import android.util.Log
import de.flowtron.sokoban.ui.game.InteractionMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameToolStateFlow {
    private val mutableGameToolStateFlow: MutableStateFlow<InteractionMode> = MutableStateFlow(InteractionMode.MAIN_CONTROLS)
    fun setGameTool(interactionMode: InteractionMode) {
        mutableGameToolStateFlow.value = interactionMode
    }

    val interactionMode = mutableGameToolStateFlow.asStateFlow()
    fun showGameTool() {
        Log.d("StateFlowHolder", "GameTool = ${interactionMode.value}")
    }
}