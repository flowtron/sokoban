package de.flowtron.sokoban

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val levelsViewModel: LevelsViewModel by viewModels()
    private val gameViewModel: GameViewModel by viewModels()

    @Inject
    lateinit var roomHolder: RoomHolder

    @Inject
    lateinit var stateFlowHolder: StateFlowHolder

    @Inject
    lateinit var levelProgress: LevelProgress

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch {
            setContent {
                val mainAppViewModel: MainAppViewModel = hiltViewModel()
                val isOnboardedState =
                    stateFlowHolder.configurationDoneStateFlow.done.collectAsStateWithLifecycle()
                var loadingScreenText by remember {
                    mutableStateOf(
                        if (isOnboardedState.value) {
                            "App is configured."
                        } else {
                            "App is initialising …"
                        }
                    )
                }
                SokobanTheme {
                    if (isOnboardedState.value) {
                        MainAppView(
                            applicationContext,
                            toastHandler = mainAppViewModel.toastHandler,
                            levelsViewModel = levelsViewModel,
                            gameViewModel = gameViewModel,
                            stateFlowHolder = stateFlowHolder,
                            levelProgress = levelProgress,
                        )
                    } else {
                        if (isOnboardedState.value) {
                            Log.i(
                                "MainActivity",
                                "App is configured."
                            )
                            applyConfiguration()
                            NavigateToGameScreen(gameViewModel = gameViewModel)
                        } else {
                            LaunchedEffect(Unit) {
                                Log.i(
                                    "MainActivity",
                                    "Waiting for ${(MINIMUM_INITIALISATION_SHOW_MILLIS / 100.0f).toInt() / 10.0f} seconds…"
                                )
                                kotlinx.coroutines.delay(MINIMUM_INITIALISATION_SHOW_MILLIS)

                                applyConfiguration()
                                roomHolder.markSetupAsDone()
                                stateFlowHolder.configurationDoneStateFlow.setOnboarded(true)

                                loadingScreenText = "App is now configured."

                                Log.i("MainActivity", "App is now configured.")
                            }
                        }
                        // on my screen the URL text was smushed on the right edge
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            InfoScreenWithLogo(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .scale(.75f),
                                logo = painterResource(id = R.drawable.splashscreen_logo),
                                line1Text = "flowtron provides",
                                line2Text = "S O K O B A N",
                                multilineText = loadingScreenText,
                                copyrightText = "©2025 Florian 'flowtron' Schulte",
                                urlText = "flowtron.de",
                                onMultilineTextChange = { newText ->
                                    loadingScreenText = newText
                                }
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
        val config = roomHolder.getRoomStatus()
        if (config != null) {
            stateFlowHolder.dragSensitivityStateFlow.setDragSensitivity(config.dragSensitivity)
            Log.d("MainActivity", "select level too? : ${config.lastLevelId}")
        }


    }
}