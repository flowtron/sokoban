package de.flowtron.sokoban.ui.game

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleLeft
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.flowtron.sokoban.game.LevelProgress
import de.flowtron.sokoban.state.StateFlowHolder

/*
    onToolAClick: () -> Unit,
    onToolBClick: () -> Unit,

 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractionControlWidget(
    stateFlowHolder: StateFlowHolder,
    levelProgress: LevelProgress?,
    modifier: Modifier = Modifier,
    // Add callbacks for all actions from the different control sets .. and at some point consolidate
    onSolutionClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onRenderClick: () -> Unit,
    onZoomClick: () -> Unit,
    onLeftClickOffset: () -> Unit,
    onUpClickOffset: () -> Unit,
    onCenterClickOffset: () -> Unit,
    onDownClickOffset: () -> Unit,
    onRightClickOffset: () -> Unit,
    onLeftClickSolution: () -> Unit,
    onRightClickSolution: () -> Unit,
) {
    val basicCurrentMode by stateFlowHolder.gameToolStateFlow.interactionMode.collectAsStateWithLifecycle()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.animateContentSize()) {
                UiControlsForMode(
                    stateFlowHolder,
                    basicCurrentMode,
                    onLeftClickOffset,
                    onUpClickOffset,
                    onCenterClickOffset,
                    onDownClickOffset,
                    onRightClickOffset,
                    levelProgress,
                    onSolutionClick,
                    onLeftClickSolution,
                    onRightClickSolution
                )
            }
            UiControlsForModeChange(stateFlowHolder, basicCurrentMode)
        }
    }
}

private fun cycleMode(stateFlowHolder: StateFlowHolder, currentMode: InteractionMode, cycleStep: Int = 1) {
    val cycledMode = (currentMode.ordinal + cycleStep + InteractionMode.entries.size) % InteractionMode.entries.size
    val nextInteractionMode = InteractionMode.entries[cycledMode]
    stateFlowHolder.gameToolStateFlow.setGameTool(nextInteractionMode)
    Log.d("InteractionControlWidget", "Mode changed by $cycleStep to ${stateFlowHolder.gameToolStateFlow.interactionMode}")
}

@Composable
private fun UiControlsForModeChange(stateFlowHolder: StateFlowHolder, currentMode: InteractionMode) {
    val rowMinSize = 128.dp
    val rowModifier = Modifier
        .padding(bottom = 8.dp, top = 4.dp)
        .height(40.dp)
        .scale(0.8f)
        .defaultMinSize(rowMinSize)

    //.border(width = BorderStroke(2.dp, Color.TRANSPARENT), color = Color.TRANSPARENT, shape = RectangleShape)
    // Icons
    // Icons.Filled.Refresh
    Row {
        OutlinedButton(
            onClick = { cycleMode(stateFlowHolder, currentMode, -1) },
            modifier = rowModifier
        ) {
            Icon(Icons.Filled.ArrowCircleLeft, contentDescription = "Cycle Mode")
            Spacer(Modifier.width(4.dp))
            Text( interactionModeName( stepInteractionMode(currentMode, -1) ) )
        }

        //Text( interactionModeName( currentMode ) )
        OutlinedButton(
            onClick = { },
            modifier = rowModifier
        ) {
            Text( interactionModeName( currentMode ) )
        }

        OutlinedButton(
            onClick = { cycleMode(stateFlowHolder, currentMode, 1) },
            modifier = rowModifier
        ) {
            Text( interactionModeName( stepInteractionMode(currentMode, +1) ) )
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Filled.ArrowCircleRight, contentDescription = "Cycle Mode")
        }
    }
}

@Composable
private fun UiControlsForMode(
    stateFlowHolder: StateFlowHolder,
    currentMode: InteractionMode,
    onLeftClickOffset: () -> Unit,
    onUpClickOffset: () -> Unit,
    onCenterClickOffset: () -> Unit,
    onDownClickOffset: () -> Unit,
    onRightClickOffset: () -> Unit,
    levelProgress: LevelProgress?,
    onSolutionClick: () -> Unit,
    onLeftClickSolution: () -> Unit,
    onRightClickSolution: () -> Unit
) {
    when (currentMode) {
        InteractionMode.MAIN_CONTROLS -> MainInteractionControls(
            onSolutionClick = {
                stateFlowHolder.gameToolStateFlow.setGameTool(
                    InteractionMode.SOLUTION_CONTROLS
                )
            },
            onHistoryClick = {
                stateFlowHolder.gameToolStateFlow.setGameTool(
                    InteractionMode.HISTORY_CONTROLS
                )
            },
            onRenderClick = {
                stateFlowHolder.gameToolStateFlow.setGameTool(
                    InteractionMode.MAIN_CONTROLS
                )
            },
            onZoomClick = {
                stateFlowHolder.gameToolStateFlow.setGameTool(
                    InteractionMode.ZOOM_CONTROLS
                )
            },
            onLeftClick = onLeftClickOffset,
            onUpClick = onUpClickOffset,
            onCenterClick = onCenterClickOffset,
            onDownClick = onDownClickOffset,
            onRightClick = onRightClickOffset,
        )

        InteractionMode.ZOOM_CONTROLS -> ZoomControls(stateFlowHolder)

        InteractionMode.HISTORY_CONTROLS -> HistoryControls(stateFlowHolder, levelProgress)

        InteractionMode.SOLUTION_CONTROLS -> SolutionControls(
            stateFlowHolder,
            onSolutionClick,
            onLeftClickSolution,
            onRightClickSolution,
        )
    }
}

private fun interactionModeName(currentMode: InteractionMode): String {
    return when (currentMode) {
        InteractionMode.MAIN_CONTROLS -> "Controls"
        InteractionMode.ZOOM_CONTROLS -> "Zoom"
        InteractionMode.HISTORY_CONTROLS -> "History"
        InteractionMode.SOLUTION_CONTROLS -> "Solution"
    }
}

private fun stepInteractionMode(currentMode: InteractionMode, step: Int = 1): InteractionMode {
    val currentModeOrdinal = currentMode.ordinal
    val setSize = InteractionMode.entries.size
    val steppedModeOrdinal = ( currentModeOrdinal + step + setSize) % setSize
    return InteractionMode.entries[steppedModeOrdinal]
}


/**
 * InteractionMode.MAIN_CONTROLS -> InteractionMode.ZOOM_CONTROLS
 *             InteractionMode.ZOOM_CONTROLS -> InteractionMode.HISTORY_CONTROLS
 *             InteractionMode.HISTORY_CONTROLS -> InteractionMode.SOLUTION_CONTROLS
 *             InteractionMode.SOLUTION_CONTROLS -> InteractionMode.MAIN_CONTROLS
 */
/*
 * the big question is if the button shows current or upcoming combo
when (currentMode) {
                        InteractionMode.MAIN_CONTROLS -> "Controls"
                        InteractionMode.ZOOM_CONTROLS -> "Zoom"
                        InteractionMode.SOLUTION_CONTROLS -> "Solution"
                    }
 */

@Preview(showBackground = true)
@Composable
fun InteractionControlWidgetPreview(stateFlowHolder: StateFlowHolder = StateFlowHolder()) {//, levelProgress: LevelProgress = LevelProgress(stateFlowHolder, toastHandler = ToastHandler(@ApplicationContext))) {
    InteractionControlWidget(
        stateFlowHolder = stateFlowHolder,
        levelProgress = null,
        onSolutionClick = {},
        onHistoryClick = {},
        onRenderClick = {},
        onZoomClick = {},
        onLeftClickOffset = {},
        onUpClickOffset = {},
        onDownClickOffset = {},
        onCenterClickOffset = {},
        onRightClickOffset = {},
        onLeftClickSolution = {},
        onRightClickSolution = {},


    )
// onSliderSolution = {},
// onToolAClick = {},
// onToolBClick = {}
}