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
            val levelDataPair = levelParser.expandedLevelFromBinaryData(levelByteArrayOrNull)
            val levelData = levelDataPair.first
            val edgeAdded = levelDataPair.second
            val scaleSuggest = levelDataPair.third
            requireNotNull(levelData)
            //val levelData = LevelData(origLevelData, Coordinates(origLevelData.dimensions.x + edgeAdded, origLevelData.dimensions.y + edgeAdded))


            // new
            // TODO: clever decision on whether or not to use edgeAdded may rely on map size smaller displayable size
            //val suggestedOffset = Coordinates(-(1+3*edgeAdded/2),-edgeAdded)
            stateFlowHolder.scaleStateFlow.setScale(levelData, scaleSuggest, edgeAdded)
            //val suggestedOffset = Coordinates(-edgeAdded/2,-edgeAdded/2)
            val suggestedOffset = Coordinates(0,0) // Coordinates(-edgeAdded,-edgeAdded)
            // TODO: the player is often not in the center of the map at the start, but seeing the entire level would be nice
            // so we should try to figure out how much me /may/ move the offset w/o the player being on the edge or even beyond the view
            //if()

            stateFlowHolder.levelDataStateFlow.setLevelData(levelData)
            stateFlowHolder.levelDataStateFlow.showLevelData()

            stateFlowHolder.levelOriginalStateFlow.setLevelOriginal(levelData)
            stateFlowHolder.levelSolutionStateFlow.setLevelSolution(levelData)

            val roomData = roomLevelDao.getLevelById(gameDataInfo.id?.toInt() ?: -1)
            stateFlowHolder.mapFinishedStateFlow.setMapFinished(roomData?.done ?: false)

            //Log.i("LevelLoader", "LevelData parsed ${levelData.dimensions.x}:${levelData.dimensions.y} with edge $edgeAdded becomes ${levelData.data[0].size}x${levelData.data.size} cells.")
            Log.i("LevelLoader", "LevelData parsed ${levelData.dimensions.x}:${levelData.dimensions.y} (incl. edge $edgeAdded) @scale:$scaleSuggest")

            var missingPlayer = true
            levelData.data.forEachIndexed { y, row ->
                row.forEachIndexed { x, cell ->
                    if (cell.isPlayer()) {
                        Log.i("LevelLoader", "Player found at $x, $y")
                        stateFlowHolder.coordinatesStateFlow.setCoordinates(Coordinates(x, y))
                        missingPlayer = false
                        //if(x+scaleSuggest/2 > )
                        //Log.i("LevelLoader", "The puzzle has [${levelData.dimensions.x}, ${levelData.dimensions.y}] with edge:$edgeAdded")
                    }
                }
            }
            if(missingPlayer) {
                //toastHandler.showToast( "Level data has no player location")
                stateFlowHolder.coordinatesStateFlow.setCoordinates(null)
            }else{
                stateFlowHolder.gameDataInfoStateFlow.setGameDataInfo(gameDataInfo)
                //Log.i("LevelLoader", "should set scale to 2+max(${levelData.dimensions.x}, ${levelData.dimensions.y}) => ${2 + max(levelData.dimensions.x, levelData.dimensions.y)}")
                Log.i("LevelLoader", "should set scale to max(${levelData.dimensions.x}, ${levelData.dimensions.y}) => ${max(levelData.dimensions.x, levelData.dimensions.y)}")

                stateFlowHolder.gameDataInfoStateFlow.showGameDataInfo()
                if(roomData != null){
                    val rId = roomData.id
                    val rHistory = roomData.history
                    if(rHistory != null){
//                        val hHistory = MovementParser().fromDirections(rHistory)
//                        val mHistory = hHistory.data
//                        Log.i("LevelLoader", "RoomData [$rId:${rHistory}:${mHistory}:${hHistory}]")
                        Log.i("LevelLoader", "RoomData [$rId:${rHistory}:${roomData.done}]")
                    }else{
                        Log.i("LevelLoader", "RoomData [$rId]")
                    }
                    //Log.i("LevelLoader", "RoomData matches MapFinished? ${roomData.done} == ${stateFlowHolder.mapFinishedStateFlow.finished.value}")
                }else{
                    Log.i("LevelLoader", "RoomData [NULL]")
                }
            }
            stateFlowHolder.offsetStateFlow.setOffset(suggestedOffset)

            val useAsHistory : MovementHistory = if(roomData?.history != null) {
                MovementParser().fromDirections(roomData.history)
            }else{
                MovementHistory(emptyList())
            }

            //Log.i("LevelLoader", "useAsHistory: ${useAsHistory.toDirections()}")
            stateFlowHolder.movementHistoryStateFlow.setMovementHistory(useAsHistory)
            if(useAsHistory.size()>0) {
                stateFlowHolder.movementHistoryStateFlow.setIndex(useAsHistory.size())

                val origMap = requireNotNull(stateFlowHolder.levelOriginalStateFlow.levelOriginal.value)
                val goodPos = if(!missingPlayer) { stateFlowHolder.coordinatesStateFlow.coordinates.value } else { null }
                val changedMap = levelProgress.performHistory(origMap, useAsHistory, goodPos)

                val bufCommentary = levelProgress.withCommentary
                levelProgress.setCommentary(false)
                stateFlowHolder.levelDataStateFlow.setLevelData(changedMap)
                levelProgress.setCommentary(bufCommentary)

                stateFlowHolder.coordinatesStateFlow.setCoordinates(changedMap.findPlayer())

                Log.d("LevelLoader", "Player at ${changedMap.findPlayer()} with level data: $changedMap")
            }

            stateFlowHolder.movementSolutionStateFlow.setMovementSolution(MovementHistory(emptyList()))
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