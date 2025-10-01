package de.flowtron.sokoban.di

import android.content.Context
import android.content.res.AssetManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Or ActivityComponent, etc. depending on scope
object AppModule {

//    @Provides
//    @Singleton // If ToastHandler should be a singleton
//    fun provideToastHandler(@ApplicationContext context: Context): ToastHandler {
//        return ToastHandler(context) // Assuming ToastHandler takes context
//    }

//    @Provides
//    @Singleton
//    fun provideToastHandler(): ToastHandler {
//        return ToastHandler()
//    }

    @Provides
    @Singleton
    fun provideAssetManager(@ApplicationContext context: Context): AssetManager {
        return context.assets
    }
}