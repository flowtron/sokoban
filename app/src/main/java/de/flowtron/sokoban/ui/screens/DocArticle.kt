package de.flowtron.sokoban.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mikepenz.markdown.m3.Markdown

@Composable
fun DocArticle(
    markdownContent: String,
    language: String,
    navController: NavController
) {
    val customUriHandler = object : UriHandler {
        override fun openUri(uri: String) {
            if (uri.endsWith(".md")) {
                //val fileName = uri.removeSuffix(".md")
                navController.navigate("help/$language/$uri")
            } else {
                //super.openUri(uri)
                Log.e("DocArticle", "Can not navigate to '$uri'.")
            }
        }
    }

    CompositionLocalProvider(LocalUriHandler provides customUriHandler) {
        Markdown(content = markdownContent)
    }
}


/*
@Preview(showBackground = true)
@Composable
fun DocArticlePreview() {
    // get Asset HELP / "__INTERNAL_PLACEHOLDER_for_FAILSAFE__" // FIXME duplicate magic string
    DocArticle("Lorem ipsum, dolor sit amet …", "de", navController)
}
*/