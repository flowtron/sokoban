package de.flowtron.sokoban.state

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LevelHierarchyStateFlow {
    private val mutableLevelHierarchyStateFlow: MutableStateFlow<Pair<String, String>> = MutableStateFlow(Pair("",""))
    fun setLevelHierarchy(combo: String, world: String) {
        mutableLevelHierarchyStateFlow.value = Pair(combo, world)
    }

    val hierarchy = mutableLevelHierarchyStateFlow.asStateFlow()
    fun showLevelHierarchy() {
        Log.d("StateFlowHolder", "Hierarchy = ${hierarchy.value}")
    }
}