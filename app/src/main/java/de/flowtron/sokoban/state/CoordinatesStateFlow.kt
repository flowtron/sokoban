package de.flowtron.sokoban.state

import android.util.Log
import de.flowtron.sokoban.game.Coordinates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CoordinatesStateFlow {
    private val mutableCoordinatesStateFlow: MutableStateFlow<Coordinates?> = MutableStateFlow(Coordinates(0,0))//MutableStateFlow(null)
    fun setCoordinates(coordinates: Coordinates?) {
        mutableCoordinatesStateFlow.value = coordinates
    }

    val coordinates = mutableCoordinatesStateFlow.asStateFlow()
    fun showCoordinates() {
        Log.d("StateFlowHolder", "Coordinates = ${coordinates.value}")
    }
}