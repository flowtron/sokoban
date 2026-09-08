package de.flowtron.sokoban.state

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConfigurationDoneStateFlow {
    private val mutableConfigurationDoneStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    fun setDone(done: Boolean) {
        mutableConfigurationDoneStateFlow.value = done
    }

    val done = mutableConfigurationDoneStateFlow.asStateFlow()
    fun showConfigurationDone() {
        Log.d("StateFlowHolder", "ConfigurationDone = ${done.value}")
    }
}