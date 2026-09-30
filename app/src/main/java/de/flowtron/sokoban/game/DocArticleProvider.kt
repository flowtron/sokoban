package de.flowtron.sokoban.game
import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import de.flowtron.sokoban.game.LevelProvider.Companion.COMBO_ASSET_DIR
import de.flowtron.sokoban.game.LevelProvider.Companion.LEVEL_EXTENSION
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocArticleProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val DOC_ASSET_DIR = "doc"
        const val DOC_EXTENSION = "md"
    }

    fun getAllArticles(): List<String> {
        val assetReader = AssetReader(context)
        //val result: MutableMap<String, MutableMap<String, List<String>>> = mutableMapOf()
        //val articles = assetReader.listSubdirectories(DOC_ASSET_DIR)
        val articles = assetReader.listFilesWithExtension(DOC_ASSET_DIR, DOC_EXTENSION)
        return articles
    }
}