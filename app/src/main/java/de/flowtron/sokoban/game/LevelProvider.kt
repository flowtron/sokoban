package de.flowtron.sokoban.game

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LevelProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val COMBO_ASSET_DIR = "combos"
        const val PUZZLE_EXTENSION = "l"
    }
    /*
     * combo-1:
     *  \- world-1:
     *      \- level-1
     *      |- level-2
     *      |- level-3
     *  |- world-2:
     *      \- level-1
     *      |- level-2
     * combo-2:
     *  \- world-1:
     *      \- level-1
     */
    fun getAllLevels(): Map<String, Map<String, List<String>>> {
        val assetReader = AssetReader(context)
        val result: MutableMap<String, MutableMap<String, List<String>>> = mutableMapOf()
        val combos = assetReader.listSubdirectories(COMBO_ASSET_DIR)
        val levelInfo = mutableMapOf<String, Int>()
        combos.forEach { combo ->
            //Log.v("LevelProvider", "Found combo: $combo")
            levelInfo[combo] = 0
            val worlds = assetReader.listSubdirectories("$COMBO_ASSET_DIR/$combo")
            val mapOfWorld: MutableMap<String, List<String>> = mutableMapOf()
            worlds.sortedBy {
                //it.toInt() // we used to force our world names to be numeric .. no real need
                it // I hope this is a acceptable lexical ordering by default otherwise we'll need a sorting method here
            }.forEach { world ->
                val puzzles = assetReader.listFilesWithExtension("$COMBO_ASSET_DIR/$combo/$world", PUZZLE_EXTENSION)
                mapOfWorld[world] = puzzles.map { it.split('.')[0] }

                levelInfo[combo] = puzzles.size
                //Log.v("LevelProvider", "World $world has ${levels.size} puzzles.")
            }
            result[combo] = mapOfWorld
        }
        Log.d("LevelProvider", "Puzzles in combos: ${levelInfo.map { (k, v) -> "$k: $v" }.joinToString(", ")}")
        return result
    }
}