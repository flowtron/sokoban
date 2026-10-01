package de.flowtron.sokoban.game

import javax.inject.Inject

class DocArticleParser @Inject constructor() {

    companion object {
        const val DEBUG_PARSER_TO_STRING = false
        const val DEBUG_PARSER_TO_LEVEL_DATA = false
    }

    fun parseDocArticleBinaryDataToString(docArticleDataOrNull: ByteArray): String {
        return ""
    }

    fun articleFromBinaryData(binaryData: ByteArray) : String {
        return ""
    }
}