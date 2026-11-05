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
            updateRoomLevels()
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
            Log.i("RoomHolder", "Puzzles table contains ${localLevels.size} puzzles")
            //liveDataHolder.postLevelRowLiveData(localLevels)
        }
    }

    private suspend fun generateDefaultConfig() {
        val defaultConfig = RoomStatus()
        roomStatusDao.insertConfiguration(defaultConfig)
        currentRoomStatus = defaultConfig
        Log.i("RoomHolder", "Status has been initialised")
    }

    private suspend fun populateLevelsFromAssets() {
        val origCount = roomLevelDao.getLevelCount()
        val allLevels = levelProvider.getAllLevels()
        allLevels.forEach { (comboName, comboLevels) ->
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
        val curCount = roomLevelDao.getLevelCount()
        val addedCount = curCount - origCount
        Log.i("RoomHolder", "Puzzles populated with $addedCount puzzles from assets.")
    }

    suspend fun markSetupAsDone() = withContext(Dispatchers.IO) {
        val config = currentRoomStatus ?: roomStatusDao.getConfigurationSnapshot()
        config?.let {
            val updatedConfig = it.copy(isInitialSetupDone = true)
            roomStatusDao.updateConfiguration(updatedConfig)
            currentRoomStatus = updatedConfig
            //we update the stateflow upstream
            println("ConfigurationHolder: Initial setup marked as DONE.")
        }
    }

    suspend fun setCurrentLevel(levelId: Int) = withContext(Dispatchers.IO) {
        val config = currentRoomStatus ?: roomStatusDao.getConfigurationSnapshot()
        config?.let {
            val updatedConfig = it.copy(lastLevelId = levelId)
            roomStatusDao.updateConfiguration(updatedConfig)
            currentRoomStatus = updatedConfig
            //need to update the stateflow upstream
            Log.d("RoomHolder", "ConfigurationHolder: current level set to $levelId.")
        }
    }

    suspend fun getNextLevel() : RoomLevel? {
        var rLevel : RoomLevel? = null
        val config = currentRoomStatus ?: roomStatusDao.getConfigurationSnapshot()
        Log.d("RoomHolder", "getNextLevel has config? ${if(config!=null){"OK"}else{"NOT LOADED"}}")
        config?.let { config ->
            Log.d("RoomHolder", "config lastLevelId = ${config.lastLevelId}")
            if(config.lastLevelId != null){
                val curLevel = roomLevelDao.getLevelById(config.lastLevelId)
                if(curLevel != null){
                    Log.d("RoomHolder", "curLevel = [${curLevel.combo},${curLevel.world}]")
                    val curCombo = curLevel.combo
                    val curWorld = curLevel.world
                    // the nextLevel ID 'should be' one up .. is a bit of a cheat/hack/poor mans solution
                    // but it should work reliably enough
                    requireNotNull(curCombo)
                    requireNotNull(curWorld)
                    val nextLevel = roomLevelDao.getNextLevel(curCombo, curWorld, config.lastLevelId + 1L)
                    Log.d("RoomHolder", nextLevel.toString())
                    if(nextLevel != null) {
                        rLevel = nextLevel
                    }
                }
            }
        }
        return rLevel
    }
}