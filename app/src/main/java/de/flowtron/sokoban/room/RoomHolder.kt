package de.flowtron.sokoban.room

import android.util.Log
import de.flowtron.sokoban.game.LevelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/*
 * TODO:
 * we need to make the database be more manageable if it really is meant to improve anything
 * inspection of, editing of
 */

@Singleton
class RoomHolder @Inject constructor(
    private val roomStatusDao: RoomStatusDao,
    private val roomLevelDao: RoomLevelDao,
    private val levelProvider: LevelProvider,
//    private val roomConfigDao: RoomConfigDao,
//    private val liveDataHolder: LiveDataHolder,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO + Job())

    // You might want to expose the configuration as a Flow or StateFlow
    // For simplicity, let's keep a local copy for now, or fetch when needed.
    private var currentRoomStatus: RoomStatus? = null

    //private var currentRoomLevels: List<RoomLevel?> = listOf(null)
    private var currentRoomLevels: List<RoomLevel> = emptyList()

    init {
        // Optionally load configuration when the holder is created
        coroutineScope.launch {
            Log.i("RoomHolder", "init")
            updateRoomStatus()
            updateRoomLevels()//coroutineContext as Context)
        }
    }

    fun getRoomStatus() = currentRoomStatus
    fun getRoomLevels() = currentRoomLevels

    private suspend fun updateRoomStatus() {
        currentRoomStatus = roomStatusDao.getConfigurationSnapshot()
        if (currentRoomStatus == null) {
            generateDefaultConfig()
        }
    }

    private suspend fun updateRoomLevels() {
        currentRoomLevels = roomLevelDao.getAllLevels()
        val localLevels = currentRoomLevels
        if (localLevels.isEmpty()) {
            Log.i("RoomHolder", "updateRoomLevels")
            populateLevelsFromAssets()
        } else {
            Log.i("RoomHolder", "Levels table contains ${localLevels.size} levels")
            //liveDataHolder.postLevelRowLiveData(localLevels)
        }
    }

    private suspend fun generateDefaultConfig() {
        val defaultConfig = RoomStatus()
        roomStatusDao.insertConfiguration(defaultConfig)
        currentRoomStatus = defaultConfig
        Log.i("RoomHolder", "Status has been initialised")
    }

    // private suspend fun populateLevelsFromAssets(context: Context)
    private suspend fun populateLevelsFromAssets() {
        val allLevels = levelProvider.getAllLevels()

        // ... oh dear .. we somehow got this part wrong
        // we want this to fill the ROOM DB table "Levels" on first start
        // later we can also add a IMPORT FROM ZIP feature to fill it more
        // and we should provide some interface to drop worlds/combo-entries too! .. L8R

        //Log.i("RoomHolder", "populateLevelsFromAssets: ${allLevels.size} groupings found")
        //liveDataHolder.postLevelInfoLiveData(allLevels)

//        val levels = roomLevelDao.getAllLevels()
//        levels?.let {
//            val updatedConfig = it.copy(isInitialSetupDone = true)
//            roomStatusDao.updateConfiguration(updatedConfig)
//            currentRoomStatus = updatedConfig
//            println("ConfigurationHolder: Initial setup marked as DONE.")
//        }

        allLevels.forEach { (comboName, comboLevels) ->
            //roomLevelDao.insertLevel(it)
            comboLevels.forEach { (worldName, worldLevels) ->
                worldLevels.forEach { levelName ->
                    val roomLevel = RoomLevel(
                        id = 0,
                        combo = comboName,
                        world = worldName,
                        level = levelName,
                        done = false,
                        help = false,
                        history = null,
                    )
                    roomLevelDao.insertLevel(roomLevel)
                }
            }
        }

        Log.i("RoomHolder", "Levels table has been initialised")
    }

    suspend fun checkInitialConfiguration(): Boolean = withContext(Dispatchers.IO) {
        var config = currentRoomStatus ?: roomStatusDao.getConfigurationSnapshot()

        if (config == null) {
            // This case should ideally be handled by the init block,
            // but as a fallback, create and insert default.
            val defaultConfig = RoomStatus(isInitialSetupDone = false)
            roomStatusDao.insertConfiguration(defaultConfig)
            config = defaultConfig
            currentRoomStatus = config
            // Perform initial setup tasks here if needed
            // For example, navigate to a setup screen, create default data, etc.
            // For now, we'll just mark it as "not done"
            return@withContext false // Indicate setup is needed
        }

        currentRoomStatus = config // Update local cache

        if (!config.isInitialSetupDone) {
            // Perform actions if initial setup is not done
            // e.g., log, prepare for navigation to a setup screen, etc.
            println("ConfigurationHolder: Initial setup is NOT done.")
            // You might want to update the flag after setup is complete
            // For now, we return its current state.
            return@withContext false // Indicate setup is needed
        }

        println("ConfigurationHolder: Initial setup is DONE.")
        return@withContext true // Indicate setup is complete
    }

    suspend fun markSetupAsDone() = withContext(Dispatchers.IO) {
        val config = currentRoomStatus ?: roomStatusDao.getConfigurationSnapshot()
        config?.let {
            val updatedConfig = it.copy(isInitialSetupDone = true)
            roomStatusDao.updateConfiguration(updatedConfig)
            currentRoomStatus = updatedConfig
            //need to update the stateflow upstream
            println("ConfigurationHolder: Initial setup marked as DONE.")
        }
    }

    /*// Add other methods to get/set specific configuration values
    suspend fun getUsername(): String? = withContext(Dispatchers.IO) {
        (currentRoomStatus ?: roomDao.getConfigurationSnapshot())?.username
    }

    suspend fun setUsername(username: String) = withContext(Dispatchers.IO) {
        val config = currentRoomStatus ?: roomDao.getConfigurationSnapshot()
        val newConfig = config?.copy(username = username) ?: RoomStatus(username = username)
        roomDao.insertConfiguration(newConfig) // Insert or replace
        currentRoomStatus = newConfig
    }*/
}