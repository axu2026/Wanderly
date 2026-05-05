package com.example.wanderly.ui.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// for home screen section titles, turn white if over background image
@Composable
fun AdaptiveTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    var color by remember { mutableStateOf(Color.Unspecified) }
    var useShadow by remember { mutableStateOf(false) }
    
    val darkColor = MaterialTheme.colorScheme.onSurface
    val lightColor = Color.White
    
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val textShadow = Shadow(
        color = Color.Black.copy(alpha = 0.6f),
        offset = Offset(0f, 4f),
        blurRadius = 10f
    )

    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge.copy(
            shadow = if (useShadow) textShadow else null
        ),
        fontWeight = FontWeight.SemiBold,
        color = color.takeOrElse { darkColor },
        modifier = modifier.onGloballyPositioned { coords ->
            val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }
            val y = coords.positionInWindow().y
            // If the title is in the top 30% of the screen, use light color + shadow
            val isTopHalf = y < screenHeight * 0.3f
            color = if (isTopHalf) lightColor else darkColor
            useShadow = isTopHalf
        }
    )
}
