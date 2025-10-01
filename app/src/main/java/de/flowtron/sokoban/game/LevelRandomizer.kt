package de.flowtron.sokoban.game

import android.content.Context
import android.util.Log

class LevelRandomizer(
    private val levelProvider: LevelProvider,// = ServiceLocator.levelProvider,
) {

    fun getRandomLevelFilepathFromAssets(): String? {
        return getRandomLevelFilepath(levelProvider.getAllLevels())
    }

    private fun getRandomLevelFilepath(allLevels: Map<String, Map<String, List<String>>>): String? {
        val randomSetName = allLevels.randomKey()
        val randomSet = allLevels[randomSetName]
        if (randomSet != null) {
            val randomWorldKey = randomSet.randomKey()
            val randomWorld = randomSet[randomWorldKey]
            if (!randomWorld.isNullOrEmpty()) {
                val randomLevel = randomWorld.shuffled().first()
                val filePath = "sets/$randomSetName/$randomWorldKey/$randomLevel"
                Log.i(
                    "LevelRandomizer",
                    "Random file: $randomSetName, $randomWorldKey, $randomLevel, $filePath"
                )
                return filePath
            }
        }
        return null
    }

    private fun <K, V> Map<K, V>.randomKey(): K {
        require(this.isNotEmpty()) { "Cannot select a random key from an empty map." }
        return this.keys.random()
    }

}