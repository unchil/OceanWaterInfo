package com.unchil.oceanwaterinfo


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp


@Composable
fun CaptionText(text: String, modifier:Modifier=Modifier) {
    Text(text, fontStyle = FontStyle.Italic, style = MaterialTheme.typography.bodySmall, modifier=modifier)
}


@Composable
fun ChartTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}

@Composable
fun LegendTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        title,
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier,
    )
}

@Composable
fun AxisTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        title,
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier,
    )
}

@Composable
fun AxisLabel(
    label: String,
    modifier: Modifier = Modifier,
) {
    Text(
        label,
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
    )
}

@Composable
fun BoxPlotTooltips(
    text: String,
    textStyle: TextStyle? = null,
    modifier:Modifier = Modifier,

) {

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ){
        Text(
            text,
            style = textStyle ?: TextStyle.Default,
            modifier = Modifier
                .padding(horizontal = 6.dp).padding(vertical = 2.dp),
            color = Color.White
        )

    }


}

@Composable
fun HoverSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        shadowElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
        color = Color.LightGray,
        modifier = modifier.padding(padding),
    ) {
        Box(modifier = paddingMod) {
            content()
        }
    }
}

