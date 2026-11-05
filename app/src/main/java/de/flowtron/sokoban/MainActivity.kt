package de.flowtron.sokoban

import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import de.flowtron.sokoban.AppDestinations.ROUTE_GAME
import de.flowtron.sokoban.game.DocArticleProvider
import de.flowtron.sokoban.game.LevelProgress
import de.flowtron.sokoban.room.RoomHolder
import de.flowtron.sokoban.room.RoomLevel
import de.flowtron.sokoban.state.GameDataInfo
import de.flowtron.sokoban.state.StateFlowHolder
import de.flowtron.sokoban.ui.MainAppView
import de.flowtron.sokoban.ui.models.GameViewModel
import de.flowtron.sokoban.ui.models.LevelsViewModel
import de.flowtron.sokoban.ui.models.MainAppViewModel
import de.flowtron.sokoban.ui.screens.InfoScreenWithLogo
import de.flowtron.sokoban.ui.theme.SokobanTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val levelsViewModel: LevelsViewModel by viewModels()
    private val gameViewModel: GameViewModel by viewModels()
    //private val viewModelStoreOwner: ViewModelStoreOwner by viewModels()

    @Inject
    lateinit var roomHolder: RoomHolder

    @Inject
    lateinit var stateFlowHolder: StateFlowHolder

    @Inject
    lateinit var levelProgress: LevelProgress

    @Inject
    lateinit var assetManager: AssetManager

    @Inject
    lateinit var docArticleProvider: DocArticleProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch {
            setContent {
                val mainAppViewModel: MainAppViewModel = hiltViewModel()
                val navController = rememberNavController()
                val coroutineScope = rememberCoroutineScope()

                val isConfigurationDoneState =
                    stateFlowHolder.configurationDoneStateFlow.done.collectAsStateWithLifecycle()

                var loadingScreenTextId by remember {
                    mutableIntStateOf(
                        if (isConfigurationDoneState.value) {
                            R.string.app_is_configured
                        } else {
                            R.string.app_is_initialising
                        }
                    )
                }
                val loadingScreenTextValue = stringResource(loadingScreenTextId)

                var updatedLine2 by remember { mutableStateOf("") }
                var targetPuzzle: RoomLevel? by remember { mutableStateOf(null) }

                SokobanTheme {
                    if (isConfigurationDoneState.value) {

                        MainAppView(
                            context = applicationContext,
                            toastHandler = mainAppViewModel.toastHandler,
                            levelsViewModel = levelsViewModel,
                            gameViewModel = gameViewModel,
                            stateFlowHolder = stateFlowHolder,
                            levelProgress = levelProgress,
                            assetManager = assetManager,
                            docArticleProvider = docArticleProvider,
                            roomHolder = roomHolder,
                            targetPuzzle,
                        )

                    } else {

                        LaunchedEffect(Unit) {
                            val initSecondsForHuman = (MINIMUM_INITIALISATION_SHOW_MILLIS / 100.0f).toInt() / 10.0f // not the same as straight divide-by-1K
                            Log.i("MainActivity", "Waiting for $initSecondsForHuman seconds…")

                            kotlinx.coroutines.delay(MINIMUM_INITIALISATION_SHOW_MILLIS.milliseconds)

                            applyConfiguration()

                            loadingScreenTextId = R.string.app_is_configured

                            // do NOT change
                            // - roomHolder setup
                            // - stateFlow config
                        }

                        // DEBUG added delay, to see the changed text
                        LaunchedEffect(Unit) {
                            val longer = MINIMUM_INITIALISATION_SHOW_MILLIS.milliseconds * 1.25
                            kotlinx.coroutines.delay(longer)

                            Log.i("MainActivity", "App is now configured.")
                            roomHolder.markSetupAsDone()
                            stateFlowHolder.configurationDoneStateFlow.setDone(true)

                            val lastLevel = roomHolder.getRoomStatus()?.lastLevelId
                            Log.i("MainActivity", "LastLevel = ${lastLevel.toString()}")
                            if (lastLevel != null) {
                                //var changed = false
                                val puzzles = roomHolder.getRoomLevels()
                                val lastLevelData = puzzles.findLast { it.id.toInt() == lastLevel }
                                requireNotNull(lastLevelData)
                                if(lastLevelData.done) {
                                    val nextLevelData = puzzles.find { it.id.toInt() > lastLevel && !it.done }
                                    if( nextLevelData != null && !nextLevelData.done ){
                                        //val gotoLastLevel = pickNextPuzzle(nextLevel, lastLevel, coroutineScope, navController)
                                        //if(gotoLastLevel!=lastLevel) changed = true
                                        targetPuzzle = nextLevelData
                                    }else{
                                        Log.i("MainActivity", "We could not find a NEXT LEVEL after $lastLevel.")
                                        mainAppViewModel.toastHandler.showToast("could NOT find NEXT", Toast.LENGTH_LONG)
                                    }
                                }else{
                                    targetPuzzle = lastLevelData
                                }
                                //Log.i("MainActivity", "THE NEXT LEVEL is $lastLevel (${if(changed){"changed"}else{"same"}}).")
                            }
                        }

                        val thanksText = """
                    🫶 Enjoy The Game!
                    for BaZi
                     &  Fritz
                      😃
                    """.trimIndent()

                        // layout still needs fixing
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            val useForLine2 = updatedLine2.ifEmpty { loadingScreenTextValue }
                            InfoScreenWithLogo(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .scale(.75f),
                                logo = painterResource(id = R.drawable.splashscreen_logo),
                                line1Text = "flowtron provides",
                                line2Text = "S O K O B A N",
                                multilineText1 = thanksText,
                                multilineText2 = useForLine2,
                                copyrightText = "©2025-2026 Florian 'flowtron' Schulte",
                                urlText = "flowtron.de",
                                onMultilineTextChange = { newText -> updatedLine2 = newText }
                            )
                        }

                    }
                }
            }
        }
    }

    private fun pickNextPuzzle(
        nextLevel: RoomLevel,
        lastLevel: Int,
        coroutineScope: CoroutineScope,
        navController: NavHostController
    ): Int {
        var lastLevel1 = lastLevel
        Log.i(
            "MainAcitivity",
            "right, so now we continue with ${nextLevel.id} as $lastLevel1 is already done."
        )
        lastLevel1 = nextLevel.id.toInt()
        //roomHolder.setCurrentLevel(nextLevel.id.toInt())//done again below

        // :: BEGIN levelloading (compare levelButtonClick())
        Log.i(
            "MainActivity",
            "loading level [ID ${lastLevel1}] [$nextLevel.combo:$nextLevel.world:${nextLevel.level}]"
        )
        val requestedGameDataInfo = GameDataInfo(
            id = requireNotNull(nextLevel.id),
            requireNotNull(nextLevel.combo),
            requireNotNull(nextLevel.world),
            requireNotNull(nextLevel.level)
        )
        if (stateFlowHolder.gameDataInfoStateFlow.gameDataInfo.equals(
                requestedGameDataInfo
            )
        ) {
            // TODO navigate back to the level already in progress
            Log.i(
                "MainActivity",
                "that level is already in progress"
            )
        } else {
            Log.i("MainActivity", "Next Level Seems Good To Go")
            stateFlowHolder.coordinatesStateFlow.setCoordinates(null)
            coroutineScope.safeLaunch {
                requireNotNull(requestedGameDataInfo.id)
                roomHolder.setCurrentLevel(requestedGameDataInfo.id.toInt())
            }

            levelsViewModel.loadLevel(requestedGameDataInfo)

            navController.navigate("$ROUTE_GAME/${requestedGameDataInfo.id}") {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                } // in question
                launchSingleTop = true
                restoreState = true
            }


        }
        // :: END levelloading
        return lastLevel1
    }

    fun applyConfiguration() {
        // TODO: switch from STATUS table to CONFIG table
        val config = roomHolder.getRoomStatus()
        if (config != null) {
            stateFlowHolder.dragSensitivityStateFlow.setDragSensitivity(config.dragSensitivity)
            Log.d("MainActivity", "select level too? : ${config.lastLevelId}")
        }
    }
}