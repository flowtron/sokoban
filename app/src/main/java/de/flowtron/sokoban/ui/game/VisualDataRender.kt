package de.flowtron.sokoban.ui.game

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.flowtron.sokoban.game.Cell
import de.flowtron.sokoban.game.isPlayer
import de.flowtron.sokoban.state.Renderer.BOTH
import de.flowtron.sokoban.state.Renderer.DRAW
import de.flowtron.sokoban.state.Renderer.TEXT
import de.flowtron.sokoban.state.StateFlowHolder
import de.flowtron.sokoban.ui.models.GameViewModel

@Composable
fun VisualDataRender(stateFlowHolder: StateFlowHolder, gameViewModel: GameViewModel) {
    val currentScale by stateFlowHolder.scaleStateFlow.scale.collectAsStateWithLifecycle()
    val currentLevelData by stateFlowHolder.levelDataStateFlow.levelData.collectAsStateWithLifecycle()
    val currentLevelSolution by stateFlowHolder.levelSolutionStateFlow.levelSolution.collectAsStateWithLifecycle()
    val currentCoordinates by stateFlowHolder.coordinatesStateFlow.coordinates.collectAsStateWithLifecycle()
    val currentOffset by stateFlowHolder.offsetStateFlow.offset.collectAsStateWithLifecycle()
    val currentGameDataInfo by stateFlowHolder.gameDataInfoStateFlow.gameDataInfo.collectAsStateWithLifecycle()
    val currentRenderer by stateFlowHolder.renderStateFlow.renderer.collectAsStateWithLifecycle()
    val currentGameTool by stateFlowHolder.gameToolStateFlow.interactionMode.collectAsStateWithLifecycle()

    if (currentLevelData != null && currentCoordinates != null) {
        val innerLevelData = if (currentGameTool == InteractionMode.SOLUTION_CONTROLS) {
            requireNotNull(currentLevelSolution)
        } else {
            requireNotNull(currentLevelData)
        }
        val innerCoordinates = requireNotNull(currentCoordinates)
        val innerOffset = requireNotNull(currentOffset)

        // we see the square region of scale/2 +/- curCoordinates
        val halfScale =
            (currentScale - 1) / 2 // CAVEAT EMPTOR: we REQUIRE scale to always be odd and >= 3
        val currentX = innerCoordinates.x + innerOffset.x
        val currentY = innerCoordinates.y + innerOffset.y
        val xRange = (currentX - halfScale)..(currentX + halfScale)
        val yRange = (currentY - halfScale)..(currentY + halfScale)

        // seems required workaround for something that works but is marked as broken :-(
        @SuppressLint("UnusedBoxWithConstraintsScope")
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val solutionMovementState =
                stateFlowHolder.movementSolutionStateFlow.movementSolution.collectAsStateWithLifecycle()
            val showingSolutionTool = (currentGameTool == InteractionMode.SOLUTION_CONTROLS)
            val showingSolutionWithMoves = showingSolutionTool && solutionMovementState.value.data.isNotEmpty()
            if(showingSolutionTool && !showingSolutionWithMoves) {
                Text("Map Will Be MARKED\nOnce You Step Into The Solution\nMovement History")
            }else{
                val density = LocalDensity.current
                val viewWidthDp = with(density) { constraints.maxWidth.toDp() }
                val viewHeightDp = with(density) { constraints.maxHeight.toDp() }
                val viewSizeDp = minOf(viewWidthDp, viewHeightDp)
                val sizeFloatDp = viewSizeDp / currentScale.toFloat()
                val sizeModifier = Modifier.size(sizeFloatDp)
                val tileSize = sizeModifier

                val mapDisplayGridModifier = Modifier
                    .background(color = Color.LightGray)
                    .fillMaxSize()
                    .run {

                        if (currentGameTool == InteractionMode.SOLUTION_CONTROLS) {
                            if (solutionMovementState.value.data.isNotEmpty()) {
                                this.proColorFilter(ColorFilter.colorMatrix(flowMatrix()))
                            } else {
                                this
                            }
                        } else {
                            val isMapFinished = stateFlowHolder.mapFinishedStateFlow.finished.value
                            if (isMapFinished) {
                                //this.proColorFilter(ColorFilter.colorMatrix(greyscaleMatrix()))
                                this.proColorFilter(ColorFilter.colorMatrix(greenTintMatrix()))
                            } else {
                                this
                            }
                        }
                    }

                // MAP DISPLAY GRID
                Column(modifier = mapDisplayGridModifier) {
                    innerLevelData.data.forEachIndexed { rowIndex, row ->
                        if (rowIndex in yRange) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                            ) {
                                row.forEachIndexed { cellIndex, cell ->
                                    if (cellIndex in xRange) {
                                        val cellCell = requireNotNull(Cell.fromId(cell.toInt()))
                                        when (currentRenderer) {
                                            DRAW -> DrawMapTileFromId(cellCell.id, tileSize)
                                            TEXT -> CellAsText(
                                                cellCell,
                                                tileSize,
                                                innerCoordinates,
                                                rowIndex,
                                                cellIndex
                                            )

                                            BOTH -> {
                                                if (cellCell.isPlayer()) {
                                                    DrawMapTileFromId(cellCell.id, tileSize)
                                                } else {
                                                    CellAsText(
                                                        cellCell,
                                                        tileSize,
                                                        innerCoordinates,
                                                        rowIndex,
                                                        cellIndex
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        stateFlowHolder.levelDataStateFlow.showLevelData()
        stateFlowHolder.gameDataInfoStateFlow.showGameDataInfo()
        stateFlowHolder.coordinatesStateFlow.showCoordinates()

        val curGaLeIn = currentGameDataInfo
        if (curGaLeIn != null) {
            // FIXME we do not see this in regular testing
            Log.d("VisualDataRender", "Loading level $curGaLeIn")
            gameViewModel.loadLevel(gameDataInfo = curGaLeIn)
        }
    }
}

private fun flowMatrix() = ColorMatrix(
    floatArrayOf(
        0.25f, 0.25f, 0.25f, 0f, 0f,
        0.50f, 0.50f, 0.50f, 0f, 0f,
        0.75f, 0.75f, 0.75f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    )
)

private fun greyscaleMatrix() = ColorMatrix(
    floatArrayOf(
        0.2126f, 0.7152f, 0.0722f, 0f, 0f,
        0.2126f, 0.7152f, 0.0722f, 0f, 0f,
        0.2126f, 0.7152f, 0.0722f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f,
    )
)

private fun greenTintMatrix(degrees: Float = 45f): ColorMatrix {
    val m = ColorMatrix()
    m.setToRotateGreen(degrees)
    return m
}

private fun Modifier.proColorFilter(colorFilter: ColorFilter): Modifier {
    return this.drawWithCache {
        val graphicsLayer = obtainGraphicsLayer()
        graphicsLayer.apply {
            record {
                drawContent()
            }
            this.colorFilter = colorFilter
        }
        onDrawWithContent {
            drawLayer(graphicsLayer)
        }
    }
}