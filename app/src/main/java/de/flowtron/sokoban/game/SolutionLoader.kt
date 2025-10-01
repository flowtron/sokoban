package de.flowtron.sokoban.game

import android.content.res.AssetManager
import android.util.Log
import de.flowtron.sokoban.state.GameDataInfo
import de.flowtron.sokoban.state.StateFlowHolder
import de.flowtron.sokoban.ui.ToastHandler
import java.io.InputStream
import javax.inject.Inject

class SolutionLoader @Inject constructor(
    private val stateFlowHolder: StateFlowHolder,
    private val toastHandler: ToastHandler,
    private val solutionParser: SolutionParser,
) {

    fun loadSolution(gameDataInfo: GameDataInfo, assetManager: AssetManager) : MovementHistory? {
        val solutionByteArrayOrNull = loadBinaryData(gameDataInfo.getSolutionFilepath(), assetManager)
        if (solutionByteArrayOrNull == null) {
            toastHandler.showToast( "Solution data is NULL")
        } else {
            // need to deal with header somewhere!
            val solutionBinaryData = solutionParser.parseSolutionBinaryDataToMovementHistory (solutionByteArrayOrNull)
            requireNotNull(solutionBinaryData)

            Log.i("SolutionLoader", "SolutionData parsed with ${solutionBinaryData.data.size} steps.")

            //stateFlowHolder.solutionDataStateFlow.setSolutionData(movementHistory)
            //stateFlowHolder.solutionDataStateFlow.showSolutionData()
            return solutionBinaryData
        }
        return null
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