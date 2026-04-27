package com.unchil.oceanwaterinfo.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.unchil.oceanwaterinfo.BoxPlotTooltips
import com.unchil.oceanwaterinfo.ChartDataList
import com.unchil.oceanwaterinfo.ChartType
import com.unchil.oceanwaterinfo.formatLongToDateTime
import com.unchil.oceanwaterinfo.getColors
import io.github.koalaplot.core.bar.DefaultBar
import io.github.koalaplot.core.bar.DefaultBarPosition
import io.github.koalaplot.core.bar.DefaultVerticalBarPlotEntry
import io.github.koalaplot.core.bar.VerticalBarPlot
import io.github.koalaplot.core.bar.VerticalBarPlotEntry
import io.github.koalaplot.core.xygraph.Point
import io.github.koalaplot.core.xygraph.XYGraphScope

@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XYGraphScope<Double, Float>.VerticalTooltipBar(
    data: ChartDataList,
    type: ChartType,
    onHoverEvent:((Int)->Unit)? = null
){
    val colors = getColors(data.map { triple -> triple.first })
    val toolTipWidth = remember { 120.dp }

    val range = when(type){
        ChartType.Area -> {
            val baseLineData = data.flatMap { it.second }
            val areaLineData = data.flatMap { it.third["rm005"] as List<Point<Double, Float>> }
            val allPoints = baseLineData + areaLineData
            val yMax = allPoints.maxOf { it.y }
            val yMin = allPoints.minOf { it.y }
            yMin - 1.0f..yMax + 1.0f
        }
        else -> {
            val yMax = data.maxOf { entry -> entry.second.maxOf { point -> point.y } }
            val yMin = data.minOf { entry -> entry.second.minOf { point -> point.y } }
            yMin - 1.0f..yMax + 1.0f
        }
    }


    val values: List<VerticalBarPlotEntry<Double, Float>> = data.first().second.map { point ->
        DefaultVerticalBarPlotEntry(  point.x,
            DefaultBarPosition(range.start, range.endInclusive)
        )
    }
    val interactionSources = remember( data.first().second.size) {
        List(data.first().second.size) { MutableInteractionSource() }
    }

    // 1. TooltipState를 인덱스별로 기억하도록 수정 (매우 중요)
    val tooltipStates = List(data.first().second.size) { index ->
        rememberTooltipState()
    }

    BoxWithConstraints {

        // 1. 하나의 슬롯당 할당된 너비 (Dp)
        val chkSize = this.maxWidth / data.first().second.size

        // 2. 원하는 최대 막대 너비 (예: 12.dp)
        val maxBarWidth = remember{ 12.dp }

        // 3. 공식: x = maxBarWidth / chkSize
        // (chkSize가 0일 경우를 대비해 coerceAtMost(1f)로 비율의 최대값을 1.0으로 제한합니다)
        val barWidth = (maxBarWidth / chkSize).coerceIn(0.2f, 0.9f)

        VerticalBarPlot(
            values,
            modifier = Modifier,
            bar = { index, _, _ ->

                val interactionSource = interactionSources[index]
                val isHovered by interactionSource.collectIsHoveredAsState()

                LaunchedEffect(isHovered) {
                    if (isHovered)  {
                        onHoverEvent?.invoke(index)
                    } else {
                        onHoverEvent?.invoke(-1)
                    }
                }

                val modifier = if (isHovered) {
                    Modifier.zIndex(1f)
                        .border(1.dp, color= DarkGray, ShapeDefaults.Small)
                }else {
                    Modifier.zIndex(0f)
                }

                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                        if (index > (values.size / 2) ) TooltipAnchorPosition.Start else TooltipAnchorPosition.End
                    ),
                    tooltip = {
                        Box(
                            modifier = Modifier
                                .wrapContentSize(unbounded = true)
                                .background(  color = Color.Transparent,  shape = ShapeDefaults.Medium  ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(){
                                val modifier = Modifier
                                    .width(toolTipWidth)
                                    .padding(vertical = 1.dp)
                                    .border(1.dp, color= DarkGray, ShapeDefaults.Small)
                                    .background( color = DarkGray, shape = ShapeDefaults.Small)

                                val textStyleTitle = TextStyle(
                                    color = Color.White,
                                    fontSize =  12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
                                    textAlign = TextAlign.Center
                                )
                                val textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize =  12.sp,
                                    fontWeight = FontWeight.Light,
                                    textAlign = TextAlign.Start
                                )
                                val text = formatLongToDateTime(values[index].x)

                                BoxPlotTooltips(
                                    text,
                                    textStyleTitle,
                                    modifier
                                )

                                when(type){
                                    ChartType.Area -> {
                                        val sortedEntries = data.map { triple ->
                                            triple.first to (
                                                    Pair( triple.second.getOrNull(index)?.y ?: 0f,
                                                        (triple.third["rm005"] as List<Point<Double, Float>> ).getOrNull(index)?.y ?: 0f
                                                    )
                                                    )
                                        }.sortedByDescending { it.second.first }

                                        sortedEntries.forEach {  (observatory, value) ->
                                            BoxPlotTooltips(
                                                "${observatory} : ${value.first} ~ ${value.second}",
                                                textStyle,
                                                modifier.background( color = colors[observatory] as Color, shape = ShapeDefaults.Small),
                                            )
                                        }
                                    }
                                    else -> {
                                        val sortedEntries = data.map { triple ->
                                            triple.first to (triple.second.getOrNull(index)?.y ?: 0f)
                                        }.sortedByDescending { it.second }

                                        sortedEntries.forEach {  (observatory, value) ->
                                            BoxPlotTooltips(
                                                "${observatory} : ${value}",
                                                textStyle,
                                                modifier.background( color = colors[observatory] as Color, shape = ShapeDefaults.Small),
                                            )
                                        }
                                    }

                                }

                            }
                        }
                    },
                    state = tooltipStates[index],
                ){
                    DefaultBar(
                        brush = SolidColor( if (isHovered) Color.LightGray.copy(0.2f) else Color.Transparent),
                        modifier = modifier.hoverable(interactionSource = interactionSource)
                    )

                }

            },
            barWidth = barWidth,


            )


    }

}
