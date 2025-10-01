package de.flowtron.sokoban.game

import android.util.Log
import de.flowtron.sokoban.game.LevelParser.Companion.DEBUG_PARSER_TO_STRING
import javax.inject.Inject

class SolutionParser @Inject constructor() {
    fun parseSolutionBinaryDataToMovementHistory(solutionDataOrNull: ByteArray): MovementHistory? {
        if (solutionDataOrNull.isNotEmpty()) {
            // FIXME
            // need to drop header data
            // 0: 0, 1: 0, 2: W, 3: R
            // steps = W * 256 + R
            // movementHistory[4:] ..
            val movementData = mutableListOf<Byte>()
            val b0 = solutionDataOrNull[0].toInt() and 0xFF
            val b1 = solutionDataOrNull[1].toInt() and 0xFF
            val b2 = solutionDataOrNull[2].toInt() and 0xFF
            val b3 = solutionDataOrNull[3].toInt() and 0xFF
            if(b0 == 0 && b1 == 0){
                val stepCount = b2 * 256 + b3
                //Log.d("SolutionParser","I am anticipating $b2 * 256 + $b3 = $stepCount steps.")

                val stepBits = solutionDataOrNull.drop(4)
                val stepWords = stepBits.map {
                    val basicInt = it.toInt() and 0xFF
                    val bin8 = basicInt.toUInt().toString(2).padStart(8, '0')
                    //Log.d( "SolutionParser", "Binary string: $bin8 for $basicInt")

                    bin8
                }
                val stepString = stepWords.joinToString("")//.substring(0, stepCount*8)
                //Log.d("SolutionParser", "stepString = $stepString")

                /*solutionDataOrNull.drop(4).forEach { solutionStep ->
                    val basicInt = solutionStep.toInt() and 0xFF
                    //Log.d("SolutionParser", "My solutionStep is $solutionStep = ${solutionStep.toInt()} && 0xFF = $basicInt")
                    movementData.add(basicInt.toByte())
                }
                Log.d("SolutionParser", "My movementData is ${movementData.size} long.")*/
                if(stepString.length >= stepCount * 2){
                    for(stepIndex in 0..(stepCount-1)){
                        val stepOffset = stepIndex * 2
                        val stepWord = stepString.substring(stepOffset, stepOffset+2)
                        val stepByte = parseDirectionAsByte(stepWord)
                        movementData.add(stepByte)
                    }
                    //stepString.forEach { solutionStep ->
                    //    movementData.add(stepString.toByte())
                    //}
                    //Log.d("SolutionParser", "My movementData is ${movementData.size} long.")
                }else{
                    Log.d("SolutionParser", "stepString.length = ${stepString.length} < stepCount * 2 = ${stepCount * 2}")
                }
            }

            return MovementHistory(movementData)
        }
        return null
    }

    private fun parseDirectionAsString(stepWord: String): String {
        return when (stepWord) {
            "00" -> "E"
            "01" -> "N"
            "10" -> "W"
            "11" -> "S"
            else -> "?"
        }
    }

    private fun parseDirectionAsByte(stepWord: String): Byte {
        return when (stepWord) {
            "00" -> 0
            "01" -> 1
            "10" -> 2
            "11" -> 3
            else -> 4
        }
    }
}