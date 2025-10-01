// In a new file, e.g., SokobanApplication.kt
package de.flowtron.sokoban

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AppApplication : Application() {
    // You can inject things here too if needed, but for now,
    // the annotation is the most important part.
}