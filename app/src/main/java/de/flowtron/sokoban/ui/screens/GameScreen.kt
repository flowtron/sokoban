package de.flowtron.sokoban.ui.screens

import android.util.Log
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.flowtron.sokoban.game.Coordinates
import de.flowtron.sokoban.game.LevelData
import de.flowtron.sokoban.game.LevelProgress
import de.flowtron.sokoban.next
import de.flowtron.sokoban.safeLaunch
import de.flowtron.sokoban.state.StateFlowHolder
import de.flowtron.sokoban.ui.game.InformationDisplay
import de.flowtron.sokoban.ui.game.InteractionControlWidget
import de.flowtron.sokoban.ui.game.VisualDataRender
import de.flowtron.sokoban.ui.models.GameViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.delay
import kotlin.math.abs

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() { GameScreen() }

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    gameViewModel: GameViewModel = hiltViewModel(),
    stateFlowHolder: StateFlowHolder? = null, // FIXME: does this really need to be nullable?
    levelProgress: LevelProgress? = null, // FIXME: same
) {
    val coroutineScope = rememberCoroutineScope()
    //val coroutineContext = coroutineScope.coroutineContext
    requireNotNull(stateFlowHolder) // see above re:nullable - also below re: default composable
    requireNotNull(levelProgress)
    RenderGameScreen( coroutineScope, modifier, stateFlowHolder, levelProgress, gameViewModel)
}

private fun setSolutionDelta(stateFlowHolder: StateFlowHolder, levelProgress: LevelProgress, di: Int) {
    val current = stateFlowHolder.movementSolutionStateFlow.indexStateFlow.value
    stateFlowHolder.movementSolutionStateFlow.setIndex(current + di)
    performPartialSolution(stateFlowHolder, levelProgress)
}

//private fun setSolutionIndex(stateFlowHolder: StateFlowHolder, levelProgress: LevelProgress, sliderValue: Float) {
//    val sliderIndex = sliderValue.toInt()
//    val solutionMovementHistory =
//        requireNotNull(stateFlowHolder.movementSolutionStateFlow.movementSolution.value)
//
//    val current = stateFlowHolder.movementSolutionStateFlow.indexStateFlow.value
//    val maxIndex = solutionMovementHistory.data.size - 1
//    val nextIndex = current + sliderIndex
//    val upperBound = maxIndex.coerceAtLeast(0)
//    val next = nextIndex.coerceAtLeast(0).coerceAtMost(upperBound)
//
//    //Log.v("GameScreen", "setting index of solution movement history to [$next]")
//    stateFlowHolder.movementSolutionStateFlow.setIndex(next)
//    performPartialSolution(stateFlowHolder, levelProgress)
//}

//private fun setHistoryIndex(stateFlowHolder: StateFlowHolder, levelProgress: LevelProgress, sliderValue: Float) {
//    val sliderIndex = sliderValue.toInt()
//    val historyMovementHistory =
//        requireNotNull(stateFlowHolder.movementHistoryStateFlow.movementHistory.value)
//
//    val current = stateFlowHolder.movementHistoryStateFlow.indexStateFlow.value
//    val maxIndex = historyMovementHistory.data.size - 1
//    val nextIndex = current + sliderIndex
//    val upperBound = maxIndex.coerceAtLeast(0)
//    val next = nextIndex.coerceAtLeast(0).coerceAtMost(upperBound)
//    stateFlowHolder.movementHistoryStateFlow.setIndex(next)
//    performPartialHistory(stateFlowHolder, levelProgress)
//}

private fun performPartialSolution(stateFlowHolder: StateFlowHolder, levelProgress: LevelProgress) {
    val partialSolution = stateFlowHolder.movementSolutionStateFlow.partialSolution()

    var currentMap = requireNotNull(stateFlowHolder.levelOriginalStateFlow.levelOriginal.value) // start with the original map configuration
    var pusherAt = requireNotNull(currentMap.findPlayer())

    // perform every step of the partial solution onward from an original configuration
    val dondeEsta = partialSolution.toDirections()
    dondeEsta.forEach {
        //Log.i("GameScreen", "partialSolution char: $it")
        val direction = when(it) {
            'E' -> Coordinates(1,0)
            'N' -> Coordinates(0,-1)
            'W' -> Coordinates(-1,0)
            'S' -> Coordinates(0,1)
            else -> Coordinates(0, 0)
        }

        val allowed = levelProgress.allowedToMove(currentMap, pusherAt, direction)
        if( !allowed ) {
            Log.e("GameScreen", "This is bad.") // The history says to go, but our reality check says that's not allowed. Very bad!
        }

        val changedMap = levelProgress.performMove(currentMap, pusherAt, direction)

        stateFlowHolder.levelSolutionStateFlow.setLevelSolution(changedMap) // update the map configuration
        currentMap = changedMap // update the map reference
        pusherAt = requireNotNull(changedMap.findPlayer()) // keep pusher coordinates on hand
    }
}

//private fun performPartialHistory(stateFlowHolder: StateFlowHolder, levelProgress: LevelProgress) {
//    val partialHistory = stateFlowHolder.movementHistoryStateFlow.partialHistory()
//
//    var currentMap = requireNotNull(stateFlowHolder.levelOriginalStateFlow.levelOriginal.value) // start with the original map configuration
//    var pusherAt = requireNotNull(currentMap.findPlayer())
//
//    // perform every step of the partial solution onward from an original configuration
//    val dondeEsta = partialHistory.toDirections()
//    dondeEsta.forEach {
//        //Log.i("GameScreen", "partialSolution char: $it")
//        val direction = when(it) {
//            'E' -> Coordinates(1,0)
//            'N' -> Coordinates(0,-1)
//            'W' -> Coordinates(-1,0)
//            'S' -> Coordinates(0,1)
//            else -> Coordinates(0, 0)
//        }
//
//        val allowed = levelProgress.allowedToMove(currentMap, pusherAt, direction)
//        if( !allowed ) {
//            Log.e("GameScreen", "This is bad.") // The history says to go, but our reality check says that's not allowed. Very bad!
//        }
//
//        val changedMap = levelProgress.performMove(currentMap, pusherAt, direction)
//
//        stateFlowHolder.levelDataStateFlow.setLevelData(changedMap) // update the map configuration
//        //stateFlowHolder. .set (changedMap) // update the map configuration
//        currentMap = changedMap // update the map reference
//        pusherAt = requireNotNull(changedMap.findPlayer()) // keep pusher coordinates on hand
//    }
//}

private fun setOffsetBy(stateFlowHolder: StateFlowHolder, dx: Int, dy: Int) {
    val offBy = requireNotNull(stateFlowHolder.offsetStateFlow.offset.value)
    val newOffBy = offBy.copy(x = offBy.x + dx, y = offBy.y + dy)
    stateFlowHolder.offsetStateFlow.setOffset(newOffBy)
}

private fun resetOffset(stateFlowHolder: StateFlowHolder) {
    // place pusher in the center of the viewport, so top-left will depend on zoom-level
    //stateFlowHolder.offsetStateFlow.setOffset()
    Log.d("GameScreen", "reset offset: TODO")
}

// 321_123
//    ^
// P  B  S
private fun getMovementStream(stream: String, index: Int): AnnotatedString {
    val windowSize = 3
    val beginIndex = (index - windowSize).coerceAtLeast(0)
    val elongateRight = if(beginIndex < windowSize){
        windowSize - beginIndex
    } else {
        0
    }
    val endIndex =
        (index + windowSize + 1 + elongateRight).coerceAtMost(stream.length)
    val result: AnnotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(color = Color.Blue)) {
            // prefix
            if (beginIndex < index) {
                append(stream.substring(beginIndex, index))
            }
            // bold
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(stream[index].toString())
            }
            // suffix
            if (index + 1 < endIndex) {
                append(stream.substring(index + 1, endIndex))
            }
        }
    }
    return result
}

private fun onSolutionClicked(stateFlowHolder: StateFlowHolder, gameViewModel: GameViewModel) {
    val gameDataInfo = stateFlowHolder.gameDataInfoStateFlow.gameDataInfo.value
    if (gameDataInfo != null) {
        Log.d("GameScreen", "SOLUTION may be found at ${gameDataInfo.getSolutionFilepath()}")
        val movementHistory =
            gameViewModel.loadSolution(gameDataInfo)
        if (movementHistory != null) {
            //Log.d("GameScreen", "You got HELP! The solution is: ${movementHistory.toDirections()}")
            stateFlowHolder.movementSolutionStateFlow.setMovementSolution(movementHistory)
            stateFlowHolder.movementSolutionStateFlow.setIndex(0)
        } else {
            Log.d("GameScreen", "No solution found.")
        }
    }
}

fun logLevelData(levelData: LevelData) {
    levelData.data.forEach { row ->
        var rowStr = ""
        row.forEach { cell ->
            rowStr += cell
        }
        Log.i("VisualDataRender", rowStr)
    }
}

@Composable
fun RenderGameScreen(
    coroutineScope: CoroutineScope,
    modifier: Modifier,
    stateFlowHolder: StateFlowHolder,
    levelProgress: LevelProgress,
    gameViewModel: GameViewModel,
) {
    // NOTICE:
    // difference between
    // var abc by remember { mutableStateOf(0) }
    // val abc = remember { mutableStateOf(0) }
    val dragPusherX = remember { mutableFloatStateOf(0f) }
    val dragPusherY = remember { mutableFloatStateOf(0f) }
    val swipeJob = remember { mutableStateOf<Job?>(null) }

    val dragSensitivityState =
        stateFlowHolder.dragSensitivityStateFlow.dragSensitivity.collectAsStateWithLifecycle()

    val dragThreshold = 100f

    /*
    fun handleSwipe(deltaX: Float, deltaY: Float) {
        Log.v("GameScreen", "handleSwipe: deltaX=$deltaX, deltaY=$deltaY")
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
            // Optionally show a Toast or some feedback
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

    suspend fun performRepeatedSwipe(
        initialDeltaX: Float,
        initialDeltaY: Float,
        dragRepeatIntervalMillis: Long,
    ) {
        while (isActive) {
            handleSwipe(initialDeltaX, initialDeltaY)
            delay(dragRepeatIntervalMillis)
        }
    }

    fun endSwipe() {
        dragPusherX = 0f
        dragPusherY = 0f;
        swipeJob?.cancel()
    }
    */


    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { endSwipeAction(
                        dragPusherXState = dragPusherX,
                        dragPusherYState = dragPusherY,
                        swipeJobState = swipeJob
                    ) },
                    onDragEnd = { endSwipeAction(
                        dragPusherXState = dragPusherX,
                        dragPusherYState = dragPusherY,
                        swipeJobState = swipeJob
                    ) },
                    onDragCancel = { endSwipeAction(
                        dragPusherXState = dragPusherX,
                        dragPusherYState = dragPusherY,
                        swipeJobState = swipeJob
                    ) },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragPusherX.floatValue += dragAmount.x
                        dragPusherY.floatValue += dragAmount.y
                        if (abs(dragPusherX.value) > dragThreshold || abs(dragPusherY.value) > dragThreshold) {
                            if (swipeJob.value?.isActive != true) {
                                swipeJob.value = coroutineScope.safeLaunch {
                                    performRepeatedSwipeAction(
                                        dragPusherX.value,
                                        dragPusherY.value,
                                        dragSensitivityState.value,
                                        stateFlowHolder,
                                        gameViewModel,
                                    )
                                }
                            }
                        }// else { swipeJob?.cancel() } // GEMINI says this could make it stuttery if user hovers around threshold
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
            //InformationDisplay(stateFlowHolder, gameViewModel, ::getMovementStream)
            VisualDataRender(stateFlowHolder, gameViewModel)
        }

        requireNotNull(stateFlowHolder)
        requireNotNull(levelProgress)

        val coordinateState = stateFlowHolder.coordinatesStateFlow.coordinates
        val nowAtState = coordinateState.collectAsStateWithLifecycle()
        val nowAt = nowAtState.value
        requireNotNull(nowAt)

        val currentRenderer = stateFlowHolder.renderStateFlow.renderer.collectAsStateWithLifecycle()

        /*
            onToolAClick = { Log.d("GameInput", "Tool A Clicked") },
            onToolBClick = { Log.d("GameInput", "Tool B Clicked") },
         */
        InteractionControlWidget(
            stateFlowHolder = stateFlowHolder,
            levelProgress = levelProgress,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 0.dp),
            onSolutionClick = { onSolutionClicked(stateFlowHolder, gameViewModel) },
            onRenderClick = { stateFlowHolder.renderStateFlow.setRenderer(currentRenderer.value.next()) },
            onZoomClick = { Log.d("GameInput", "Zoom Clicked") },
            onHistoryClick = { Log.d("GameInput", "History Clicked") },

            onLeftClickOffset = { setOffsetBy(stateFlowHolder, -1, 0) },
            onRightClickOffset = { setOffsetBy(stateFlowHolder, +1, 0) },
            onUpClickOffset = { setOffsetBy(stateFlowHolder, 0, -1) },
            onCenterClickOffset = { resetOffset(stateFlowHolder) },
            onDownClickOffset = { setOffsetBy(stateFlowHolder, 0, +1) },

            //                Log.d("SolutionControls STEP1_LEFT", "setDelta -1")
            onLeftClickSolution = { setSolutionDelta(stateFlowHolder, levelProgress,-1) },
            onRightClickSolution = { setSolutionDelta(stateFlowHolder, levelProgress,+1) },

            //onSliderSolution = { data -> setSolutionIndex(stateFlowHolder, levelProgress, data) }, // FIXME never used!!!!
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
        delay(dragRepeatIntervalMillis)
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
