package de.flowtron.sokoban.ui.models

import android.content.res.AssetManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.flowtron.sokoban.game.Cell.GOAL
import de.flowtron.sokoban.game.Coordinates
import de.flowtron.sokoban.game.LevelData
import de.flowtron.sokoban.game.LevelLoader
import de.flowtron.sokoban.game.LevelProgress
import de.flowtron.sokoban.game.MovementHistory
import de.flowtron.sokoban.game.SolutionLoader
import de.flowtron.sokoban.room.RoomLevelDao
import de.flowtron.sokoban.safeLaunch
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
    private val roomLevelDao: RoomLevelDao,
) : ViewModel() {

    private val chatty = false

    suspend fun loadLevel(gameDataInfo: GameDataInfo) {
        levelLoader.loadMap(gameDataInfo, assetManager, roomLevelDao)
        stateFlowHolder.mapFinishedStateFlow.setMapFinished(false)
    }

    fun loadSolution(gameDataInfo: GameDataInfo): MovementHistory? {
        val solution = solutionLoader.loadSolution(gameDataInfo, assetManager)

        // FIXME TODO - remove this line and possibly commented lines below once replacement is done TAG:helpingSteps
        // this was the LOAD HELP gets you marked approach, which is not the proper way ..
        // .. we can think about leaking the data into memory by loading it before needing it L8R.
//        if(solution != null && solution.data.isNotEmpty()) {
//            viewModelScope.safeLaunch {
//                updateRoomLevel(help = true)
//            }
//        }

        return solution
    }

    private fun currentMap(): LevelData =
        requireNotNull(stateFlowHolder.levelDataStateFlow.levelData.value)

    fun checkForWin(): Boolean {
        val result = levelProgress.checkForWin(currentMap())
        return result
    }

    fun openGoals(): Int? = currentMap().data.flatten().count { it.toInt() == GOAL.id }

    fun allowedToMove(from: Coordinates, direction: Coordinates): Boolean {
        val result = levelProgress.allowedToMove(currentMap(), from, direction)
        return result
    }

    fun performMove(from: Coordinates, direction: Coordinates) {
        val newMap = levelProgress.performMove(
            currentMap(),
            from,
            direction,
        )

        levelProgress.pushIntoHistory(stateFlowHolder.movementHistoryStateFlow, direction)
        stateFlowHolder.levelDataStateFlow.setLevelData(newMap)
        stateFlowHolder.coordinatesStateFlow.setCoordinates(newMap.findPlayer())
        if(chatty) {
            Log.d(
                "GameViewModel",
                "perform move from $from in $direction leads to history: ${stateFlowHolder.movementHistoryStateFlow.showMovementHistory()}"
            )
        }

        if (checkForWin()) {
            toastHandler.showToast("Success")
            stateFlowHolder.mapFinishedStateFlow.setMapFinished(true) // CHECK: not if this is a history re-run!

            viewModelScope.safeLaunch {
                updateRoomLevel(
                    done = true,
                    history = stateFlowHolder.movementHistoryStateFlow.movementHistory.value
                )
            }
        } else {
            viewModelScope.safeLaunch {
                updateRoomLevel(
                    history = stateFlowHolder.movementHistoryStateFlow.movementHistory.value
                )
            }
        }
    }

    suspend fun updateRoomLevel(
        done: Boolean? = null,
        help: Boolean? = null,
        history: MovementHistory = MovementHistory(emptyList()),
        deleteHistory: Boolean = false,
    ) {
        val gameDataInfo = stateFlowHolder.gameDataInfoStateFlow.gameDataInfo.value
        if (gameDataInfo != null && gameDataInfo.id != null) {
            val curLevel = roomLevelDao.getLevelById(gameDataInfo.id.toInt())
            if (curLevel != null) {
                var changed = curLevel

                if(done !== null){ changed = changed.copy(done = done) }
                if(help !== null){ changed = changed.copy(help = help) }
                if(history.data.isNotEmpty()) {
                    changed = changed.copy(history = history.toString())
                }else{
                    if(deleteHistory) changed = changed.copy(history = null)
                }

                roomLevelDao.updateLevel(changed)
            }
        }
    }

    fun triggerUpdateRoomLevel(done: Boolean? = null, help: Boolean? = null, history: MovementHistory = MovementHistory(emptyList()) ){
        viewModelScope.safeLaunch {
            updateRoomLevel(done, help, history)
        }
    }
}