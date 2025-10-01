package de.flowtron.sokoban.ui.settings

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.Start
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.flowtron.sokoban.DRAG_SENSITIVITY_MAXIMUM
import de.flowtron.sokoban.state.StateFlowHolder

@Composable
fun DragSensitivity(
    stateFlowHolder: StateFlowHolder,
) {
    val modifier = Modifier.fillMaxWidth()
    val dragSensitivityState = stateFlowHolder.dragSensitivityStateFlow.dragSensitivity.collectAsStateWithLifecycle()
    Row(
        modifier = modifier
            .padding(vertical = 8.dp, horizontal = 16.dp),
    ) {
        Column(
            modifier = modifier,
            horizontalAlignment = CenterHorizontally,
        ) {
            Text(
                modifier = Modifier.align(alignment = Start),
                fontWeight = Bold,
                text = "Drag Sensitivity"
            )
            Slider(
                value = dragSensitivityState.value.toFloat(),
                onValueChange = { newValue ->
                    val longValue = newValue.toLong()
                    val flatValue = longValue - longValue % 10
                    Log.d("DragSensitivity", "newValue = $newValue, flatValue = $flatValue")

                    //val clampedValue = (flatValue - (flatValue % 50)).coerceAtLeast(50)
                    val clampedValue = flatValue.coerceAtLeast(1).coerceAtMost(2500)
                    Log.d("DragSensitivity", "clampedValue = $clampedValue")

                    stateFlowHolder.dragSensitivityStateFlow.setDragSensitivity(clampedValue)
                },
                //valueRange = 100f..DRAG_SENSITIVITY_MAXIMUM,
                //steps = 50,
                valueRange = 1f..DRAG_SENSITIVITY_MAXIMUM,
                steps = 250,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(fontWeight = Bold, text = dragSensitivityState.value.toString())
        }
    }
}