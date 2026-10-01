package de.flowtron.sokoban.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown

@Composable
fun DocArticle(itemText: String, modifier: Modifier = Modifier) {
    //Text(text=itemText)
    Box(modifier = modifier.padding(2.dp)) {
        Markdown(itemText)
    }
    Text(" ")
}
/*
@Composable
fun MarkdownText(
    content: String,
    modifier: Modifier = Modifier
) {

}
 */


@Preview(showBackground = true)
@Composable
fun DocArticlePreview() {
    // get Asset HELP / "__INTERNAL_PLACEHOLDER_for_FAILSAFE__" // FIXME duplicate magic string
    DocArticle("Lorem ipsum, dolor sit amet …")
}