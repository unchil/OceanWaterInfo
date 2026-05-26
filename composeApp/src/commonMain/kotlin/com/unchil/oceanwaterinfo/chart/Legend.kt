package com.unchil.oceanwaterinfo

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import io.github.koalaplot.core.Symbol
import io.github.koalaplot.core.legend.ColumnLegend2
import io.github.koalaplot.core.legend.FlowLegend2
import io.github.koalaplot.core.legend.LegendLocation
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun Legend(
    layout: LayoutData,
    entries:List<String>,
    type:ColorPaletteType = ColorPaletteType.Pastel
){

    val colors = getColors(entries, type, legendColor = layout.legend.legendColor)

    val defaultSize = remember {  24.dp}
    val legendLocation = layout.legend.location
    val isVertical = legendLocation == LegendLocation.LEFT || legendLocation == LegendLocation.RIGHT

    val modifier = Modifier.padding(6.dp).then(
        if (isVertical) {
            // 높이 제한 조건 계산
            val maxHeight = layout.size.height * 0.8f
            val contentHeight = entries.size.toFloat() * defaultSize

            Modifier
                .let { if (contentHeight > maxHeight) it.height(maxHeight) else it }
                .verticalScroll(rememberScrollState())
        } else {
            Modifier.horizontalScroll(rememberScrollState())
        }
    )

    val bottonPadding = if (isVertical) 0.dp else 24.dp

    Surface(
        shadowElevation = 2.dp,
        modifier = Modifier.padding(bottom = bottonPadding),
        shape = RoundedCornerShape(6.dp),

    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            if(layout.legend.isTitle){
                LegendTitle(layout.legend.title)
            }

            Box(modifier = modifier) {
                when (layout.legend.location) {
                    LegendLocation.LEFT, LegendLocation.RIGHT, LegendLocation.NONE -> {

                        ColumnLegend2(
                            itemCount  = entries.size,
                            modifier = Modifier.padding(2.dp),
                            symbol = { i ->
                                Symbol(
                                    modifier = Modifier.size(8.dp),
                                    fillBrush = SolidColor(colors[entries[i]] ?: Color.Black),
                                )
                            },
                            label = { i ->
                                Text(entries[i])
                            },
                            value = {  },
                        )
                    }

                    LegendLocation.TOP, LegendLocation.BOTTOM -> {
                        FlowLegend2(
                            itemCount = entries.size,
                            symbol = { i ->
                                Symbol(
                                    modifier = Modifier.size(8.dp),
                                    fillBrush = SolidColor(colors[entries[i]] ?: Color.Black),
                                )
                            },
                            label = { i ->
                                Text(entries[i])
                            },
                        )




                    }

                }
            }
        }
    }

}
