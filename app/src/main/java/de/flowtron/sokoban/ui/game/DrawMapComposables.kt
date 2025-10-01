package de.flowtron.sokoban.ui.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.flowtron.sokoban.game.Cell
import de.flowtron.sokoban.game.Coordinates

@Composable
fun DrawMapTextFromId(text: String, modifier: Modifier, color: Color) {
    Text(
        text = text,
        modifier = modifier,
        textAlign = TextAlign.Center,
        maxLines = 1,
        color = color
    )
}

//@Composable
//fun DrawMapTileFromCell(cell: Cell, modifier: Modifier) {
//    DrawMapTileDrawable(cell.drawable, modifier)
//}

@Composable
fun DrawMapTileFromId(tile: Int, modifier: Modifier) {
    val cell = Cell.entries.first { it.id == tile }
    DrawMapTileDrawable(cell.drawable, modifier)
}

//@Composable
//fun DrawMapTileFromId(tile: Byte, modifier: Modifier) {
//    val cell = Cell.entries.first { it.id.toByte() == tile }
//    DrawMapTileDrawable(cell.drawable, modifier)
//}

@Composable
fun DrawMapTileDrawable(drawable: Int?, modifier: Modifier) {
    if (drawable != null) {
        Image(
            modifier = modifier,
            painter = painterResource(drawable),
            contentDescription = "MapTile",
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
fun CellAsText(
    cell: Cell,
    modifier: Modifier,
    innerCoordinates: Coordinates,
    rowIndex: Int,
    cellIndex: Int
) {

    val colorAny = Color(.5f, .5f, .5f, 1.0f)
    val colorCur = Color(.25f, .65f, .75f, .84f)
    DrawMapTextFromId(
        text = cell.char.toString(),
        modifier = modifier
            .border(
                width = 1.dp,
                color = Color(0.9f, 0.9f, 0.9f, .1f)
            ),
        color = if (cellIndex == innerCoordinates.x && rowIndex == innerCoordinates.y) colorCur else colorAny
    )
}