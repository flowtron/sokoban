package de.flowtron.sokoban.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import de.flowtron.sokoban.AppDestinations.GAME_ROUTE
import de.flowtron.sokoban.room.RoomLevel
import de.flowtron.sokoban.state.GameDataInfo
import de.flowtron.sokoban.state.StateFlowHolder
import de.flowtron.sokoban.ui.models.LevelsViewModel

@Composable
fun LevelsScreen(
    levelsViewModel: LevelsViewModel = hiltViewModel(),
    stateFlowHolder: StateFlowHolder,
    navController: androidx.navigation.NavHostController, // do NOT auto-assign, needs to be the prepped one! = rememberNavController(),
    //liveDataHolder: LiveDataHolder,
    //modifier: Modifier = Modifier
) {
//    val localLevelInfoLiveData: MapAssets by liveDataHolder.levelInfoLiveData.observeAsState(
//        emptyMap()
//    )

    val localLevelInfoLiveData: List<RoomLevel> by levelsViewModel.allLevels.observeAsState(
        emptyList()
    )
    Log.d("LevelsScreen", "localLevelInfoLiveData: ${localLevelInfoLiveData.size} top level items")

    Surface(color = MaterialTheme.colorScheme.background) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.size(30.dp))
            if (localLevelInfoLiveData.isEmpty()) {
                Text("Level Info not available")
            } else {
                LevelInfoList(
                    localLevelInfoLiveData,
                    levelsViewModel,
                    stateFlowHolder,
                    navController
                )
            }
            Spacer(modifier = Modifier.size(60.dp))
        }
    }
}
/*Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Levels Screen", fontSize = 24.sp)
        // TODO: Implement your Levels UI here

        val localLevelInfoLiveData: MapAssets by liveDataHolder.levelInfoLiveData.observeAsState(emptyMap())
    }*/

@Composable
fun LevelInfoList(
    levelLiveData: List<RoomLevel>,
    levelsViewModel: LevelsViewModel,
    stateFlowHolder: StateFlowHolder,
    navController: androidx.navigation.NavHostController = rememberNavController()
) {
    val comboNameBackgroundColor = Color(0x68, 0xA0, 0xC0)
    val buttonsPerRow = 3 // was 4
    Column {
        levelLiveData.groupBy { it.combo }.forEach { (combo, worlds) ->
            //val levelCount = worlds.map { it.level }.size
            // levelCount an worlds.size are the same!
            //Text(text = "$combo ($levelCount in ${worlds.size})", style = typography.headlineLarge)
            Text(
                text = "$combo",
                textAlign = TextAlign.Center,
                style = typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(comboNameBackgroundColor)
                    .padding(8.dp)
            ) //typography.headlineLarge
            worlds.groupBy { it.world }.forEach { (world, levels) ->
                world?.let {
                    Text(
                        text = it.format("%03d"),
                        textAlign = TextAlign.Center,
                        style = typography.labelSmall
                    )
                }//typography.headlineMedium

                Column {
                    val rows = (levels.size + buttonsPerRow - 1) / buttonsPerRow
                    for (row in 0 until rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (col in 0 until buttonsPerRow) {
                                val buttonIndex = row * buttonsPerRow + col

                                if (buttonIndex < levels.size) {
                                    val level = levels[buttonIndex]
                                    Button(onClick = {
                                        Log.i(
                                            "HomeFragment",
                                            "clicked on level [ID ${level.id}] [$combo:$world:${level.level}]"
                                        )
                                        val requestedGameDataInfo = GameDataInfo(
                                            id = requireNotNull(level.id),
                                            requireNotNull(combo),
                                            requireNotNull(world),
                                            requireNotNull(level.level)
                                        )
                                        if (stateFlowHolder.gameDataInfoStateFlow.gameDataInfo.equals(
                                                requestedGameDataInfo
                                            )
                                        ) {
                                            // TODO navigate back to the level already in progress
                                            Log.i(
                                                "LevelsScreen",
                                                "that level is already in progress"
                                            )
                                        } else {
                                            stateFlowHolder.coordinatesStateFlow.setCoordinates(null)

                                            navController.navigate("$GAME_ROUTE/${requestedGameDataInfo.id}") {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    inclusive = true
                                                } // in question
                                                launchSingleTop = true
                                                restoreState = true
                                            }

                                            levelsViewModel.loadLevel(requestedGameDataInfo)
                                        }
                                    }) {
                                        Text(text = "${level.level?.format("%03d")}")
                                    }
                                }

                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = DividerDefaults.Thickness,
                        color = DividerDefaults.color
                    )
                }
            }
        }
    }
}

@Composable
fun ForMapAssets_LevelInfoList(levelInfoLiveData: Map<String, Map<String, List<String>>>) {
    val buttonsPerRow = 4
    Column {
        levelInfoLiveData.forEach { (set, worlds) ->
            Text(text = "$set (${worlds.keys.size})", style = typography.headlineLarge)
            worlds.forEach { (world, levels) ->
                Text(text = world.format("%03d"), style = typography.headlineMedium)
                Column {
                    val rows = (levels.size + buttonsPerRow - 1) / buttonsPerRow
                    for (row in 0 until rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (col in 0 until buttonsPerRow) {
                                val buttonIndex = row * buttonsPerRow + col
                                if (buttonIndex < levels.size) {
                                    val level = levels[buttonIndex]
                                    Button(onClick = {
                                        Log.i(
                                            "HomeFragment",
                                            "clicked on level [$set][$world][$level]"
                                        )
//                                        lifecycleScope.safeLaunch {
//                                            homeViewModel.loadLevel(requireContext(), set, world, level)
//                                        }
                                    }) {
                                        Text(text = level)
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.size(10.dp))
            }
        }
    }
}
