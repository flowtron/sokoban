package de.flowtron.sokoban.ui.game

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BlurCircular
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.flowtron.sokoban.game.LevelProgress
import de.flowtron.sokoban.game.MovementHistory
import de.flowtron.sokoban.state.StateFlowHolder
import kotlin.math.max

@Composable
fun MainInteractionControls(
    onSolutionClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onRenderClick: () -> Unit,
    onZoomClick: () -> Unit,
    onLeftClick: () -> Unit,
    onUpClick: () -> Unit,
    onCenterClick: () -> Unit,
    onDownClick: () -> Unit,
    onRightClick: () -> Unit
) {
    Column {
        ActionSwitcher(onSolutionClick, onHistoryClick, onRenderClick, onZoomClick)
        CoordinateSteppers(onLeftClick, onUpClick, onCenterClick, onDownClick, onRightClick)
    }
}

private fun Modifier.interactionModifier() = this
    .fillMaxWidth()
    .padding(vertical = 8.dp, horizontal = 16.dp)

@Composable
fun ActionSwitcher(
    onSolutionClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onRenderClick: () -> Unit,
    onZoomClick: () -> Unit
) {
    val modifier = Modifier.padding(horizontal = 4.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onSolutionClick, modifier, Icons.Filled.Lightbulb, "Solution")
        IconButton(onHistoryClick, modifier, Icons.Filled.History, "History")
        IconButton(onRenderClick, modifier, Icons.Filled.Draw, "Renderer")
        IconButton(onZoomClick, modifier, Icons.Filled.ZoomIn, "Zoom")
    }
}

@Composable
fun CoordinateSteppers(
    onLeftClick: () -> Unit,
    onUpClick: () -> Unit,
    onCenterClick: () -> Unit,
    onDownClick: () -> Unit,
    onRightClick: () -> Unit,
) {
    Row(
        modifier = Modifier.interactionModifier(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val btnModifier = Modifier.weight(1f) // USELESS .. if all are the same
        // ArrowBack/ArrowForward could be NavigateBack/NavigateNext .. but that name is "wrong"
        IconButton(onLeftClick, btnModifier, Icons.Filled.ArrowBack, "Left")
        IconButton(onUpClick, btnModifier, Icons.Filled.ArrowUpward, "Up")
        IconButton(
            onClick = onCenterClick,
            btnModifier,
            imageVector = Icons.Filled.BlurCircular,
            "Center"
        )
        IconButton(onDownClick, btnModifier, Icons.Filled.ArrowDownward, "Down")
        IconButton(onRightClick, btnModifier, Icons.Filled.ArrowForward, "Right")
    }
}

@Composable
private fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier,
    imageVector: ImageVector,
    contentDescription: String
) {
    Button(onClick = onClick, modifier = modifier) {
        Icon(imageVector = imageVector, contentDescription = contentDescription)
    }
}

@Composable
fun ZoomControls(
    stateFlowHolder: StateFlowHolder,
) {
    val sliderPosition = stateFlowHolder.scaleStateFlow.scale.collectAsStateWithLifecycle()
    val levelDataState = stateFlowHolder.levelDataStateFlow.levelData.collectAsStateWithLifecycle()
    val scaleState = stateFlowHolder.scaleStateFlow.scale.collectAsStateWithLifecycle()
    Row(
        modifier = Modifier.interactionModifier(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val levelData = levelDataState.value
        requireNotNull(levelData)
        val maxRange = max(levelData.dimensions.x, levelData.dimensions.y).toFloat()
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Slider(
                value = sliderPosition.value.toFloat(),
                onValueChange = { newValue ->
                    stateFlowHolder.scaleStateFlow.setScale(levelData, newValue.toInt())
                },
                valueRange = 3f..maxRange,
                steps = maxRange.toInt() - 3,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(scaleState.value.toString())
        }
    }
}

@Composable
fun HistoryControls(
    stateFlowHolder: StateFlowHolder,
    levelProgress: LevelProgress?,
    onDeleteHistory: () -> Unit
) {
    val historyMovements =
        stateFlowHolder.movementHistoryStateFlow.movementHistory.collectAsStateWithLifecycle()
    val maxIndex = historyMovements.value.data.size
    val maxRange = maxIndex.toFloat()
    val historyPosition =
        stateFlowHolder.movementHistoryStateFlow.indexStateFlow.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    Column {
        // SINGLE STEPS
        Row(
            modifier = Modifier.interactionModifier(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Button(
                onClick = {
                    stepThroughHistory(
                        stateFlowHolder,
                        levelProgress,
                        -1
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Left")
            }
            Button(
                onClick = {
                    showDialog = true
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete")
            }
            Button(
                onClick = {
                    stepThroughHistory(
                        stateFlowHolder,
                        levelProgress,
                        +1
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.ArrowForward, contentDescription = "Right")
            }
        }
        // INDEX SLIDER
        Row(
            modifier = Modifier.interactionModifier(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Slider(
                    value = historyPosition.value.toFloat(),
                    onValueChange = {
                        //onSliderSolution(it)
                        Log.d("HistoryControls", "onValueChange: $it")
                    },
                    valueRange = 0f..maxRange,
                    steps = maxRange.toInt(),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Text(historyPosition.value.toString())
            }

        }
    }

    if (showDialog) {
        AreYouSureDialog(
            onDismissRequest = { showDialog = false },
            onConfirmation = {

                showDialog = false
                // TODO: (compare below) we should have a spinner shown until we get confirmation that the history was deleted

                Log.d("HistoryControls", "History delete confirmed!")

                stateFlowHolder.movementHistoryStateFlow.setMovementHistory(
                    MovementHistory(
                        emptyList()
                    )
                )
                stateFlowHolder.levelDataStateFlow.setLevelData(stateFlowHolder.levelOriginalStateFlow.levelOriginal.value)
                val cleanLevelData =
                    requireNotNull(stateFlowHolder.levelDataStateFlow.levelData.value)
                stateFlowHolder.coordinatesStateFlow.setCoordinates(cleanLevelData.findPlayer())

                onDeleteHistory()
                // TODO: (compare above) wait for confirmation, then hide the spinner
            },
            dialogTitle = "Delete History",
            dialogText = "Are you sure you want to delete the history?"
        )
    }
}

fun stepThroughHistory(
    stateFlowHolder: StateFlowHolder,
    levelProgress: LevelProgress?,
    delta: Int
) {
    /* FIXME:
     * the state changes fine already, but the roomLevel value for history is not changed ..
     * .. which is fine for going forward again.
     * But we need to fix the history to whatever step we may start walking about again!
     */
    if (levelProgress != null) {
        stateFlowHolder.movementHistoryStateFlow.stepIndex(delta)
        val partialHistory = stateFlowHolder.movementHistoryStateFlow.partialHistory()

        val origMap = requireNotNull(stateFlowHolder.levelOriginalStateFlow.levelOriginal.value)
        val changedMap = levelProgress.performHistory(origMap, partialHistory)
        stateFlowHolder.levelDataStateFlow.setLevelData(changedMap)
        stateFlowHolder.coordinatesStateFlow.setCoordinates(changedMap.findPlayer())

        Log.d(
            "HistoryControls",
            "partial history: ${partialHistory.toDirections()} with player at ${stateFlowHolder.coordinatesStateFlow.coordinates.value} and level data $changedMap"
        )
    } else {
        Log.d("HistoryControls", "without LEVEL_PROGRESS we can't rewrite HISTORY")
    }
}

@Composable
fun AreYouSureDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
) {
    AlertDialog(
        title = { Text(text = dialogTitle) },
        text = { Text(text = dialogText) },
        onDismissRequest = { onDismissRequest() },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Dismiss")
            }
        }
    )
}

@Composable
fun SolutionControls(
    stateFlowHolder: StateFlowHolder,
    onActionClick: () -> Unit,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit,
) {
    val historyMovements =
        stateFlowHolder.movementSolutionStateFlow.movementSolution.collectAsStateWithLifecycle()
    val maxIndex = historyMovements.value.data.size
    val maxRange = maxIndex.toFloat()
    val historyPosition =
        stateFlowHolder.movementSolutionStateFlow.indexStateFlow.collectAsStateWithLifecycle()

    Column {
        if (maxIndex == 0) {
            // ACTION BUTTON
            Row(
                modifier = Modifier.interactionModifier(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onActionClick, modifier = Modifier.fillMaxWidth(0.5f)) {
                    Text("SOLUTION")
                }
            }
        }
        if (maxIndex > 0) {
            // SINGLE STEPS
            Row(
                modifier = Modifier.interactionModifier(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Button(onClick = onLeftClick, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Left")
                }
                Button(onClick = onRightClick, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.ArrowForward, contentDescription = "Right")
                }
            }
            // INDEX SLIDER
            Row(
                modifier = Modifier.interactionModifier(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Slider(
                        value = historyPosition.value.toFloat(),
                        onValueChange = {
                            //onSliderSolution(it)
                            Log.d("SolutionControls", "onValueChange: $it")
                        },
                        valueRange = 0f..maxRange,
                        steps = maxRange.toInt(),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Text(historyPosition.value.toString())
                }

            }
        }
    }
}

// Previews for individual control sets (optional but helpful)
@Preview(showBackground = true, backgroundColor = 0xFFCCCCCC)
@Composable
fun MainInteractionControlsPreview() {
    MainInteractionControls(
        onSolutionClick = {},
        onHistoryClick = {},
        onRenderClick = {},
        onZoomClick = {},
        onLeftClick = {},
        onUpClick = {},
        onCenterClick = {},
        onDownClick = {},
        onRightClick = {})
}