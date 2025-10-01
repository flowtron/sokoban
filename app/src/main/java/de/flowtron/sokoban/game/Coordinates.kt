package de.flowtron.sokoban.game

data class Coordinates(val x: Int, val y: Int) {

    override fun toString(): String = "($x, $y)"

    fun add(there: Coordinates): Coordinates {
        return Coordinates(x + there.x, y + there.y)
    }
}



