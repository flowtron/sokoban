package de.flowtron.sokoban.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level")
data class RoomLevel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val combo: String? = null,
    val world: String? = null,
    val level: String? = null,

    val done: Boolean = false,
    val help: Boolean = false,

    val history: String? = null,
)