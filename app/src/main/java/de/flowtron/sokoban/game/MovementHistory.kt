package de.flowtron.sokoban.game

data class MovementHistory (
    val data: List<Byte>,
) {
    fun get(step: Int): Int {
        return data[step].toInt()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MovementHistory

        return this.toString() == other.toString()
    }

    override fun hashCode(): Int {
        return this.toString().hashCode()
    }

    fun size(): Int {
        return data.size
    }

    override fun toString(): String {
        return data.joinToString("") { step ->
            when(step.toInt()) {
                0 -> "E"
                1 -> "N"
                2 -> "W"
                3 -> "S"
                else -> "?"
            }
        }
    }
    fun toDirections() =  this.toString()

}

