package de.flowtron.sokoban.game

class MovementParser {

    fun fromDirections(directions: String): MovementHistory {
        val result = mutableListOf<Byte>()
        directions.forEach { step ->
            when(step){
                'E' -> result.add(0)
                'N' -> result.add(1)
                'W' -> result.add(2)
                'S' -> result.add(3)
            }
        }
        return MovementHistory(result)
    }

}