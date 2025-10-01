package de.flowtron.sokoban.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        RoomStatus::class,
        RoomLevel::class,
        //RoomConfigString::class,
        //RoomConfigInt::class
               ],
    version = 1,
    exportSchema = false
)
abstract class RoomDb : RoomDatabase() {
    abstract fun statusDao(): RoomStatusDao
    abstract fun levelDao(): RoomLevelDao
    //abstract fun configDao(): RoomConfigDao
}