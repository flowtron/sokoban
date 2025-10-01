package de.flowtron.sokoban.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.Start
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.flowtron.sokoban.DRAG_SENSITIVITY_MAXIMUM
import de.flowtron.sokoban.state.StateFlowHolder
import de.flowtron.sokoban.ui.models.SettingsViewModel
import de.flowtron.sokoban.ui.settings.AppDetails
import de.flowtron.sokoban.ui.settings.DragSensitivity
import kotlin.math.max

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    stateFlowHolder: StateFlowHolder = StateFlowHolder(),
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = CenterHorizontally,
    ) {
        Text("Settings")
        DragSensitivity(stateFlowHolder)
        AppDetails()
    }
}

