package de.flowtron.sokoban.ui.models

import android.content.res.AssetManager
import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.flowtron.sokoban.game.Cell.GOAL
import de.flowtron.sokoban.game.Coordinates
import de.flowtron.sokoban.game.LevelData
import de.flowtron.sokoban.game.LevelLoader
import de.flowtron.sokoban.game.LevelProgress
import de.flowtron.sokoban.game.MovementHistory
import de.flowtron.sokoban.game.SolutionLoader
import de.flowtron.sokoban.state.GameDataInfo
import de.flowtron.sokoban.state.StateFlowHolder
import de.flowtron.sokoban.ui.ToastHandler
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val levelLoader: LevelLoader,
    private val levelProgress: LevelProgress,
    private val solutionLoader: SolutionLoader,
    private val stateFlowHolder: StateFlowHolder,
    private val toastHandler: ToastHandler,
    private val assetManager: AssetManager,
) : ViewModel() {
    fun loadLevel(gameDataInfo: GameDataInfo) {
        levelLoader.loadMap(gameDataInfo, assetManager)
        stateFlowHolder.mapFinishedStateFlow.setMapFinished(false)
    }

    fun loadSolution(gameDataInfo: GameDataInfo): MovementHistory? {
        return solutionLoader.loadSolution(gameDataInfo, assetManager)
        //stateFlowHolder.gotHelp(true)
    }

    private fun currentMap(): LevelData =
        requireNotNull(stateFlowHolder.levelDataStateFlow.levelData.value)

    fun checkForWin(): Boolean {
        //levelProgress.setCommentary(true)
        val result = levelProgress.checkForWin(currentMap())
        //levelProgress.setCommentary(false)
        return result
    }

//    fun unmatchedFields() : Int? {
//        return levelProgress.unmatchedFields(currentMap())
//    }

    fun openGoals(): Int? = currentMap().data.flatten().count { it.toInt() == GOAL.id }

    fun allowedToMove(from: Coordinates, direction: Coordinates): Boolean {
        //levelProgress.setCommentary(true)
        val result = levelProgress.allowedToMove(currentMap(), from, direction)
        //levelProgress.setCommentary(false)
        return result
    }

//    fun allowedToPush(from: Coordinates, direction: Coordinates): Boolean {
//        return levelProgress.allowedToPush(currentMap(), from, direction)
//    }

    // if we want to do this commentary toggling repeatedly, there's a nice pattern for it! ;-)
    fun performMove(from: Coordinates, direction: Coordinates) {
        //levelProgress.setCommentary(true)
        val newMap = levelProgress.performMove(
            currentMap(),
            from,
            direction,
//            stateFlowHolder.movementHistoryStateFlow,
//            stateFlowHolder.coordinatesStateFlow,
        )

        levelProgress.pushIntoHistory(stateFlowHolder.movementHistoryStateFlow, direction)
        stateFlowHolder.levelDataStateFlow.setLevelData(newMap)
        stateFlowHolder.coordinatesStateFlow.setCoordinates(newMap.findPlayer())
        Log.i("GameViewModel", "perform move from $from in $direction leads to history: ${stateFlowHolder.movementHistoryStateFlow.showMovementHistory()}")

        if (checkForWin()) {
            toastHandler.showToast("Success")
            stateFlowHolder.mapFinishedStateFlow.setMapFinished(true)
        }
        //levelProgress.setCommentary(false)
    }
}