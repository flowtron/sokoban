package de.flowtron.sokoban.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


/*fun getAllArticles() : List<String> {
    val assetReader = AssetReader(context)
    //val result: MutableMap<String, MutableMap<String, List<String>>> = mutableMapOf()
    val articles = assetReader.listSubdirectories(DOC_ASSET_DIR)
    return articles
}*/

//docArticleProvider: DocArticleProvider,

@Composable
fun DocScreen(
    articles: List<String>,
    modifier: Modifier = Modifier,
) {
    /*
    if(articles.isEmpty()){
        articles = getAllArticles()
    }
    */
    //val articles = docArticleProvider.getAllArticles()
    Box(modifier = modifier
        .fillMaxSize()
        .padding(0.dp)
        .background(Color(0xff1070a0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            articles.forEachIndexed { idx, articleTitle ->
                Box(
                    Modifier
                        .padding(
                            start = 4.dp,
                            end = 4.dp,
                            top = 0.dp,
                            bottom = 8.dp
                        )
                        .background(
                            // TODO dark/light themed
                            Color(200, 200, 200)
                        )
                ){
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 6.dp,
                                vertical = 12.dp
                            )
                    ) {
                        Text(
                            text = articleTitle,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Left,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        val isFinalArticle = idx == articles.lastIndex
                        val theHeight = if(isFinalArticle){ 16.dp }else{ 8.dp }
                        val modWithHeight = modifier.height(theHeight)
                        Spacer(modifier = modWithHeight)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DocScreenPreview() {
    val previewArticles : List<String> = listOf(
        "item 1",
        "item 2",
        "tutorial 1",
        "item alpha",
        "item beta",
        "tutorial gamma",
        "item 314",
        "item 200",
        "tutorial 112112112112112112112112112",
        "item alpha",
        "item beta",
        "tutorial gamma",
        "item item item",
        "whatever",
        "1",
    )
    DocScreen(
        articles = previewArticles,
        modifier = Modifier // Modifier.fillMaxWidth()
    )
}