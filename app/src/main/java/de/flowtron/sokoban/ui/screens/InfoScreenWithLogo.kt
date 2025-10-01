package de.flowtron.sokoban.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
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
    multilineText: String,
    copyrightText: String,
    urlText: String,
    modifier: Modifier = Modifier,
    onMultilineTextChange: (String) -> Unit = {}
) {
    Box(modifier = modifier.fillMaxSize()) {
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
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = line2Text,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                readOnly = true,
                value = multilineText,
                onValueChange = onMultilineTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 80.dp),
                label = { Text("Details") }
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
    InfoScreenWithLogo(
        logo = logo,
        line1Text = "flowtron provides",
        line2Text = "S O K O B A N",
        multilineText = "This is a longer description of the company or application. It can span multiple lines and provide more detailed information to the user.",
        copyrightText = "© 2025 Florian 'flowtron' Schulte",
        urlText = "flowtron.de"
    )
}