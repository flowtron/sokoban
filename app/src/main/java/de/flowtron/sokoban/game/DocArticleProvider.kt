package de.flowtron.sokoban.game
import android.content.Context
import android.content.res.AssetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocArticleProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val docArticleParser: DocArticleParser,
) {

    companion object {
        const val DOC_ASSET_DIR = "articles"
        const val DOC_EXTENSION = "md"
        const val DOC_FAILSAFE = "__INTERNAL_PLACEHOLDER_for_FAILSAFE__"
    }

    private fun pGetArticle(assetManager: AssetManager, name: String, lang: String) : String {
        val articleAssetPath = "${DOC_ASSET_DIR}/${lang}/${name}.${DOC_EXTENSION}"
        val docArticle = loadTextData(articleAssetPath, assetManager)
        return docArticle
    }

    @Suppress("SpellCheckingInspection")
    private fun pGetFailSafeArticle() = """
        Hello World!
        
        This is the fail-safe documentation article.
        
        The content should be … properly written.
        
        Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Duis sem velit, ultrices et, fermentum auctor, rhoncus ut, ligula. Phasellus at purus sed purus cursus iaculis. Suspendisse fermentum. Pellentesque et arcu. Maecenas viverra. In consectetuer, lorem eu lobortis egestas, velit odio imperdiet eros, sit amet sagittis nunc mi ac neque. Sed non ipsum. Nullam venenatis gravida orci. Curabitur nunc ante, ullamcorper vel, auctor a, aliquam at, tortor. Etiam sodales orci nec ligula. Sed at turpis vitae velit euismod aliquet. Fusce venenatis ligula in pede. Pellentesque viverra dolor non nunc. Donec interdum vestibulum libero. Morbi volutpat. Phasellus hendrerit. Quisque dictum quam vel neque. Quisque aliquam, nulla ac scelerisque convallis, nisi ligula sagittis risus, at nonummy arcu urna pulvinar nibh. Nam pharetra. Nam rhoncus, lectus vel hendrerit congue, nisl lorem feugiat ante, in fermentum erat nulla tristique arcu. Mauris et dolor. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Donec gravida, ante vel ornare lacinia, orci enim porta est, eget sollicitudin lectus lectus eget lacus. Praesent a lacus vitae turpis consequat semper. In commodo, dolor quis fermentum ullamcorper, urna massa volutpat massa, vitae mattis purus arcu nec nulla. In hac habitasse platea dictumst.
    """.trimIndent()

    fun getArticle(assetManager: AssetManager, name: String, lang: String = "en") : String {
        return if(name==DOC_FAILSAFE) pGetFailSafeArticle() else pGetArticle(assetManager, name, lang)
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

    private fun loadTextData(fileName: String, assetManager: AssetManager): String {
        return try {
            val fullExtension = ".${DOC_EXTENSION}"
            val useFileName = if(fileName.endsWith(fullExtension)) {
                fileName.removeSuffix(fullExtension)
            }else{
                fileName
            }
            assetManager
                .open(useFileName)
                .bufferedReader(Charsets.UTF_8)
                .use {
                    it.readText()
                }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun getAllArticles(lang: String = "en"): List<String> {
        val assetReader = AssetReader(context)
        val articles = assetReader.listFilesWithExtension("${DOC_ASSET_DIR}/${lang}", DOC_EXTENSION)

        return if(articles.isEmpty()){
            listOf(DOC_FAILSAFE)
        }else {
            articles
        }
    }
}