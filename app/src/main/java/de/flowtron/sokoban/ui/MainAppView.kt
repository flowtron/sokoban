package de.flowtron.sokoban.ui // Or your preferred package

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat.getString
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import de.flowtron.sokoban.AppDestinations.ROUTE_GAME
import de.flowtron.sokoban.AppDestinations.ROUTE_HELP
import de.flowtron.sokoban.AppDestinations.ROUTE_PUZZLES
import de.flowtron.sokoban.AppDestinations.ROUTE_SETTINGS
import de.flowtron.sokoban.R
import de.flowtron.sokoban.game.DocArticleProvider
import de.flowtron.sokoban.game.LevelProgress
import de.flowtron.sokoban.room.RoomHolder
import de.flowtron.sokoban.room.RoomLevel
import de.flowtron.sokoban.safeLaunch
import de.flowtron.sokoban.state.StateFlowHolder
import de.flowtron.sokoban.ui.models.GameViewModel
import de.flowtron.sokoban.ui.models.LevelsViewModel
import de.flowtron.sokoban.ui.screens.DocArticle
import de.flowtron.sokoban.ui.screens.DocScreen
import de.flowtron.sokoban.ui.screens.GameScreen
import de.flowtron.sokoban.ui.screens.LevelsScreen
import de.flowtron.sokoban.ui.screens.SettingsScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope

// Data class to represent items in the bottom navigation
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
)

@Composable
fun MainAppView(
    context: Context,
    toastHandler: ToastHandler,
    levelsViewModel: LevelsViewModel,
    gameViewModel: GameViewModel,
    stateFlowHolder: StateFlowHolder,
    levelProgress: LevelProgress,
    assetManager: AssetManager,
    docArticleProvider: DocArticleProvider,
    roomHolder: RoomHolder,
    targetPuzzle: RoomLevel?,
) {
    val navController = rememberNavController()

    // FIXME this works differently than intuited; our composables aren't alive as much as we thought
    // State to track if a level has been selected, determining "Game" tab availability
    var selectedLevel by remember { mutableStateOf<Long?>(targetPuzzle?.id ) } // Store selected level ID, null if none

    val currentGameDataInfo = stateFlowHolder.gameDataInfoStateFlow.gameDataInfo.collectAsStateWithLifecycle()
    if (currentGameDataInfo.value != null) {
        if (selectedLevel != currentGameDataInfo.value!!.id) {
            selectedLevel = currentGameDataInfo.value!!.id
            Log.i("MainAppView", "selectedLevel = $selectedLevel")
            //coroutineScope.safeLaunch {  }
            /*safeLaunch {

            }*/
            //roomHolder.setCurrentLevel(selectedLevel)
        }
    }

//    // DEBUG -- wanna see it triggered
//    LaunchedEffect(currentGameDataInfo) {
//        val gli = currentGameDataInfo.value
//        if (gli != null) {
//            Log.i("MainAppView", "New game level info: ${gli.id} ${gli.combo}:${gli.world}:${gli.level}")
//        }
//    }

    // FYI: the enabled boolean we had here is now dealt with in the logic
    // only the gameRoute is going to be toggled and the criteria is clear-cut
    val navItems = listOf(
        BottomNavItem(
            label = getString(context,R.string.screen_doc),
            icon = Icons.Filled.Info,
            route = ROUTE_HELP,
        ),
        BottomNavItem(
            label = getString(context,R.string.screen_puzzles),
            icon = Icons.Filled.FileOpen,
            route = ROUTE_PUZZLES,
        ),
        BottomNavItem(
            label = getString(context, R.string.screen_settings),
            icon = Icons.Filled.Settings,
            route = ROUTE_SETTINGS,
        ),
        BottomNavItem(
            label = getString(context, R.string.screen_game),
            icon = Icons.Filled.Gamepad,
            route = ROUTE_GAME,
        )
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                navItems.forEach { item ->
                    OneNavigationBarItem(
                        item,
                        selectedLevel,
                        currentDestination,
                        navController,
                        toastHandler,
                        roomHolder,
                    )
                }
            }
        }
    ) { innerPadding ->
        // >> INITIAL DESTINATION:
        val language = "en" // where is our "currentLanguage"?
        val article = "index.md"
        val config = roomHolder.getRoomStatus()
        requireNotNull(config)
        val gotoAtStart = if(config.lastLevelId == null) {
            "$ROUTE_HELP/$language/$article" // ROUTE_HELP
        }else{
            "$ROUTE_GAME/${config.lastLevelId}"
        }
        // - ROUTE_LEVELS
        // - ROUTE_HELP
        // <<

        NavHost(
            navController = navController,
            startDestination = gotoAtStart,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = ROUTE_PUZZLES) {
                LevelsScreen(
                    levelsViewModel,
                    stateFlowHolder,
                    roomHolder,
                    navController
                )
            }

            composable(
                route = "$ROUTE_PUZZLES/{combo}/{world}",
                arguments = listOf(
                    navArgument("combo") { type = NavType.StringType },
                    navArgument("world") { type = NavType.StringType }
                )
            ) { backStackEntry ->

                val combo = backStackEntry.arguments?.getString("combo") ?: ""
                val world = backStackEntry.arguments?.getString("world") ?: ""
                stateFlowHolder.levelHierarchyStateFlow.setLevelHierarchy(combo, world)

                LevelsScreen(
                    levelsViewModel,
                    stateFlowHolder,
                    roomHolder,
                    navController,
//                    combo = combo,
//                    world = world,
                )
            }

            composable(route = ROUTE_SETTINGS) { SettingsScreen(stateFlowHolder = stateFlowHolder) }
            composable(route = ROUTE_GAME) {
                // maybe not needed, self-evident toast and renavigate! // toastHandler,
                AlwaysNeedSelectedLevel(navController)
            }
            composable(
                route = "${ROUTE_GAME}/{levelId}",
                arguments = listOf(navArgument("levelId") { type = NavType.LongType })
            ) { backstackEntry ->
                Log.i("MainAppView", "going to GAME ${backstackEntry.arguments?.getLong("levelId")}")
                ShowGameScreen(
                    backstackEntry,
                    gameViewModel,
                    stateFlowHolder,
                    levelProgress,
                    toastHandler,
                    navController,
                    roomHolder,
                )
            }
            composable(
                route = "${ROUTE_HELP}/{articleLang}/{articleName}",
                arguments = listOf(
                    navArgument("articleLang") { type = NavType.StringType },
                    navArgument("articleName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                Log.d("MainAppView", "backStackEntry{ ${backStackEntry.destination} | ${backStackEntry.arguments} }")

                val articleLang = backStackEntry.arguments?.getString("articleLang") ?: "en"
                val articleName = backStackEntry.arguments?.getString("articleName") ?: DocArticleProvider.DOC_FAILSAFE
                Log.d("MainAppView", "composing HELP:$articleLang:$articleName …")

                ShowDocArticleScreen(backStackEntry, assetManager, docArticleProvider, navController)
            }
            composable(
                route = ROUTE_HELP
            ) { backStackEntry ->
                Log.d("MainAppView", "backStackEntry{ ${backStackEntry.destination} | ${backStackEntry.arguments} }")

                val languageCode = LocaleListCompat.getDefault()[0]?.language ?: "en"
                DocScreen(/*context,*/ docArticleProvider, languageCode, Modifier, navController)
            }

            // fail-safe fallback – MUST BE last
            composable("{*path}") {
                //LevelsScreen(levelsViewModel,stateFlowHolder,navController)
                //DocScreen(docArticleProvider)
                navController.navigate(ROUTE_HELP) {
                    //popUpTo(navController.graph.findStartDestination().id) { inclusive = true } // in question
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }
}

@Composable
private fun ShowGameScreen(
    backstackEntry: NavBackStackEntry,
    gameViewModel: GameViewModel,
    stateFlowHolder: StateFlowHolder,
    levelProgress: LevelProgress,
    toastHandler: ToastHandler,
    //context: Context,
    navController: NavHostController,
    roomHolder: RoomHolder,
) {
    val passedLevelId = backstackEntry.arguments?.getLong("levelId")
    if (passedLevelId != null) {
        // this is triggered multiple times after a click on an item on the LevelsScreen
        //Log.i("MainAppView", "Displaying GameScreen for LevelId = $passedLevelId")
        GameScreen(
            modifier = Modifier,
            gameViewModel = gameViewModel,
            stateFlowHolder = stateFlowHolder,
            levelProgress = levelProgress,
            navController = navController,
            roomHolder = roomHolder,
        ) // levelId = passedLevelId,
    } else {
        LaunchNavigateToLevelsScreen(toastHandler, /*context, */navController)
    }
}

@Composable
private fun ShowDocArticleScreen(
    backstackEntry: NavBackStackEntry,
    assetManager: AssetManager,
    docArticleProvider: DocArticleProvider,
    navController: NavController,
) {
    val passedArticleLang = backstackEntry.arguments?.getString("articleLang") ?: "en"
    val passedArticleName = backstackEntry.arguments?.getString("articleName") ?: DocArticleProvider.DOC_FAILSAFE
    val articleText = docArticleProvider.getArticle (assetManager, name = passedArticleName, lang = passedArticleLang)
    DocArticle( markdownContent = articleText, language = passedArticleLang, navController = navController)
}

@Composable
private fun LaunchNavigateToLevelsScreen(
    toastHandler: ToastHandler,
    //context: Context,
    navController: NavHostController
) {
    //Log.e("MainAppView", "LevelId is null even with argument, redirecting.")
    LaunchedEffect(Unit) {
        toastHandler.showToast("Error: Level ID missing. Please select a level.")
        navController.navigate(ROUTE_PUZZLES) {
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = true
            } // in question
            launchSingleTop = true
            restoreState = true
        }
    }
}

@Composable
private fun AlwaysNeedSelectedLevel(
    //toastHandler: ToastHandler,
    navController: NavHostController
) {
    LaunchedEffect(Unit) {
        Log.d("MainAppView", "GameScreen without a LevelId --> pick a level")
//        toastHandler.showToast("You need to pick a level")
        navController.navigate(ROUTE_PUZZLES) {
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = true
            } // remove ROUTE_GAME from backstack // avoid loop if back is pressed

            launchSingleTop = true
            restoreState = true
        }
    }
    //Text("redirecting to select a level") // briefly shown
}

@Composable
private fun RowScope.OneNavigationBarItem(
    item: BottomNavItem,
    selectedLevel: Long?,
    currentDestination: NavDestination?,
    navController: NavHostController,
    toastHandler: ToastHandler,
    roomHolder: RoomHolder,
    //context: Context
) {
    val coroutineScope = rememberCoroutineScope()

    val isGameRoute = item.route == ROUTE_GAME
    val isEnabled = if (isGameRoute) selectedLevel != null else true

    NavigationBarItem(
        selected = isSelectedNavDestination(currentDestination, isGameRoute, selectedLevel, item),
        onClick = onNavItemClick(
            isEnabled,
            isGameRoute,
            selectedLevel,
            item,
            navController,
            toastHandler,
            roomHolder,
            coroutineScope,
            //context
        ),

        icon = { Icon(item.icon, contentDescription = item.label) },
        label = { Text(item.label) },
        enabled = isEnabled,
    )
}

@Composable
private fun onNavItemClick(
    isEnabled: Boolean,
    isGameRoute: Boolean,
    selectedLevel: Long?,
    item: BottomNavItem,
    navController: NavHostController,
    toastHandler: ToastHandler,
    roomHolder: RoomHolder,
    coroutineScope: CoroutineScope
    //context: Context
): () -> Unit = {
    //val coroutineScope = rememberCoroutineScope()
    if (isEnabled) {
        val usableRoute = if (isGameRoute && selectedLevel !== null) {
            "${ROUTE_GAME}/$selectedLevel" // FIXME combo/world/id should be the path
        } else {
            item.route
        }
        Log.d("MainAppView", "usableRoute first '$usableRoute'")
        if (isGameRoute && selectedLevel !== null)
        {
            coroutineScope.safeLaunch {
                Log.d("MainAppView", "setting current level to $selectedLevel.")
                roomHolder.setCurrentLevel(selectedLevel.toInt())
            }
        }
        Log.i("MainAppView", "usable route is '$usableRoute'")
        // LaunchedEffect(usableRoute) {}
        //LaunchedEffect(Unit) {
            Log.i("MainAppView", "route used is now '$usableRoute'")
            navController.navigate(usableRoute) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true // all others have inclusive = true here
                }
                launchSingleTop = true
                restoreState = true
            }
        //}
    } else { // if (isGameRoute)
        toastHandler.showToast("Please Pick Puzzle")
    }
    //return { Unit } // we're currently slightly confused about this … would like to purge it, but are stuck in a seems-required loop
}

@Composable
private fun isSelectedNavDestination(
    currentDestination: NavDestination?,
    isGameRoute: Boolean,
    selectedLevel: Long?,
    item: BottomNavItem
): Boolean = currentDestination?.hierarchy?.any { navDest ->
    val currentRoute = navDest.route
    if (isGameRoute) {
        currentRoute == ROUTE_GAME ||
                currentRoute == "${ROUTE_GAME}/${selectedLevel}"
    } else {
        currentRoute == item.route
    }
} == true