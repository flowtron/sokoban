package de.flowtron.sokoban

import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import de.flowtron.sokoban.game.DocArticleProvider
import de.flowtron.sokoban.game.LevelProgress
import de.flowtron.sokoban.room.RoomHolder
import de.flowtron.sokoban.state.StateFlowHolder
import de.flowtron.sokoban.ui.MainAppView
import de.flowtron.sokoban.ui.models.GameViewModel
import de.flowtron.sokoban.ui.models.LevelsViewModel
import de.flowtron.sokoban.ui.models.MainAppViewModel
import de.flowtron.sokoban.ui.screens.GameScreen
import de.flowtron.sokoban.ui.screens.InfoScreenWithLogo
import de.flowtron.sokoban.ui.theme.SokobanTheme
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

                SokobanTheme {
                    if (isConfigurationDoneState.value) {
                        MainAppView(
                            toastHandler = mainAppViewModel.toastHandler,
                            levelsViewModel = levelsViewModel,
                            gameViewModel = gameViewModel,
                            stateFlowHolder = stateFlowHolder,
                            levelProgress = levelProgress,
                            assetManager = assetManager,
                            docArticleProvider = docArticleProvider,
                        )
                    } else {
                        if (isConfigurationDoneState.value) {
                            Log.i("MainActivity", "App is configured.")
                            applyConfiguration()
                            NavigateToGameScreen(gameViewModel = gameViewModel)
                        } else {
                            LaunchedEffect(Unit) {
                                val initSeconds = (MINIMUM_INITIALISATION_SHOW_MILLIS / 100.0f).toInt() / 10.0f
                                Log.i("MainActivity","Waiting for $initSeconds seconds…")
                                kotlinx.coroutines.delay(MINIMUM_INITIALISATION_SHOW_MILLIS.milliseconds)

                                applyConfiguration()
                                //kotlinx.coroutines.delay(MINIMUM_INITIALISATION_SHOW_MILLIS.milliseconds)

                                Log.i("MainActivity", "STRING CHANGED")
                                loadingScreenTextId = R.string.app_is_configured
                                // do NOT change
                                // - roomHolder setup
                                // - stateFlow config
                            }
                            // DEBUG added delay, to see the changed text
                            LaunchedEffect(Unit){
                                Log.i("MainActivity", "another wait")

                                val longer = MINIMUM_INITIALISATION_SHOW_MILLIS.milliseconds * 1.25
                                kotlinx.coroutines.delay(longer)

                                Log.i("MainActivity", "App is now configured.")
                                roomHolder.markSetupAsDone()
                                stateFlowHolder.configurationDoneStateFlow.setDone(true)
                            }
                        }

                        val thanksText = """
                        🫶 Enjoy The Game!
                        @ BaZi
                        @ Fritz
                          😃
                        """.trimIndent()

                        // FIXME
                        // add something like this into ROUTE:/doc/introduction
                        // You can only push, not pull boxes. The boxes and goals are all the same. Least pushes, then moves wins in comparison.

                        // on my screen the URL text was smushed on the right edge
                        //var updatedLine2 = ""
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
                                //onMultilineTextChange = { newText ->loadingScreenTextValue = newText }
                                onMultilineTextChange = { newText -> updatedLine2 = newText }
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun NavigateToGameScreen(gameViewModel: GameViewModel) {
        GameScreen(
            modifier = Modifier,
            gameViewModel = gameViewModel,
            stateFlowHolder = stateFlowHolder,
            levelProgress = levelProgress,
        )
    }

    private fun applyConfiguration() {
        // TODO: switch from STATUS table to CONFIG table
        val config = roomHolder.getRoomStatus()
        if (config != null) {
            stateFlowHolder.dragSensitivityStateFlow.setDragSensitivity(config.dragSensitivity)
            Log.d("MainActivity", "select level too? : ${config.lastLevelId}")
        }
    }
}