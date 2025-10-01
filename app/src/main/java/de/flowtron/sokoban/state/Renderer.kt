package de.flowtron.sokoban.state

enum class Renderer {
    DRAW,
    TEXT,
    BOTH
}

/*
// actually .. we have a catch-all extension function for this and any other enum!
fun Renderer.next() : Renderer {
    val allValues = Renderer.entries
    val nextIndex = (this.ordinal + 1) % allValues.size
    return allValues[nextIndex]
}
*/