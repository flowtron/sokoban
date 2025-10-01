package de.flowtron.sokoban.game

import android.content.Context
import android.util.Log
import java.io.IOException

//import androidx.datastore.core.use

class AssetReader(private val context: Context) {

    /**
     * Reads all files with a specific extension from a subdirectory within the assets folder.
     *
     * @param subfolderName The name of the subdirectory within the assets folder.
     * @param fileExtension The file extension to filter by (e.g., "txt", "json").
     * @return A list of file names (including the extension) that match the criteria, or an empty list if none are found or an error occurs.
     */
    fun listFilesWithExtension(subfolderName: String, fileExtension: String): List<String> {
        val fileNames = mutableListOf<String>()
        try {
            val assetManager = context.assets
            val allFilesAndFolders = assetManager.list(subfolderName) ?: emptyArray()

            for (item in allFilesAndFolders) {
                if (item.endsWith(".$fileExtension")) {
                    fileNames.add(item)
                }
            }
        } catch (e: IOException) {
            Log.e("AssetReader", "Error reading files from assets/$subfolderName", e)
        }
        return fileNames
    }

    /**
     * Lists all subdirectories within a given directory in the assets folder.
     *
     * @param directoryName The name of the directory within the assets folder.
     * @return A list of subdirectory names, or an empty list if none are found or an error occurs.
     */
    fun listSubdirectories(directoryName: String): List<String> {
        val subdirectories = mutableListOf<String>()
        try {
            val assetManager = context.assets
            val allItems = assetManager.list(directoryName) ?: emptyArray()
            for (item in allItems) {
                try {
                    assetManager.open("$directoryName/$item")
                } catch (e: IOException) {
                    // It's a directory
                    subdirectories.add(item)
                }
            }
        } catch (e: IOException) {
            Log.e("AssetReader", "Error listing subdirectories in assets/$directoryName", e)
        }
        return subdirectories
    }
}
