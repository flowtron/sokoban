package de.flowtron.sokoban.ui

import android.content.Context
import android.widget.Toast
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToastHandler @Inject constructor(
    @ApplicationContext private val context: Context // Inject context
) {

    // pre injection
//    fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
//        Toast.makeText(context, message, duration).show()
//    }

    fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }
}