package de.flowtron.sokoban.game

import de.flowtron.sokoban.R
import de.flowtron.sokoban.game.Cell.BOX
import de.flowtron.sokoban.game.Cell.BOX_ON_GOAL
import de.flowtron.sokoban.game.Cell.GOAL
import de.flowtron.sokoban.game.Cell.PLAYER
import de.flowtron.sokoban.game.Cell.PLAYER_ON_GOAL

enum class Cell(val id: Int, val char: Char = ' ', val drawable: Int, val label: String ) {
    WALL(id = 1, char = '#', drawable = R.drawable.mt_wall, label = "wall"),
    PLAYER(id = 2, char = '@', drawable = R.drawable.mt_push, label = "player"),
    PLAYER_ON_GOAL(id = 3, char = '+', drawable = R.drawable.mt_push, label = "player on goal"),
    BOX(id = 4, char = '$', drawable = R.drawable.mt_box, label = "box"),
    BOX_ON_GOAL(id = 5, char = '*', drawable = R.drawable.mt_good, label = "box on goal"),
    GOAL(id = 6, char = '.', drawable = R.drawable.mt_goal, label = "goal"),
    SPACE(id = 7, char = ' ', drawable = R.drawable.mt_floor, label = "space");

    companion object {
        fun fromId(id: Int): Cell? = entries.find { it.id == id }

        fun labelById(id: Int): String {
            val cell = fromId(id)
            return cell?.label ?: "(invalid cell:$id)"
        }
        fun charById(id: Int): Char {
            val cell = fromId(id)
            return cell?.char ?: '␀'
        }
        //fun isPlayer()
    }
}

fun Cell.isPlayer() : Boolean = this == PLAYER || this == PLAYER_ON_GOAL
fun Cell.isBox() : Boolean = this == BOX || this == BOX_ON_GOAL
fun Cell.isGoal() : Boolean = this == GOAL || this == BOX_ON_GOAL