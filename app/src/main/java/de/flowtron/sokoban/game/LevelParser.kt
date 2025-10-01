package de.flowtron.sokoban.game

import android.util.Log
import javax.inject.Inject

class LevelParser @Inject constructor() {

    companion object {
        const val DEBUG_PARSER_TO_STRING = false
        const val DEBUG_PARSER_TO_LEVEL_DATA = false
    }

    fun parseLevelBinaryDataToString(levelDataOrNull: ByteArray): String {
        if (levelDataOrNull.size > 2) {
            val columns = levelDataOrNull[0]
            val rows = levelDataOrNull[1]
            val requiredBits = columns * rows * 3
            val expectedBits = (levelDataOrNull.size - 2) * 8
            if (expectedBits >= requiredBits) {
                val mapBits = levelDataOrNull.copyOfRange(2, levelDataOrNull.size)
                val mapWords = mapBits.map {
                    val basicInt = it.toInt() and 0xFF
                    val bin8 = basicInt.toUInt().toString(2).padStart(8, '0')
                    if(DEBUG_PARSER_TO_STRING) Log.d( "LevelParser", "Binary string: $bin8 for $basicInt")

                    bin8
                }
                val mapString = mapWords.joinToString("").substring(0, requiredBits)
                if(DEBUG_PARSER_TO_STRING) Log.d("LevelParser", "Map string: $mapString")
                val readMapRowList = mutableListOf<String>()
                if (mapString.length >= requiredBits) {
                    for (curRow in 0..<rows) {
                        var curRowString = ""
                        for (curCol in 0..<columns) {
                            val offsetRow = curRow * columns * 3
                            val offsetCol = curCol * 3
                            val offsetCur = offsetRow + offsetCol
                            val fieldValue = mapString.substring(offsetCur, offsetCur + 3)
                            curRowString += parseFilling(fieldValue)
                        }
                        if(DEBUG_PARSER_TO_STRING) Log.d("LevelParser", "Row string: $curRowString")
                        readMapRowList.add(curRowString)
                    }
                }
                if(DEBUG_PARSER_TO_STRING) Log.d("LevelParser", "Map list: $readMapRowList")
                return readMapRowList.reversed().joinToString("\n")
            } else {
                Log.e(
                    "LevelParser",
                    "Level data is too small (${levelDataOrNull.size}>=$requiredBits+2)"
                )
            }
        }
        return ""
    }

    fun expandedLevelFromBinaryData(levelDataOrNull: ByteArray): LevelData? {
        val parsedLevelData = parseLevelBinaryDataToLevelData(levelDataOrNull)
        val mutableLevelData = MutableLevelData(requireNotNull(parsedLevelData).data)
        mutableLevelData.expand(2)
        return mutableLevelData.toLevelData()
    }

    fun parseLevelBinaryDataToLevelData(levelDataOrNull: ByteArray): LevelData? {
        if (levelDataOrNull.size > 2) {
            val columns = levelDataOrNull[0]
            val rows = levelDataOrNull[1]
            val requiredBits = columns * rows * 3
            val expectedBits = (levelDataOrNull.size - 2) * 8
            if (expectedBits >= requiredBits) {
                val mapBits = levelDataOrNull.copyOfRange(2, levelDataOrNull.size)
                val mapWords = mapBits.map {
                    val basicInt = it.toInt() and 0xFF
                    val bin8 = basicInt.toUInt().toString(2).padStart(8, '0')
                    if(DEBUG_PARSER_TO_LEVEL_DATA) Log.d( "LevelParser", "Binary string: $bin8 for $basicInt")

                    bin8
                }
                val mapString = mapWords.joinToString("").substring(0, requiredBits)
                if(DEBUG_PARSER_TO_LEVEL_DATA) Log.d("LevelParser", "Map string: $mapString")

                val mapRows = mutableListOf<ByteArray>()
                if (mapString.length >= requiredBits) {
                    for (curRow in 0..<rows) {
                        val curRowBytes = mutableListOf<Byte>()
                        var rowRepresentation = ""
                        for (curCol in 0..<columns) {
                            val offsetRow = curRow * columns * 3
                            val offsetCol = curCol * 3
                            val offsetCur = offsetRow + offsetCol
                            val fieldValue =
                                mapString.substring(offsetCur, offsetCur + 3) // (000, )001, .. 111
                            val binaryValue = fieldValue.toInt(radix = 2)
                            if (binaryValue in 0..7) {
                                curRowBytes.add(binaryValue.toByte())
                                rowRepresentation += binaryValue
                            }
                        }
                        mapRows.add(curRowBytes.toByteArray())

                        if(DEBUG_PARSER_TO_LEVEL_DATA) Log.d("LevelParser", "Map row: '$rowRepresentation'")
                    }
                }
                //DEPRECATED //if(DEBUG_PARSER_TO_LEVEL_DATA) Log.d("LevelParser", "Map: ${mapByteArray.map{ row -> row.map{ byte -> byte.toString() }}}")

                //val mapByteArray = mapRows.toTypedArray().reversedArray()
                //return LevelData(data = mapByteArray)

                //FIXME: do we need this row reversal anymore? I thought we did away with that
                // otherwise: fix the data files beforehand!
                val levelDataList = mapRows.reversed().map { row ->
                    row.toList()
                }
                return LevelData(
                    data = levelDataList,
                    dimensions = Coordinates(columns.toInt(), rows.toInt()),// Pair(columns.toInt(), rows.toInt()), // columns rows
                )
            } else {
                Log.e(
                    "LevelParser",
                    "Level data is too small (${levelDataOrNull.size}>=$requiredBits+2)"
                )
            }
        }
        return null
    }

    private fun parseFilling(fieldValue: String): String {
        return when (fieldValue) {
            "000" -> "␀"
            "001" -> "#"
            "010" -> "@"
            "011" -> "+"
            "100" -> "$"
            "101" -> "*"
            "110" -> "."
            "111" -> " "
            else -> "?"
        }
    }
}