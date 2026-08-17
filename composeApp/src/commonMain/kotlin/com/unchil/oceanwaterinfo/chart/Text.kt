package com.unchil.oceanwaterinfo


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.koalaplot.core.util.VerticalRotation
import io.github.koalaplot.core.util.rotateVertically


val xTitle: @Composable ((text: String) -> Unit) = {
    Box( modifier = Modifier.fillMaxWidth(),  contentAlignment = Alignment.Center ) {
        AxisTitle(it, paddingMod)
    }
}
val yTitle: @Composable ((text: String) -> Unit) = {
    Box( modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
        AxisTitle( it,  modifier = paddingMod.rotateVertically(VerticalRotation.COUNTER_CLOCKWISE))
    }
}

val description: @Composable (ColumnScope.(text: String) -> Unit) = {
    Text(
        text = it,
        modifier = Modifier.fillMaxWidth().padding( 8.dp),
        textAlign = TextAlign.Start
    )
    HorizontalDivider(modifier = Modifier.padding(10.dp))
}

val caption: @Composable ( (text: String,  contentAlignment: Alignment) -> Unit ) = { text, contentAlignment ->
    Box( modifier = Modifier.fillMaxSize(), contentAlignment = contentAlignment) {
        Text(text, fontStyle = FontStyle.Italic, style = MaterialTheme.typography.bodySmall, modifier=Modifier.fillMaxWidth(), textAlign= TextAlign.Center)
    }
}


@Composable
fun CaptionText(text: String, modifier:Modifier=Modifier.fillMaxWidth(), textAlign: TextAlign? = null,) {
    Text(text, fontStyle = FontStyle.Italic, style = MaterialTheme.typography.bodySmall, modifier=modifier, textAlign=textAlign)
}

@Composable
fun NotFoundData(){
    Text( "서버로부터 데이터를 로드하지 못했습니다.", color = Color.Red, )
}

@Composable
fun DataLoading(){
    Text( "서버로부터 데이터를 로드하고 있습니다.", color = Color.Blue, )
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
                .padding(horizontal = 6.dp).padding(vertical = 2.dp)
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

