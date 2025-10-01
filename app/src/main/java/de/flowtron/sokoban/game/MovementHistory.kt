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

    override fun toString(): String {
        return data.joinToString(",") { step ->
                Cell.charById(step.toInt()).toString()

        }
    }

    fun toDirections(): String {
        return data.joinToString("") { step ->
            val stepInt = step.toInt()
            var stepDir = "?"
            when(stepInt){
                0 -> stepDir = "E"
                1 -> stepDir = "N"
                2 -> stepDir = "W"
                3 -> stepDir = "S"
            }

            stepDir
        }
    }
}

