package com.unchil.oceanwaterinfo.chart

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.unchil.oceanwaterinfo.ChartDataList
import com.unchil.oceanwaterinfo.ChartType
import com.unchil.oceanwaterinfo.getColors
import io.github.koalaplot.core.line.AreaBaseline
import io.github.koalaplot.core.line.AreaPlot2
import io.github.koalaplot.core.line.LinePlot2
import io.github.koalaplot.core.style.AreaStyle
import io.github.koalaplot.core.style.LineStyle
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.Point
import io.github.koalaplot.core.xygraph.XYGraphScope

@OptIn(ExperimentalMaterial3Api::class, ExperimentalKoalaPlotApi::class)
@Composable
fun XYGraphScope<Double, Float>.AreaLineChart(
    data: Any,
    usableTooltips: Boolean = false,
    usableSymbol: Boolean = true,
) {

    val data = (data as ChartDataList)

    val colors = getColors(data.map { triple -> triple.first })

    val isVisibleSymbol = remember { mutableStateOf(0) }

    val onHoverEvent = { index: Int ->
        isVisibleSymbol.value = index
    }


    if (usableTooltips) {
        VerticalTooltipBar(data, ChartType.Area, onHoverEvent)
    }

    data.forEachIndexed { index, triple ->


        val strokeWidth = remember { mutableStateOf(1.dp) }
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val isUsableSymbolTooltips by interactionSource.collectIsHoveredAsState()
        strokeWidth.value = if (isPressed) 2.dp else 0.5.dp


        val infiniteTransition = rememberInfiniteTransition()
        val phase by infiniteTransition.animateFloat(
            initialValue = 20f,
            targetValue = 0f, // 패턴의 총 합(20+10)만큼 이동하면 부드럽게 반복됨
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )

        val phase2 by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 20f, // 패턴의 총 합(20+10)만큼 이동하면 부드럽게 반복됨
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )


        LinePlot2(
            data = triple.second,
            lineStyle = LineStyle(
                brush = SolidColor(colors[triple.first] ?: Color.Black),
                strokeWidth = strokeWidth.value,
                // [선 길이, 공백 길이] 순서로 입력 (단위: 픽셀)
                pathEffect = PathEffect.dashPathEffect(
                    intervals = floatArrayOf(10f, 10f), // 10px 그리고 10px 띔
                    phase = phase
                ),
                alpha = 1.0f,
            ),

        ) //LinePlot2




        AreaPlot2(
            data =  triple.third["rm005"] as List<Point<Double, Float>>,
            lineStyle = LineStyle(
                brush = SolidColor(colors[triple.first] ?: Color.Black),
                strokeWidth = strokeWidth.value,
                // [선 길이, 공백 길이] 순서로 입력 (단위: 픽셀)
                pathEffect = PathEffect.dashPathEffect(
                    intervals = floatArrayOf(10f, 10f), // 10px 그리고 10px 띔
                    phase = phase2
                ),
                alpha = 1.0f,
            ),
            areaStyle = AreaStyle(
                brush = SolidColor(colors[triple.first] ?: Color.Black),
                alpha = 0.2f,
            ),
            areaBaseline = AreaBaseline.ArbitraryLine(triple.second)
        )


    }

}
