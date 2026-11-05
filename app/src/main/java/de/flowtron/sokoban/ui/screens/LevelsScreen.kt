package de.flowtron.sokoban.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getString
//import androidx.hilt.navigation.compose.hiltViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import de.flowtron.sokoban.AppDestinations.ROUTE_GAME
import de.flowtron.sokoban.R
import de.flowtron.sokoban.room.RoomHolder
import de.flowtron.sokoban.room.RoomLevel
import de.flowtron.sokoban.safeLaunch
import de.flowtron.sokoban.state.GameDataInfo
import de.flowtron.sokoban.state.StateFlowHolder
import de.flowtron.sokoban.ui.models.LevelsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope

/*

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

 */

@Composable
fun LevelsScreen(
    levelsViewModel: LevelsViewModel = hiltViewModel(),
    stateFlowHolder: StateFlowHolder,
    roomHolder: RoomHolder,
    navController: androidx.navigation.NavHostController,
//    combo: String? = null,
//    world: String? = null,
) {
    val localLevelInfoLiveData: List<RoomLevel> by levelsViewModel.allLevels.observeAsState(
        emptyList()
    )
    Log.d("LevelsScreen", "localLevelInfoLiveData: ${localLevelInfoLiveData.size} puzzles")

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
                    roomHolder,
                    navController,
//                    combo,
//                    world,
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
    roomHolder: RoomHolder,
    navController: androidx.navigation.NavHostController = rememberNavController(),
//    combo: String?,
//    world: String?,
) {
    val providerBG: Color = colorResource(id = R.color.comboProviderBG)
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        levelLiveData.groupBy { it.combo }.forEach { (listCombo, worlds) ->
            requireNotNull(listCombo)
            ComboHeader(listCombo, providerBG)

            worlds.groupBy { it.world }.forEach { (listWorld, levels) ->
                requireNotNull(listWorld)
                WorldHeader(listCombo, listWorld, stateFlowHolder, levels)
                WorldColumn(levels, listCombo, listWorld, levelsViewModel, stateFlowHolder, roomHolder,navController)
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
            .padding(2.dp)
    )
}

@Composable
fun WorldHeader(combo: String, world: String, stateFlowHolder: StateFlowHolder, levels: List<RoomLevel>) {
    val currentHierarchyStateFlow = stateFlowHolder.levelHierarchyStateFlow.hierarchy.collectAsStateWithLifecycle()
    val currentHierarchy = currentHierarchyStateFlow.value

    val currentIsAlreadyActive = currentHierarchy.first == combo && currentHierarchy.second == world

    val modifier = Modifier.fillMaxWidth(1f)
    //if(currentIsAlreadyActive){}else{Modifier}

    var headerSolved = 0
    var headerHelped = 0
//    val headerText = if(currentIsAlreadyActive){
//        world
//    }else{
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
        /*val hadHelp = if(helped > 0){
            " with help for $helped."
        }else{
            "."
        }*/
        headerSolved = solved
        headerHelped = helped
        //"$world\nSolved $solved of $levelCount$hadHelp"
//    }
    val border = if(currentIsAlreadyActive) {
        BorderStroke(2.dp, colorResource(id = R.color.levelCurrentActive))
    }else{
        null
    }

    Button(
        modifier = modifier,
        shape = CutCornerShape(4.dp),
        border = border,
        colors = ButtonDefaults.buttonColors(
            contentColor = colorResource(id = R.color.worldHeadFG),
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
        Row {
            Column(
                Modifier.weight(.25f)
            ) {
                Text(
                    text = world,
                    textAlign = TextAlign.Center,
                    style = typography.labelSmall,
                    modifier = Modifier.scale(1.75f)
                )

            }
            Column(
                Modifier.weight(.5f)
            ) {
                val countTimes = if(headerHelped==1){
                    "time"
                }else{
                    "times"
                }
                if(headerHelped>0){
                    Text(
                        text = "had help $headerHelped $countTimes",
                        textAlign = TextAlign.Center,
                        style = typography.labelSmall,
                        modifier = Modifier.scale(0.90f)
                    )
                }
            }
            Column(
                Modifier.weight(.25f)
            ) {
                Text(
                    text = "$headerSolved/$levelCount",
                    textAlign = TextAlign.Center,
                    style = typography.labelSmall,
                    modifier = Modifier.scale(1.25f)
                )
            }
        }

    }
}

@Composable
fun getLevelBorderStroke(level: RoomLevel): BorderStroke? {
    return if(level.help) {
        BorderStroke(2.dp, colorResource(id = R.color.levelUsedSolution))
    }else{
        null
    }
}

@Composable
fun getLevelBackgroundColor(level: RoomLevel): Color {
    return if(level.done) {
        colorResource(id = R.color.levelFinishedBG)
    }else{
        colorResource(id = R.color.levelFreshBG)
    }
}

@Composable
fun WorldColumn(levels: List<RoomLevel>, combo: String, world: String, levelsViewModel: LevelsViewModel, stateFlowHolder: StateFlowHolder, roomHolder: RoomHolder, navController: androidx.navigation.NavHostController ) {
    val currentHierarchyStateFlow = stateFlowHolder.levelHierarchyStateFlow.hierarchy.collectAsStateWithLifecycle()
    val currentHierarchy = currentHierarchyStateFlow.value
    val hideDoneLevels = remember { mutableStateOf(true) }
    val hasSolvedLevels = levels.any{ it.done }
    val coroutineScope = rememberCoroutineScope()
    if(currentHierarchy.first == combo && currentHierarchy.second == world) {
        if(hasSolvedLevels) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                shape = CutCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.levelFinishedBG),
                    contentColor = colorResource(id = R.color.levelFinishedFG)
                ),
                onClick = {
                    hideDoneLevels.value = !hideDoneLevels.value
                }
            ) {
                val msg = if(hideDoneLevels.value) {
                    stringResource(R.string.levels_show_done)
                } else {
                    stringResource(R.string.levels_hide_done)
                }
                Text(text = msg)
            }
        }
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp),
            horizontalArrangement = Arrangement.SpaceBetween, // spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            levels.forEach { level ->
                if(!hideDoneLevels.value || !level.done){
                    Button(
                        border = getLevelBorderStroke(level),
                        shape = CutCornerShape(4.dp), // RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = getLevelBackgroundColor(level),
                        ),
                        onClick = {
                            levelButtonClick(
                                level,
                                combo,
                                world,
                                levelsViewModel,
                                stateFlowHolder,
                                roomHolder,
                                coroutineScope,
                                navController
                            )
                        }
                    ) {
                        Text(text = "${level.level?.format("%03d")}") // FIXME: this is not guaranteed, is it – should be a possibly alpha-numeric filename.
                    }
                }
            }
        }
    }
}

fun levelButtonClick(level: RoomLevel, combo: String, world: String, levelsViewModel: LevelsViewModel, stateFlowHolder: StateFlowHolder, roomHolder: RoomHolder, coroutineScope: CoroutineScope, navController: androidx.navigation.NavHostController) {
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
        coroutineScope.safeLaunch {
            requireNotNull(requestedGameDataInfo.id)
            roomHolder.setCurrentLevel(requestedGameDataInfo.id.toInt())
        }

        navController.navigate("$ROUTE_GAME/${requestedGameDataInfo.id}") {
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = true
            } // in question
            launchSingleTop = true
            restoreState = true
        }

        levelsViewModel.loadLevel(requestedGameDataInfo)
    }
}