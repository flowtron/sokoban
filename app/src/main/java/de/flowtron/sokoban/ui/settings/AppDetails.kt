package de.flowtron.sokoban.ui.settings

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.Start
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
//import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.flowtron.sokoban.state.StateFlowHolder
import de.flowtron.sokoban.BuildConfig
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone
import kotlin.text.format
import kotlin.time.ExperimentalTime
//import kotlin.time.Instant

@Composable
fun AppDetails(/*stateFlowHolder: StateFlowHolder*/) {
    val modifier = Modifier.fillMaxWidth()
    Row(
        modifier = modifier
            .padding(vertical = 8.dp, horizontal = 16.dp),
    ) {
        Column(
            modifier = modifier,
            horizontalAlignment = CenterHorizontally,
        ) {
            Text(
                modifier = modifier.align(alignment = Start),
                fontWeight = Bold,
                text = "App Details"
            )
            Text(
                modifier = modifier,
                text = "Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
            )
            Text(
                modifier = modifier,
                text = "${BuildConfig.BUILD_TYPE} build at ${BuildConfig.BUILD_DATE}"
            )
            Text(
                modifier = modifier,
                text = getFormattedBuildDate(BuildConfig.BUILD_DATE, "yyyyMMdd HH:mm")
            )
        }
    }
}

//@OptIn(ExperimentalTime::class)
fun getFormattedBuildDate(buildTimestampString: String, formatPattern: String): String {
    return try {
        val timestampMillis = buildTimestampString.toLong()
        val instant = Instant.ofEpochMilli(timestampMillis)
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())

        //val instant = Instant.fromEpochMilliseconds(timestampMillis) //  Instant.ofEpochMilli(timestampMillis)
        //val instant = Instant.fromEpochMilliseconds(timestampMillis)
        //val localDateTime = instant.
        //val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            //LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            //LocalDateTime.ofInstant(instant, ZoneId.systemDefault()) // ZoneId.of("UTC")
        val formatter = DateTimeFormatter.ofPattern(
            formatPattern,
            Locale.getDefault()
        ) // Or a specific Locale
        localDateTime.format(formatter)
    } catch (e: NumberFormatException) {
        // Handle cases where the string might not be a valid long
        Log.e("BuildDate", "Error parsing build timestamp string: $buildTimestampString", e)
        "Invalid Date"
    } catch (e: Exception) {
        // Handle other potential formatting errors
        Log.e("BuildDate", "Error formatting build date: $buildTimestampString", e)
        "Error Formatting Date"
    }
}