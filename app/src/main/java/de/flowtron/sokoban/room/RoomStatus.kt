package de.flowtron.sokoban.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "status",
    foreignKeys = [
        ForeignKey(
            entity = RoomLevel::class,
            parentColumns = ["id"],
            childColumns = ["lastLevelId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["lastLevelId"], unique = true)]
)
data class RoomStatus(
    @PrimaryKey val id: Int = 1, // Singleton configuration
    val isInitialSetupDone: Boolean = false,
    val lastLevelId: Int? = null,
    val dragSensitivity: Long = 250L,
)

