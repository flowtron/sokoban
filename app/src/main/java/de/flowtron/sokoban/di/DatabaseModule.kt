package de.flowtron.sokoban.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.flowtron.sokoban.room.RoomDb
import de.flowtron.sokoban.room.RoomLevelDao
import de.flowtron.sokoban.room.RoomStatusDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Lives as long as the application
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): RoomDb {
        return Room.databaseBuilder(
            appContext,
            RoomDb::class.java,
            "flowtron_sokoban"
        ).build()
    }

    @Provides
    @Singleton
    fun provideStatusDao(appRoomDb: RoomDb): RoomStatusDao {
        return appRoomDb.statusDao()
    }

    @Provides
    @Singleton
    fun provideLevelDao(appRoomDb: RoomDb): RoomLevelDao {
        return appRoomDb.levelDao()
    }
}