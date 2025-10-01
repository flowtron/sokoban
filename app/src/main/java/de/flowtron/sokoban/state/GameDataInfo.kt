package de.flowtron.sokoban.state

data class GameDataInfo(
    val id: Long? = null,
    val combo: String,
    val world: String,
    val level: String,
) {

    // TODO: clarify this is set before loading .. which seems unwise
    // we should have a container for the sane current state and a trigger for requesting something to replace it
    fun getLevelFilepath(): String {
        val prefix = getFilepath()
        return "$prefix.l"
    }

    fun getSolutionFilepath(): String {
        val prefix = getFilepath()
        return "$prefix.s"
    }

    private fun getFilepath(): String = "combos/$combo/$world/$level"
}