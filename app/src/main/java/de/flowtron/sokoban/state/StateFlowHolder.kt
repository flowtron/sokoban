package de.flowtron.sokoban.state

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StateFlowHolder @Inject constructor() {
    val configurationDoneStateFlow = ConfigurationDoneStateFlow()
    val renderStateFlow = RenderStateFlow()
    val gameToolStateFlow = GameToolStateFlow()
    val gameDataInfoStateFlow = GameDataInfoStateFlow()

    val mapFinishedStateFlow = MapFinishedStateFlow()

    val levelOriginalStateFlow = LevelOriginalStateFlow() // map state once loaded
    val levelDataStateFlow = LevelDataStateFlow() // map state of game
    //val levelHistoryStateFlow = LevelHistoryStateFlow() // history of moves
    val movementHistoryStateFlow = MovementHistoryStateFlow() // history of moves
    //val historyData

    val levelSolutionStateFlow = LevelSolutionStateFlow() // map state of solution
    val movementSolutionStateFlow = MovementSolutionStateFlow() // moves of solution

    val coordinatesStateFlow = CoordinatesStateFlow()
    val offsetStateFlow = OffsetStateFlow()
    val scaleStateFlow = ScaleStateFlow()
    val dragSensitivityStateFlow = DragSensitivityStateFlow()
}