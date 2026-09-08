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

    /*
     * different LEVEL states
     * - levelOriginal : original map
     * - levelData     : current map
     * - levelSolution : current map of solution
     *
     * original is for comparison/reset, we switch gameplay/solution as displayed per active tool (InteractionControls)
     *
     * different MOVEMENT states
     * - movementHistory : gameplay history
     * - movementSolution : solution history
     *
     */
    val levelOriginalStateFlow = LevelOriginalStateFlow()
    val levelDataStateFlow = LevelDataStateFlow()
    val movementHistoryStateFlow = MovementHistoryStateFlow()

    val levelSolutionStateFlow = LevelSolutionStateFlow()
    val movementSolutionStateFlow = MovementSolutionStateFlow()

    val coordinatesStateFlow = CoordinatesStateFlow()
    val offsetStateFlow = OffsetStateFlow()
    val scaleStateFlow = ScaleStateFlow()

    val dragSensitivityStateFlow = DragSensitivityStateFlow()
}