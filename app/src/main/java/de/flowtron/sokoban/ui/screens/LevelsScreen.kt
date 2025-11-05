package de.flowtron.sokoban.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import de.flowtron.sokoban.AppDestinations.GAME_ROUTE
import de.flowtron.sokoban.R
import de.flowtron.sokoban.room.RoomLevel
import de.flowtron.sokoban.state.GameDataInfo
import de.flowtron.sokoban.state.StateFlowHolder
import de.flowtron.sokoban.ui.models.LevelsViewModel

@Composable
fun LevelsScreen(
    levelsViewModel: LevelsViewModel = hiltViewModel(),
    stateFlowHolder: StateFlowHolder,
    navController: androidx.navigation.NavHostController,
) {
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

@Composable
fun LevelInfoList(
    levelLiveData: List<RoomLevel>,
    levelsViewModel: LevelsViewModel,
    stateFlowHolder: StateFlowHolder,
    navController: androidx.navigation.NavHostController = rememberNavController()
) {
    val providerBG: Color = colorResource(id = R.color.comboProviderBG)
    val buttonsPerRow = 3 
    Column {
        levelLiveData.groupBy { it.combo }.forEach { (combo, worlds) ->
            requireNotNull(combo)
            ComboHeader(combo, providerBG)

            worlds.groupBy { it.world }.forEach { (world, levels) ->
                requireNotNull(world)
                WorldHeader(combo, world, stateFlowHolder, levels)
                WorldColumn(levels, buttonsPerRow, combo, world, levelsViewModel, stateFlowHolder, navController)
            }
        }
    }
}

@Composable
fun ComboHeader(combo: String, providerBG: Color) {
    Text(
        text = combo,
        textAlign = TextAlign.Center,
        style = typography.titleLarge,
        modifier = Modifier
            .fillMaxWidth()
            .background(providerBG)
            .padding(8.dp)
    )
}

@Composable
fun WorldHeader(combo: String, world: String, stateFlowHolder: StateFlowHolder, levels: List<RoomLevel>) {
    val currentHierarchyStateFlow = stateFlowHolder.levelHierarchyStateFlow.hierarchy.collectAsStateWithLifecycle()
    val currentHierarchy = currentHierarchyStateFlow.value
    val currentIsAlreadyActive = currentHierarchy.first == combo && currentHierarchy.second == world
    val headerText = if(currentIsAlreadyActive){
        world // used to be force to INT .. then: .format("%03d")
    }else{
        var solved = 0
        var helped = 0
        var levelCount = 0
        levels.forEach {
            levelCount++
            if(it.done){
                solved++
            }
            if(it.help){
                helped++
            }
        }

        "$world: $solved ($helped) of $levelCount"
    }
    Button(
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(id = R.color.worldHeadBG),
        ),
        onClick = {
            val useValues = if(currentIsAlreadyActive){
                Pair("","")
            }else{
                Pair(combo, world)
            }
            Log.d("LevelsScreen", "setting hierarchy to (${useValues.first}, ${useValues.second})")
            stateFlowHolder.levelHierarchyStateFlow.setLevelHierarchy(useValues.first, useValues.second)
        } ) {
        Text(
            text = headerText,
            textAlign = TextAlign.Center,
            style = typography.labelSmall
        )
    }
}

@Composable
fun WorldColumn(levels: List<RoomLevel>, buttonsPerRow: Int, combo: String, world: String, levelsViewModel: LevelsViewModel, stateFlowHolder: StateFlowHolder, navController: androidx.navigation.NavHostController) {
    val currentHierarchyStateFlow = stateFlowHolder.levelHierarchyStateFlow.hierarchy.collectAsStateWithLifecycle()
    val currentHierarchy = currentHierarchyStateFlow.value
    if(currentHierarchy.first == combo && currentHierarchy.second == world){
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
                            val border = if(level.help) {
                                BorderStroke(2.dp, colorResource(id = R.color.levelUsedSolution))
                            }else{
                                null
                            }
                            val backgroundColor = if(level.done) {
                                colorResource(id = R.color.levelFinishedBG)
                            }else{
                                colorResource(id = R.color.levelFreshBG)
                            }
                            Button(
                                border = border,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = backgroundColor,
                                ),
                                onClick = {
                                    requireNotNull(combo)
                                    requireNotNull(world)
                                    levelButtonClick(level, combo, world, levelsViewModel, stateFlowHolder, navController)
                                } ) {
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

fun levelButtonClick(level: RoomLevel, combo: String, world: String, levelsViewModel: LevelsViewModel, stateFlowHolder: StateFlowHolder, navController: androidx.navigation.NavHostController) {
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
}