package com.unchil.oceanwaterinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
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
import io.github.koalaplot.core.bar.DefaultBar
import io.github.koalaplot.core.bar.DefaultBarPosition
import io.github.koalaplot.core.bar.DefaultVerticalBarPlotEntry
import io.github.koalaplot.core.bar.VerticalBarPlot
import io.github.koalaplot.core.bar.VerticalBarPlotEntry
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.Point
import io.github.koalaplot.core.xygraph.XYGraphScope


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XYGraphScope<Double, Float>.VerticalBarChart(
    data: List<Triple< String, List<Point<Double, Float>>, Map<String, Any>>>,
    range: ClosedFloatingPointRange<Float>,
    onHoverEvent:((Int)->Unit)? = null
){

    val colors = getColors(data.map { triple -> triple.first })
    val toolTipWidth = remember { 120.dp }

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

    // val initHoverInteraction: MutableState<HoverInteraction.Enter?> = remember{ mutableStateOf(null) }

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
                val interactionSource =   interactionSources[index]
                val isHovered by interactionSource.collectIsHoveredAsState()
                //  val tooltipState = tooltipStates[index]
                /*
                if( index == lastIndex ) {

                    LaunchedEffect(Unit) {
                        yield()
                        if (initHoverInteraction.value == null) {
                            val enter = HoverInteraction.Enter()
                            initHoverInteraction.value = enter
                            interactionSource.emit(enter)
                        }

                     /*
                     * | 단계 | 설명 | 사용 사례 |
                     * | :--- | :--- | :--- |
                     * | Default | 가장 낮은 우선순위. | 일반적인 UI 업데이트, 프로그램에 의한 상태 변경. |
                     * | UserInput | 중간 우선순위. | 사용자가 직접 화면을 터치하거나 드래그하는 동작. |
                     * | PreventUserInput | 가장 높은 우선순위. | 사용자 입력을 무시하고 반드시 실행되어야 하는 강제 로직. |
                     * MutatePriority.PreventUserInput를 사용하여
                     * 다른 상호작용이 이 작업을 방해하지 못하도록 강제합니다.
                     */
                        tooltipState.show(mutatePriority = MutatePriority.PreventUserInput)
                    }

// 처음 로드시 강제로 발생한 HoverInteraction.Enter 를 1초 후에 삭제하고 로드된 툴팁을 dismiss 시킴
                    LaunchedEffect(tooltipState.isVisible){
                        if(tooltipState.isVisible && initHoverInteraction.value != null){
                            delay(1000)  // first tooltips display time
                            tooltipState.dismiss()
                            initHoverInteraction.value?.let {
                                interactionSources[lastIndex].emit(   HoverInteraction.Exit(it) )
                                initHoverInteraction.value = null
                            }
                        }
                    }

                }
                 */

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
                                //   .border(1.dp, color=Color.Black)
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


                                val sortedEntries = data.map { triple ->
                                   triple.first to (triple.second.getOrNull(index)?.y ?: 0f)
                               }.sortedByDescending { it.second }


                                // 4. 정렬된 리스트를 순회하며 툴팁을 그립니다.
                                sortedEntries.forEach {  (observatory, value) ->

                                    BoxPlotTooltips(
                                        "${observatory} : ${value}",
                                        textStyle,
                                        modifier.background( color = colors[observatory] as Color, shape = ShapeDefaults.Small),

                                        )
                                }


                            }

                        }

                    },
                    state = tooltipStates[index],
                ) {
                    DefaultBar(
                        brush = SolidColor( if (isHovered) Color.LightGray.copy(0.2f) else Color.Transparent),
                        modifier = modifier.hoverable(interactionSource = interactionSource)
                    )
                }

            },
            barWidth = barWidth,
            /*
                        startAnimationUseCase = StartAnimationUseCase(
                            executionType = StartAnimationUseCase.ExecutionType.Default,
                            chartAnimationSpecs = arrayOf(
                             //   spring( dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow )
                             //   tween( durationMillis = 1000,  delayMillis = 100 )
                                keyframes {
                                    durationMillis = 1000
                                    0f at 0 using LinearEasing
                                    0.8f at 500 using FastOutLinearInEasing
                                    1f at 1000
                                }
                            )
                        )

             */
        )
    }

}





@OptIn(ExperimentalMaterial3Api::class, ExperimentalKoalaPlotApi::class)
@Composable
fun XYGraphScope<String, Float>.VerticalBarChart(
    data: Any,
    usableTooltips: Boolean,
    barWidth: Float = 0.9f
){
    val data = (data as List<Triple< String, Point<String, Float>, Map<String, Any>>>)

    val colors = getColors(data.map { triple -> triple.first })


    val values = data.map { triple ->
        DefaultVerticalBarPlotEntry(  triple.second.x,
            DefaultBarPosition(0f, triple.second.y)
        )
    }

    VerticalBarPlot(
        values,
        bar = { index, _, _ ->

            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    TooltipAnchorPosition.Above
                ),
                tooltip = {
                    if (usableTooltips){
                        PlainTooltip {
                            Text("${values[index].x}\n${values[index].y.end }" )
                        }
                    }
                }                    ,
                state = rememberTooltipState(),
            ) {
                DefaultBar(
                    brush = SolidColor(colors[data[index].first] ?: Color.Black),
                    modifier = Modifier.fillMaxWidth(),
                )
            }


        },
        barWidth = barWidth
    )
}


