package de.flowtron.sokoban.game

import android.content.res.AssetManager
import android.util.Log
import de.flowtron.sokoban.room.RoomLevelDao
import de.flowtron.sokoban.state.GameDataInfo
import de.flowtron.sokoban.state.StateFlowHolder
import de.flowtron.sokoban.ui.ToastHandler
import java.io.InputStream
import javax.inject.Inject
import kotlin.math.max

class LevelLoader @Inject constructor(
    private val stateFlowHolder: StateFlowHolder,
    private val toastHandler: ToastHandler,
    private val levelParser: LevelParser,
    private val levelProgress: LevelProgress,
) {

    companion object {
        const val PUSHER_ON_FLOOR = 2.toByte()
        const val PUSHER_ON_GOAL = 3.toByte()
    }

    // 010 = "@" && 011 = "+" - pusher and pusher on goal, there is only ever one pusher
    private fun Byte.isPlayer(): Boolean {
        return this == PUSHER_ON_FLOOR || this == PUSHER_ON_GOAL
    }

    suspend fun loadMap(gameDataInfo: GameDataInfo, assetManager: AssetManager, roomLevelDao: RoomLevelDao) {
        val levelByteArrayOrNull = loadBinaryData(gameDataInfo.getLevelFilepath(), assetManager)
        if (levelByteArrayOrNull == null) {
            toastHandler.showToast( "Level data is NULL")
        } else {
            //val levelData = levelParser.parseLevelBinaryDataToLevelData(levelDataOrNull)
            //val previousCellCount = levelByteArrayOrNull.size

            val levelData = levelParser.expandedLevelFromBinaryData(levelByteArrayOrNull)
            requireNotNull(levelData)

            stateFlowHolder.levelDataStateFlow.setLevelData(levelData)
            stateFlowHolder.levelDataStateFlow.showLevelData()

            stateFlowHolder.levelOriginalStateFlow.setLevelOriginal(levelData)
            stateFlowHolder.levelSolutionStateFlow.setLevelSolution(levelData)

            stateFlowHolder.scaleStateFlow.setScale(levelData, 15) // passably sane default: 3 .. 5 .. 7

            // find out what is stored in database - e.g. FINISHED, and HISTORY (see below)
            val roomData = roomLevelDao.getLevelById(gameDataInfo.id?.toInt() ?: -1) // levelData.data.)

            stateFlowHolder.mapFinishedStateFlow.setMapFinished(roomData?.done ?: false)

            //Log.i("LevelLoader", "LevelData parsed ${levelData?.dimensions} : ${levelData?.data}")
            //Log.i("LevelLoader", "LevelData parsed ${levelData?.dimensions?.x}:${levelData?.dimensions?.y} with ${levelData?.data?.size} ($previousCellCount) cells.")
            Log.i("LevelLoader", "LevelData parsed ${levelData.dimensions.y}:${levelData.dimensions.x} with ${levelData.data.size}x${levelData.data[0].size} cells.")

            var missingPlayer = true
            levelData.data.forEachIndexed { y, row ->
                row.forEachIndexed { x, cell ->
                    if (cell.isPlayer()) {
                        Log.i("LevelLoader", "Player found at $x, $y")
                        stateFlowHolder.coordinatesStateFlow.setCoordinates(Coordinates(x, y))
                        missingPlayer = false
                    }
                }
            }
            if(missingPlayer) {
                //toastHandler.showToast( "Level data has no player location")
                stateFlowHolder.coordinatesStateFlow.setCoordinates(null)
            }else{
                stateFlowHolder.gameDataInfoStateFlow.setGameDataInfo(gameDataInfo)
                Log.i("LevelLoader", "should set scale to 2+max(${levelData.dimensions.x}, ${levelData.dimensions.y}) => ${2 + max(levelData.dimensions.x, levelData.dimensions.y)}")
                //stateFlowHolder.setScale(2 + max(levelData.dimensions.x, levelData.dimensions.y))
//                    Log.i("LevelLoader", "GameDataInfo set")
                stateFlowHolder.gameDataInfoStateFlow.showGameDataInfo()
                if(roomData != null){
                    val rId = roomData.id
                    val rHistory = roomData.history
                    if(rHistory != null){
                        val hHistory = MovementParser().fromDirections(rHistory) //MovementHistory(mHistory ).toDirections()
                        val mHistory = hHistory.data
                        Log.i("LevelLoader", "RoomData [$rId:${rHistory}:${mHistory}:${hHistory}]")
                    }else{
                        Log.i("LevelLoader", "RoomData [$rId]")
                    }

                }else{
                    Log.i("LevelLoader", "RoomData [NULL]")
                }
            }
            stateFlowHolder.offsetStateFlow.setOffset(Coordinates(0,0))

            // reset all level specific state flows .. much already dealt with above.
            //stateFlowHolder.levelFinishedStateFlow.setLevelFinished(false)
            //stateFlowHolder.levelOriginalStateFlow.setLevelOriginal()
            //stateFlowHolder.movementHistoryStateFlow.setMovementHistory(MovementHistory(emptyList()))
            //stateFlowHolder.movementSolutionStateFlow.setMovementSolution(MovementSolution(emptyList()))

            val useAsHistory : MovementHistory = if(roomData != null && roomData.history != null) {
                MovementParser().fromDirections(roomData.history)
            }else{
                MovementHistory(emptyList())
            }

            Log.i("LevelLoader", "useAsHistory: ${useAsHistory.toDirections()}")
            stateFlowHolder.movementHistoryStateFlow.setMovementHistory(useAsHistory)
            if(useAsHistory.size()>0) {
                stateFlowHolder.movementHistoryStateFlow.setIndex(useAsHistory.size())

                val origMap = requireNotNull(stateFlowHolder.levelOriginalStateFlow.levelOriginal.value)
                val changedMap = levelProgress.performHistory(origMap, useAsHistory)

                val bufCommentary = levelProgress.withCommentary
                levelProgress.setCommentary(false)
                stateFlowHolder.levelDataStateFlow.setLevelData(changedMap)
                levelProgress.setCommentary(bufCommentary)

                stateFlowHolder.coordinatesStateFlow.setCoordinates(changedMap.findPlayer())

                Log.d("LevelLoader", "Player at ${changedMap.findPlayer()} with level data: $changedMap")
            }

            stateFlowHolder.movementSolutionStateFlow.setMovementSolution(MovementHistory(emptyList()))

            // used to set this if levelData was NULL
            // stateFlowHolder.coordinatesStateFlow.setCoordinates(null)
        }
    }

    private fun loadBinaryData(fileName: String, assetManager: AssetManager): ByteArray? {
        return try {
            val inputStream: InputStream = assetManager.open(fileName)
            inputStream.readBytes()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}