package de.flowtron.sokoban.state

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StateFlowHolder @Inject constructor() {
    val configurationDoneStateFlow = ConfigurationDoneStateFlow()

    val levelHierarchyStateFlow = LevelHierarchyStateFlow()

    val renderStateFlow = RenderStateFlow()
    val gameToolStateFlow = GameToolStateFlow()
    val gameDataInfoStateFlow = GameDataInfoStateFlow()

    val mapFinishedStateFlow = MapFinishedStateFlow()

    val levelOriginalStateFlow = LevelOriginalStateFlow() // state of map (original)

    val levelDataStateFlow = LevelDataStateFlow() // state of map of game
    val movementHistoryStateFlow = MovementHistoryStateFlow() // history of moves of game

    val levelSolutionStateFlow = LevelSolutionStateFlow() // state of map of solution
    val movementSolutionStateFlow = MovementSolutionStateFlow() // history of moves of solution

    val coordinatesStateFlow = CoordinatesStateFlow()
    val offsetStateFlow = OffsetStateFlow()
    val scaleStateFlow = ScaleStateFlow()

    val dragSensitivityStateFlow = DragSensitivityStateFlow()
}