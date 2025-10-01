package de.flowtron.sokoban.ui.models

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.flowtron.sokoban.state.StateFlowHolder
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val stateFlowHolder: StateFlowHolder,
) : ViewModel() {

    // save settings to roomDB

}