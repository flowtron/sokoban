package de.flowtron.sokoban.game

data class LevelData (
    val data: List<List<Byte>>,
    val dimensions: Coordinates = Coordinates(data[0].size, data.size), //Pair<Int, Int> = Pair(data[0].size, data.size), // columns, rows
) {
    fun get(x: Int, y: Int): Int {
        return data[y][x].toInt()
    }
    fun get(at: Coordinates): Int {
        return data[at.y][at.x].toInt()
    }

    fun findPlayer(): Coordinates? {
        for (y in 0 until dimensions.y) {
            for (x in 0 until dimensions.x) {
                val cellByte = get(x,y)
                if (cellByte == Cell.PLAYER.id || cellByte == Cell.PLAYER_ON_GOAL.id) {
                    return Coordinates(x, y)
                }
            }
        }
        return null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LevelData

        return this.toString() == other.toString()
    }

    override fun hashCode(): Int {
        return this.toString().hashCode()
    }

    override fun toString(): String {
        return data.joinToString("\n") { row ->
            row.joinToString("") {
                Cell.charById(it.toInt()).toString()
            }
        }
    }
}