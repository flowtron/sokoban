package de.flowtron.sokoban.state

import android.util.Log
import de.flowtron.sokoban.game.LevelData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max

class ScaleStateFlow {
    private val mutableScaleStateFlow: MutableStateFlow<Int> = MutableStateFlow(3)
    fun setScale(levelData: LevelData, scale: Int, edge: Int = 0) {
        /*
         * as we were reading StateFlows here directly
           val currentLevelData = levelData.value
           if (currentLevelData != null) {
           } else {
            mutableScaleStateFlow.value = 3
            Log.d(
                "StateFlowHolder",
                "Forcing SCALE to 3 while LevelData is NULL\nSCALE = ${mutableScaleStateFlow.value}"
            )
           }
         *
         */

        // we always want an odd scale value so the centre is established without rounding …
        // … do we really need that?
        val totalMax = max(levelData.dimensions.x, levelData.dimensions.y) // 2 +
        var saneScale = scale.coerceIn(3, totalMax)
        if (saneScale % 2 == 0) saneScale += 1
        if (saneScale > totalMax) saneScale = totalMax - 1
        Log.d("StateFlowHolder", "SCALE = $scale => $saneScale")
        mutableScaleStateFlow.value = saneScale

    }

    val scale = mutableScaleStateFlow.asStateFlow()
    fun showScale() {
        Log.d("StateFlowHolder", "Scale = ${scale.value}")
    }
}