package de.flowtron.sokoban.game

import android.util.Log

class MutableLevelData(
    val data: List<List<Byte>>
) {
    private val _data: MutableList<MutableList<Byte>> = data.map { it.toMutableList() }.toMutableList()

    private val rows: Int = _data.size
    private val columns: Int = if (_data.isNotEmpty()) _data[0].size else 0

    init {
        require(_data.all { it.size == columns }) { "All rows must have the same number of columns" }
    }

    fun get(x: Int, y: Int): Int {
        if(x in 0..<columns && y in 0..<rows){
            return _data[y][x].toInt()
        }
        return -1
    }
    fun get(at: Coordinates): Int {
        if(at.x in 0..<columns && at.y in 0..<rows) {
            return _data[at.y][at.x].toInt()
        }
        return -1
    }

    fun set(x: Int, y: Int, value: Int) {
        if(x in 0..<columns && y in 0..<rows){
            _data[y][x] = value.toByte()
            _data[y][x] = value.toByte()
        }
    }
    fun set(at: Coordinates, value: Int) {
        if(at.x in 0..<columns && at.y in 0..<rows) {
            _data[at.y][at.x] = value.toByte()
        }
    }

    fun expand(edge: Int) {
        Log.i("LevelData", "Expanding level data by $edge. Currently have ${_data.size} x ${_data[0].size}.")
        val filler = Cell.SPACE.id.toByte()
        // add edge rows above and below (of original size)
        for(y in 1..edge) {
            _data.add(0, MutableList(columns) { filler })
            _data.add(MutableList(columns) { filler })
        }
        // add edge columns left and right expanding to new size
        _data.forEach {
            for(i in 1..edge) it.add(0, filler)
            for(i in 1..edge) it.add(filler)
        }
        Log.i("LevelData", "Expanded level data to ${_data.size} x ${_data[0].size}.")
    }

    fun toLevelData(): LevelData {
        return LevelData(_data)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LevelData

        return this.toString() == other.toString()
    }

    override fun hashCode(): Int {
        return _data.toString().hashCode()
    }

    override fun toString(): String {
        return _data.joinToString("\n") { row ->
            row.joinToString("") {
                Cell.charById(it.toInt()).toString()
            }
        }
    }
}