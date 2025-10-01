package de.flowtron.sokoban.ui.models

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.flowtron.sokoban.ui.ToastHandler
import javax.inject.Inject

@HiltViewModel
class MainAppViewModel @Inject constructor(
    val toastHandler: ToastHandler,
//    val liveDataHolder: LiveDataHolder
) : ViewModel() {
    // You can add LiveData, StateFlow, or functions here
    // that MainAppView might need to interact with.

//    fun doSomethingAndShowToast() {
//        toastHandler.showToast("something happened")
//    }
}