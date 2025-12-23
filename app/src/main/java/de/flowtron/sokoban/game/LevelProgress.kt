package de.flowtron.sokoban.game

import android.util.Log
import de.flowtron.sokoban.game.Cell.BOX
import de.flowtron.sokoban.game.Cell.BOX_ON_GOAL
import de.flowtron.sokoban.game.Cell.GOAL
import de.flowtron.sokoban.game.Cell.PLAYER
import de.flowtron.sokoban.game.Cell.PLAYER_ON_GOAL
import de.flowtron.sokoban.game.Cell.SPACE
import de.flowtron.sokoban.game.Cell.WALL
import de.flowtron.sokoban.state.MovementHistoryStateFlow
import de.flowtron.sokoban.state.StateFlowHolder
import de.flowtron.sokoban.ui.ToastHandler
import javax.inject.Inject

class LevelProgress @Inject constructor(
    val stateFlowHolder: StateFlowHolder,
    val toastHandler: ToastHandler,
) {

    var withCommentary: Boolean = false
        private set

    fun setCommentary(value: Boolean) { withCommentary = value }

    fun checkForWin(map: LevelData) : Boolean {
        var result = false
        if(openGoals(map) == 0){
            Log.i("GameViewModel", "You have successfully finished the level.")
            //toastHandler.showToast("success")

            result = true
            // TODO: must be set if this win is in the officially running game, not solution or history stepping
            //stateFlowHolder.mapFinishedStateFlow.setMapFinished(true)
        }
        return result
    }

    // CAVEAT EMPTOR: you will get even results only, two for every humanly counted open goal.
    // oh, wait - this is probably a rule change of mine -- can there not be more boxes than goals, ..
    // .. which makes it not be a solution requirement
    fun unmatchedFields(map: LevelData?): Int? = map?.data?.flatten()?.count { it.toInt() == GOAL.id || it.toInt() == BOX.id }

    // OK, so now it should be concrete
    //fun openGoals(map: LevelData?): Int? = map?.data?.flatten()?.count { it.toInt() == GOAL.id }
    fun openGoals(map: LevelData?): Int? = map?.data?.flatten()?.count { it.toInt() in listOf(GOAL.id, PLAYER_ON_GOAL.id)}

    fun allowedToMove(map: LevelData, from: Coordinates, direction: Coordinates): Boolean {
        val to = from.add(direction)
        val cell = map.get(to.x, to.y)
        if(withCommentary) Log.v("GameViewModel", "From $from to $to where the map has ${Cell.labelById(cell)}")
        return when (cell) {
            WALL.id -> false
            SPACE.id -> true // always allowed to move onto space
            GOAL.id -> true // always allowed to move onto goal
            BOX_ON_GOAL.id -> allowedToPush(map, to, direction)
            BOX.id -> allowedToPush(map, to, direction)
            else -> false
        }
    }

    fun allowedToPush(map: LevelData, from: Coordinates, direction: Coordinates): Boolean {
        val to = from.add(direction)
        val cell = map.get(to.x, to.y)
        if(withCommentary) Log.v("GameViewModel", "Push from $from to $to where the map has ${Cell.labelById(cell)}")
        return when (cell) {
            SPACE.id -> true // always allowed to push onto space
            GOAL.id -> true // always allowed to push onto goal
            else -> false
        }
    }

    // , historyStateFlow: MovementHistoryStateFlow, coordinatesStateFlow: CoordinatesStateFlow
    fun performMove(map: LevelData, from: Coordinates, direction: Coordinates): LevelData {
        val to = from.add(direction)
        val cellTo = map.get(to)
        val cellFrom = map.get(from)

        if(withCommentary) Log.v("GameViewModel", "Move from $from to $to where the map has ${Cell.labelById(cellTo)} vacating ${Cell.labelById(cellFrom)}")

        val cellBehind = cellAfterPusherLeaves(cellFrom)
        val cellForward = when(cellTo){
            SPACE.id -> PLAYER.id
            GOAL.id -> PLAYER_ON_GOAL.id
            BOX_ON_GOAL.id -> PLAYER_ON_GOAL.id
            BOX.id -> PLAYER.id
            else -> PLAYER.id
        }

        val mutableMap = MutableLevelData(map.data)

        if (cellTo == BOX_ON_GOAL.id || cellTo == BOX.id) {
            performPush(to, direction, mutableMap)
        }
        mutableMap.set(from, cellBehind)
        mutableMap.set(to, cellForward)

        // TODO: has to be gleaned from the return value
        //stateFlowHolder.coordinatesStateFlow.setCoordinates(to)
        //stateFlowHolder.levelDataStateFlow.setLevelData(mutableMap.toLevelData())

        // TODO: has to be checked and appropriate actions taken
        //checkForWin(stateFlowHolder.levelDataStateFlow.levelData.value)

        if(withCommentary) Log.v("GameViewModel", "Moved from $from to $to, exposing ${Cell.labelById(cellBehind)} again.")

        return mutableMap.toLevelData()
    }

    private fun moveToCoordinates(move: Byte): Coordinates {
        return when(move.toInt()) {
            0 -> Coordinates(+1, 0)
            1 -> Coordinates(0, -1)
            2 -> Coordinates(-1, 0)
            3 -> Coordinates(0, +1)
            else -> Coordinates(0, 0)
        }
    }

    fun naive_performHistory(map: LevelData, moves: MovementHistory): LevelData {
        val mutableMap = MutableLevelData(map.data)
        var from = requireNotNull(map.findPlayer())

        if(withCommentary) Log.v("LevelProgress", "performing history of length ${moves.data.size}.")
        moves.data.forEach { move ->
            val direction = moveToCoordinates(move)
            val to = from.add(direction)
            val cellTo = map.get(to)
            val cellFrom = map.get(from)

            val cellBehind = cellAfterPusherLeaves(cellFrom)
            val cellForward = when(cellTo){
                SPACE.id -> PLAYER.id
                GOAL.id -> PLAYER_ON_GOAL.id
                BOX_ON_GOAL.id -> PLAYER_ON_GOAL.id
                BOX.id -> PLAYER.id
                else -> PLAYER.id
            }

            if (cellTo == BOX_ON_GOAL.id || cellTo == BOX.id) {
                val bufCommentary = withCommentary
                withCommentary = false
                performPush(to, direction, mutableMap)
                withCommentary = bufCommentary
            }
            mutableMap.set(from, cellBehind)
            mutableMap.set(to, cellForward)

            from = to
        }
        return mutableMap.toLevelData()
    }

    /*
     * 20250905 need to FIX performHistory
     * there can be corruption happening.
     *
     * Let's break it down.
     * a history can have N steps. the index can then be [0…N] inclusive.
     * from before the 1st to after the Nth step.
     *
     * we see a box shoved onto a goal as the final step did not render properly
     *
     */
    fun performHistory(map: LevelData, moves: MovementHistory): LevelData {
        var from = requireNotNull(map.findPlayer())
        val mutableMap = MutableLevelData(map.data)

        if(withCommentary) Log.v("LevelProgress", "performing history of length ${moves.data.size}.")

        moves.data.forEach { move ->
            val direction = moveToCoordinates(move)
            val to = from.add(direction)
            val cellTo = mutableMap.get(to)
            val cellFrom = mutableMap.get(from)

            val cellBehind = cellAfterPusherLeaves(cellFrom)
            val cellForward = when(cellTo){
                SPACE.id -> PLAYER.id
                GOAL.id -> PLAYER_ON_GOAL.id
                BOX_ON_GOAL.id -> PLAYER_ON_GOAL.id
                BOX.id -> PLAYER.id
                else -> PLAYER.id
            }

            Log.d("LevelProgress", "$from -> $to : $cellFrom / $cellTo")
            if (cellTo == BOX_ON_GOAL.id || cellTo == BOX.id) {
                val bufCommentary = withCommentary
                withCommentary = false
                performPush(to, direction, mutableMap)
                withCommentary = bufCommentary
            }//else{ Log.d("LevelProgress", "cellTo: $cellTo") }

            mutableMap.set(from, cellBehind)
            mutableMap.set(to, cellForward)

            if(withCommentary)
                Log.i("LevelProgress", "$mutableMap")

            from = to
        }
        return mutableMap.toLevelData()
    }

    fun pushIntoHistory(historyStateFlow: MovementHistoryStateFlow, direction: Coordinates) {
        val newHistory: MutableList<Byte> = historyStateFlow.movementHistory.value.data.toMutableList()
        // EAST NORTH WEST SOUTH 0 1 2 3
        var stepByte: Byte = 0x7
        when (direction.x) {
            1 -> stepByte = 0x0
            -1 -> stepByte = 0x2
            0 -> when (direction.y) {
                1 -> stepByte = 0x3
                -1 -> stepByte = 0x1
            }
        }
        newHistory.add(stepByte)
        val makeHistory = MovementHistory(newHistory)
        historyStateFlow.setMovementHistory(makeHistory)
        historyStateFlow.setIndex(makeHistory.data.size)
        // think of it like this:
        // 0 means "before 1st step"
        // 1 means "after 1st step"
        // N means "after Nth step"

        //if(withCommentary)
        //Log.d("GameViewModel", "HISTORY: ${makeHistory.toDirections()}")
    }

    private fun cellAfterPusherLeaves(cellFrom: Int): Int {
        return if (cellFrom == PLAYER_ON_GOAL.id) {
            GOAL.id
        } else {
            SPACE.id
        }
    }

    private fun cellAfterBoxLeaves(cellFrom: Int): Int {
        return if (cellFrom == BOX_ON_GOAL.id) {
            GOAL.id
        } else {
            SPACE.id
        }
    }

    private fun cellWhenBoxArrives(cellTo: Int): Int {
        return if (cellTo == GOAL.id) {
            BOX_ON_GOAL.id
        } else {
            BOX.id
        }
    }

    private fun performPush(from: Coordinates, direction: Coordinates, mutableMap: MutableLevelData) {
        val to = from.add(direction)

        val cellTo = mutableMap.get(to)
        val cellFrom = mutableMap.get(from)

        if(withCommentary) Log.v("GameViewModel", "Push from $from to $to where the map has ${Cell.labelById(cellTo)} and exposing ${Cell.labelById(cellFrom)}")

        val cellBehind = cellAfterBoxLeaves(cellFrom)
        val cellForward = cellWhenBoxArrives(cellTo)

        mutableMap.set(from, cellBehind)
        mutableMap.set(to, cellForward)
    }
}