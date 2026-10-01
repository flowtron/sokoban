package de.flowtron.sokoban.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun DocArticle(itemText: String) {
    Text(text=itemText)
}


@Preview(showBackground = true)
@Composable
fun DocArticlePreview() {
    // get Asset HELP / "__INTERNAL_PLACEHOLDER_for_FAILSAFE__" // FIXME duplicate magic string
    DocArticle("Lorem ipsum, dolor sit amet …")
}