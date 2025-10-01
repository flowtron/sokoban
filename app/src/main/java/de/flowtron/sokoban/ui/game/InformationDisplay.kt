package de.flowtron.sokoban.ui.game

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.flowtron.sokoban.state.StateFlowHolder
import de.flowtron.sokoban.ui.models.GameViewModel

@Composable
fun InformationDisplay(
    stateFlowHolder: StateFlowHolder,
    gameViewModel: GameViewModel,
    getMovementStream: (String, Int) -> AnnotatedString
) {
    // WIP info display
    // player position PP, map center MC, offsetXY
    //val coordinates = stateFlowHolder.coordinatesStateFlow.coordinates.value //setCoordinates(nowAt.copy(x = nowAt.x + 1))
    val observedCoordinates by stateFlowHolder.coordinatesStateFlow.coordinates.collectAsStateWithLifecycle() // player position
    val observedOffset by stateFlowHolder.offsetStateFlow.offset.collectAsStateWithLifecycle() // map offset
    val observedMap by stateFlowHolder.levelDataStateFlow.levelData.collectAsStateWithLifecycle() // map level data
    //val observedSolution by stateFlowHolder.levelSolutionStateFlow.levelSolution.collectAsStateWithLifecycle() // solution level data
    if (observedCoordinates != null && observedOffset != null && observedMap != null) {
        val coordinates = requireNotNull(observedCoordinates)
        val playerAt = "${coordinates.x}, ${coordinates.y}"

        val offset = requireNotNull(observedOffset)
        val mapOffset = "${offset.x}, ${offset.y}"

        val openGoals = gameViewModel.openGoals()//(map = observedMap)
        var moreGameInfo: AnnotatedString = buildAnnotatedString { }
//                var testGameInfo: AnnotatedString = buildAnnotatedString { append("TEST\n") }

        val observedSolution =
            stateFlowHolder.movementSolutionStateFlow.movementSolution.collectAsStateWithLifecycle()
        val fullSolution = observedSolution.value.toDirections()
        if (fullSolution.isNotEmpty()) {

            val index =
                stateFlowHolder.movementSolutionStateFlow.indexStateFlow.collectAsStateWithLifecycle()
            val currentMove = fullSolution[index.value]
            val movementStream = getMovementStream(fullSolution, index.value)
            moreGameInfo = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.Gray)) {
                    append("${index.value + 1} / ${fullSolution.length} = $currentMove\n")
                }
                append(movementStream)
            }
        }
        //val gameTool = stateFlowHolder.gameToolStateFlow.interactionMode.collectAsStateWithLifecycle()
        val gameInfoText = buildAnnotatedString {
            //append("PP $playerAt MO $mapOffset GT ${gameTool.value}\n")
            append("PP $playerAt MO $mapOffset\n")
            //append("OG $openGoals\n")
            withStyle(style = SpanStyle(color = Color.Blue)) {
                append("OG ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(openGoals.toString())
                }
                append("\n")
            }
            if (moreGameInfo.isNotEmpty()) {
                append(moreGameInfo)
                append("\n")
            }
            buildDebugInformation(buildTestGameInfo(fullSolution), openGoals)
        }

        //Text(modifier = Modifier.scale(.5f), text = gameInfoText)
        Text(gameInfoText)
    }

}

fun buildTestGameInfo(fullSolution: String): AnnotatedString = buildAnnotatedString {
    withStyle(style = SpanStyle(color = Color.Green)) {
        append("SOLUTION [")
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append(fullSolution.length.toString())
        }
        append("]")
        append("\n")
    }
}

fun buildDebugInformation(testGameInfo: AnnotatedString, openGoals: Int?): AnnotatedString {
    return buildAnnotatedString {
        if (testGameInfo.isNotEmpty()) {
            append(testGameInfo)
            append("\n")
        }
        withStyle(style = SpanStyle(color = Color.Blue)) {
            append("OG ")
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(openGoals.toString())
            }
            append("\n")
        }
    }
}