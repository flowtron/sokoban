package de.flowtron.sokoban.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import de.flowtron.sokoban.AppDestinations.ROUTE_GAME
import de.flowtron.sokoban.AppDestinations.ROUTE_PUZZLES
import de.flowtron.sokoban.game.Coordinates
import de.flowtron.sokoban.game.LevelData
import de.flowtron.sokoban.game.LevelProgress
import de.flowtron.sokoban.game.MovementHistory
import de.flowtron.sokoban.next
import de.flowtron.sokoban.room.RoomHolder
import de.flowtron.sokoban.safeLaunch
import de.flowtron.sokoban.state.StateFlowHolder
import de.flowtron.sokoban.ui.game.InteractionControlWidget
import de.flowtron.sokoban.ui.game.InteractionMode
import de.flowtron.sokoban.ui.game.VisualDataRender
import de.flowtron.sokoban.ui.models.GameViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

/*
@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    GameScreen(Modifier(),)
}*/

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    gameViewModel: GameViewModel,// = hiltViewModel(),
    stateFlowHolder: StateFlowHolder,
    levelProgress: LevelProgress,
    navController: NavController,
    roomHolder: RoomHolder,
) {
    val coroutineScope = rememberCoroutineScope()
    val mapFinished = stateFlowHolder.mapFinishedStateFlow.finished.collectAsStateWithLifecycle()

    RenderGameScreen(coroutineScope, modifier, stateFlowHolder, levelProgress, gameViewModel, roomHolder)
    if(mapFinished.value){
        RenderFinishDialog(gameViewModel, navController, roomHolder)
    }
}

private fun setSolutionDelta(
    stateFlowHolder: StateFlowHolder,
    levelProgress: LevelProgress,
    gameViewModel: GameViewModel,
    di: Int
) {
    val current = stateFlowHolder.movementSolutionStateFlow.indexStateFlow.value
    stateFlowHolder.movementSolutionStateFlow.setIndex(current + di)
    if (stateFlowHolder.movementSolutionStateFlow.indexStateFlow.value > 0) {
        gameViewModel.triggerUpdateRoomLevel(help = true)
    }
    performPartialSolution(stateFlowHolder, levelProgress)
}

private fun performPartialSolution(stateFlowHolder: StateFlowHolder, levelProgress: LevelProgress) {
    val partialSolution = stateFlowHolder.movementSolutionStateFlow.partialSolution()

    var currentMap =
        requireNotNull(stateFlowHolder.levelOriginalStateFlow.levelOriginal.value) // start with the original map configuration
    var pusherAt = requireNotNull(currentMap.findPlayer())

    // perform every step of the partial solution onward from an original configuration
    val dondeEsta = partialSolution.toDirections()
    dondeEsta.forEach {
        //Log.i("GameScreen", "partialSolution char: $it")
        val direction = when (it) {
            'E' -> Coordinates(1, 0)
            'N' -> Coordinates(0, -1)
            'W' -> Coordinates(-1, 0)
            'S' -> Coordinates(0, 1)
            else -> Coordinates(0, 0)
        }

        val allowed = levelProgress.allowedToMove(currentMap, pusherAt, direction)
        if (!allowed) {
            Log.e(
                "GameScreen",
                "This is bad."
            ) // The history says to go, but our reality check says that's not allowed. Very bad!
        }

        val changedMap = levelProgress.performMove(currentMap, pusherAt, direction)

        stateFlowHolder.levelSolutionStateFlow.setLevelSolution(changedMap) // update the map configuration
        currentMap = changedMap // update the map reference
        pusherAt = requireNotNull(changedMap.findPlayer()) // keep pusher coordinates on hand
    }
}

private fun moveOffsetBy(stateFlowHolder: StateFlowHolder, dx: Int, dy: Int) {
    val offBy = requireNotNull(stateFlowHolder.offsetStateFlow.offset.value)
    val newOffBy = offBy.copy(x = offBy.x + dx, y = offBy.y + dy)
    stateFlowHolder.offsetStateFlow.setOffset(newOffBy)
}

private fun resetOffset(stateFlowHolder: StateFlowHolder) {
    // place pusher in the centre of the viewport, so top-left will depend on zoom-level

    //stateFlowHolder.offsetStateFlow.setOffset(…)
    //Log.d("GameScreen", "reset offset: TODO")
    val playerAt = stateFlowHolder.coordinatesStateFlow.coordinates.value
    val scaleLevel = stateFlowHolder.scaleStateFlow.scale.value
    val floorHalf = scaleLevel / 2
    requireNotNull(playerAt)
    stateFlowHolder.offsetStateFlow.setOffset(Coordinates(playerAt.x-floorHalf, playerAt.y-floorHalf))
    stateFlowHolder.offsetStateFlow.logOffset()
}

private fun onSolutionClicked(stateFlowHolder: StateFlowHolder, gameViewModel: GameViewModel) {
    val gameDataInfo = stateFlowHolder.gameDataInfoStateFlow.gameDataInfo.value
    if (gameDataInfo != null) {
        //Log.d("GameScreen", "SOLUTION may be found at ${gameDataInfo.getSolutionFilepath()}")
        val movementHistory = gameViewModel.loadSolution(gameDataInfo)
        if (movementHistory != null) {
            //Log.d("GameScreen", "You got HELP! The solution is: ${movementHistory.toDirections()}")
            stateFlowHolder.movementSolutionStateFlow.setMovementSolution(movementHistory)
            stateFlowHolder.movementSolutionStateFlow.setIndex(0)
            logLevelData(stateFlowHolder.levelDataStateFlow.levelData.value)
        } else {
            Log.d("GameScreen", "No solution found.")
        }
    }
}

fun logLevelData(levelData: LevelData?) {
    levelData?.data?.forEach { row ->
        var rowStr = ""
        row.forEach { cell ->
            rowStr += cell
        }
        Log.i("GameScreen", rowStr)
    }
}

@Composable
fun RenderFinishDialog(
    gameViewModel: GameViewModel,
    navController: NavController,
    roomHolder: RoomHolder,
) {
    var showDialog by remember { mutableStateOf(true) }
    if(showDialog){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = .5f))
                .clickable(
                    indication = null,
                    interactionSource = remember {MutableInteractionSource() }
                ) {
                    // TODO close dialog onClick
                    showDialog = false
                },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(de.flowtron.sokoban.R.string.success),
                        style = typography.titleSmall
                    )
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                gameViewModel.viewModelScope.safeLaunch {
                                    Log.d("GameScreen", "onclick for PICK PUZZLE")
                                    navController.navigate(ROUTE_PUZZLES) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            inclusive = true
                                        } // in question
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(de.flowtron.sokoban.R.string.pick_puzzle))
                        }

                        TextButton(
                            onClick = {
                                Log.d("GameScreen", "onclick for NEXT PUZZLE")
                                gameViewModel.viewModelScope.safeLaunch {
                                    val nextLevel = roomHolder.getNextLevel()
                                    var gotoRoute = ROUTE_GAME // ROUTE_PUZZLES

                                    if(nextLevel != null) {
                                        //gotoRoute += "/${nextLevel.combo}/${nextLevel.world}/${nextLevel.id}"
                                        gotoRoute += "/$nextLevel.id"
                                    }

                                    Log.d("GameScreen", "gotoRoute = '$gotoRoute'")
                                    navController.navigate(gotoRoute) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            inclusive = true
                                        } // in question
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(de.flowtron.sokoban.R.string.levels_next))
                        }
                    }
                }
            }
        }
    }else{
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember {MutableInteractionSource() }
                ) {
                    showDialog = true
                },
            contentAlignment = Alignment.Center
        ) {
        }


    }

}

@Composable
fun RenderGameScreen(
    coroutineScope: CoroutineScope,
    modifier: Modifier,
    stateFlowHolder: StateFlowHolder,
    levelProgress: LevelProgress,
    gameViewModel: GameViewModel,
    roomHolder: RoomHolder,
) {
    //TODO: swipe or click | val _ = roomHolder.getRoomStatus().movementMethod
    val dragPusherX = remember { mutableFloatStateOf(0f) }
    val dragPusherY = remember { mutableFloatStateOf(0f) }
    val swipeJob = remember { mutableStateOf<Job?>(null) }

    val dragSensitivityState =
        stateFlowHolder.dragSensitivityStateFlow.dragSensitivity.collectAsStateWithLifecycle()

    val dragThreshold = 100f

    val currentInteractionMode by stateFlowHolder.gameToolStateFlow.interactionMode.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        endSwipeAction(
                            dragPusherXState = dragPusherX,
                            dragPusherYState = dragPusherY,
                            swipeJobState = swipeJob
                        )
                    },
                    onDragEnd = {
                        endSwipeAction(
                            dragPusherXState = dragPusherX,
                            dragPusherYState = dragPusherY,
                            swipeJobState = swipeJob
                        )
                    },
                    onDragCancel = {
                        endSwipeAction(
                            dragPusherXState = dragPusherX,
                            dragPusherYState = dragPusherY,
                            swipeJobState = swipeJob
                        )
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()

                        val currentToolCanDrag =
                            currentInteractionMode == InteractionMode.MAIN_CONTROLS
                        if (currentToolCanDrag) {
                            dragPusherX.floatValue += dragAmount.x
                            dragPusherY.floatValue += dragAmount.y
                            if (abs(dragPusherX.floatValue) > dragThreshold || abs(dragPusherY.floatValue) > dragThreshold) {
                                if (swipeJob.value?.isActive != true) {
                                    swipeJob.value = coroutineScope.safeLaunch {
                                        performRepeatedSwipeAction(
                                            dragPusherX.floatValue,
                                            dragPusherY.floatValue,
                                            dragSensitivityState.value,
                                            stateFlowHolder,
                                            gameViewModel,
                                        )
                                    }
                                }
                            }// else { swipeJob?.cancel() } // GEMINI says this could make it stuttery if user hovers around threshold
                        }

                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            VisualDataRender(stateFlowHolder, roomHolder, gameViewModel)
        }

        val coordinateState = stateFlowHolder.coordinatesStateFlow.coordinates
        val nowAtState = coordinateState.collectAsStateWithLifecycle()
        val nowAt = nowAtState.value
        requireNotNull(nowAt)

        val currentRenderer = stateFlowHolder.renderStateFlow.renderer.collectAsStateWithLifecycle()

        InteractionControlWidget(
            stateFlowHolder = stateFlowHolder,
            levelProgress = levelProgress,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(all = 0.dp),
            onSolutionClick = { onSolutionClicked(stateFlowHolder, gameViewModel) },
            onRenderClick = { stateFlowHolder.renderStateFlow.setRenderer(currentRenderer.value.next()) },
            onZoomClick = {},
            onHistoryClick = {},

            onLeftClickOffset = { moveOffsetBy(stateFlowHolder, -1, 0) },
            onRightClickOffset = { moveOffsetBy(stateFlowHolder, +1, 0) },
            onUpClickOffset = { moveOffsetBy(stateFlowHolder, 0, -1) },
            onCenterClickOffset = { resetOffset(stateFlowHolder) },
            onDownClickOffset = { moveOffsetBy(stateFlowHolder, 0, +1) },

            onLeftClickSolution = {
                setSolutionDelta(
                    stateFlowHolder,
                    levelProgress,
                    gameViewModel,
                    -1
                )
            },
            onRightClickSolution = {
                setSolutionDelta(
                    stateFlowHolder,
                    levelProgress,
                    gameViewModel,
                    +1
                )
            },
            onDeleteHistory = {
                gameViewModel.viewModelScope.safeLaunch {
                    gameViewModel.updateRoomLevel(done = false, history = MovementHistory(emptyList()), deleteHistory = true) // help should stay
                    Log.i("GameScreen", "History deleted")
                    stateFlowHolder.mapFinishedStateFlow.setMapFinished(false)
                    stateFlowHolder.gameToolStateFlow.setGameTool(InteractionMode.MAIN_CONTROLS)
                }
            },
        )
    }
}

private fun handleSwipeAction(
    deltaX: Float,
    deltaY: Float,
    stateFlowHolder: StateFlowHolder,
    gameViewModel: GameViewModel
) {
    //Log.v("GameScreen", "handleSwipeAction: deltaX=$deltaX, deltaY=$deltaY")
    val currentLocation = stateFlowHolder.coordinatesStateFlow.coordinates.value
    if (currentLocation == null) {
        Log.w("GameScreen", "Cannot handle swipe, current location is null.")
        return
    }

    val direction = if (abs(deltaX) > abs(deltaY)) {
        if (deltaX > 0) Coordinates(1, 0) else Coordinates(-1, 0)
    } else {
        if (deltaY > 0) Coordinates(0, 1) else Coordinates(0, -1)
    }

    if (stateFlowHolder.mapFinishedStateFlow.finished.value) {
        Log.d("GameScreen", "Level is finished, swipe ignored.")
    } else {
        if (gameViewModel.allowedToMove(currentLocation, direction)) {
            gameViewModel.performMove(currentLocation, direction)
        } else {
            Log.d(
                "GameScreen",
                "Move not allowed in direction: $direction from $currentLocation"
            )
        }
    }
}

private suspend fun performRepeatedSwipeAction(
    initialDeltaX: Float,
    initialDeltaY: Float,
    dragRepeatIntervalMillis: Long,
    stateFlowHolder: StateFlowHolder, // Pass parameters needed by handleSwipeAction
    gameViewModel: GameViewModel     // or pass handleSwipeAction directly
) {
    // This assumes the coroutine calling this is active.
    // The `isActive` check from `kotlinx.coroutines.isActive` is implicitly
    // handled by cancellable suspending functions like `delay`.
    // If the coroutine is cancelled, delay will throw CancellationException.
    while (true) { // Loop will be broken by coroutine cancellation
        handleSwipeAction(initialDeltaX, initialDeltaY, stateFlowHolder, gameViewModel)
        delay(dragRepeatIntervalMillis.milliseconds)
    }
}

private fun endSwipeAction(
    dragPusherXState: MutableState<Float>,
    dragPusherYState: MutableState<Float>,
    swipeJobState: MutableState<Job?>
) {
    dragPusherXState.value = 0f
    dragPusherYState.value = 0f
    swipeJobState.value?.cancel()
    swipeJobState.value = null // Good practice to nullify after cancelling
}
