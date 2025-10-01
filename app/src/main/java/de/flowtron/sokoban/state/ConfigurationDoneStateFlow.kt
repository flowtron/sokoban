package de.flowtron.sokoban.state

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConfigurationDoneStateFlow {
    private val mutableOnboardedStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    fun setOnboarded(done: Boolean) {
        mutableOnboardedStateFlow.value = done
    }

    val done = mutableOnboardedStateFlow.asStateFlow()
    fun showOnboarded() {
        Log.d("StateFlowHolder", "Onboarded = ${done.value}")
    }
}