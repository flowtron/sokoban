package de.flowtron.sokoban.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.flowtron.sokoban.R

@Composable
fun InfoScreenWithLogo(
    logo: Painter,
    line1Text: String,
    line2Text: String,
    multilineText1: String,
    multilineText2: String,
    copyrightText: String,
    urlText: String,
    modifier: Modifier = Modifier,
    onMultilineTextChange: (String) -> Unit = {}
) {
    Box(modifier = modifier.fillMaxSize().background(Color(0xFF10a0c0))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(.1f))

            Image(
                painter = logo,
                contentDescription = "Logo",
                modifier = Modifier.size(128.dp)
            )

            Spacer(modifier = Modifier.weight(.1f))

            Text(
                text = line1Text,
                fontSize = 32.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = line2Text,
                fontSize = 40.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                readOnly = true,
                value = multilineText1,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 80.dp),
                textStyle = TextStyle(fontSize = 18.sp),
                label = { Text(
                        text = "\uD83D\uDCE6 push boxes onto goals \uD83D\uDCE6",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )

            TextField(
                readOnly = true,
                value = multilineText2,
                onValueChange = onMultilineTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 80.dp),
                textStyle = TextStyle(fontSize = 14.sp),
            )

            Spacer(modifier = Modifier.weight(1f))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 1.dp, vertical = 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = copyrightText, fontSize = 12.sp)
            Text(text = urlText, fontSize = 12.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InfoScreenWithLogoPreview() {
    val logo = painterResource(id = R.drawable.splashscreen_logo)
    val multilineText = """
        
        Enjoy the game!
        
        You can only push, not pull boxes. The boxes and goals are all the same. Least pushes, then moves wins in comparison. 
        
        
        🫶 for BaZi, Fritz and all you others 😃 
        
    """.trimIndent()
    InfoScreenWithLogo(
        logo = logo,
        line1Text = "flowtron provides",
        line2Text = "S O K O B A N",
        multilineText1 = multilineText,
        multilineText2 = "… the app is doing .. something or other .. well, well, well …",
        copyrightText = "©2025 Florian 'flowtron' Schulte",
        urlText = "flowtron.de"
    )
}