package com.unchil.oceanwaterinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unchil.oceanwaterinfo.ChartUiState.*
import com.unchil.oceanwaterinfo.chart.XYPlotChart
import io.github.koalaplot.core.ChartLayout
import io.github.koalaplot.core.Symbol
import io.github.koalaplot.core.polar.AngularAxisModel
import io.github.koalaplot.core.polar.PolarGraph
import io.github.koalaplot.core.polar.PolarGraphDefaults
import io.github.koalaplot.core.polar.PolarPlotSeries2
import io.github.koalaplot.core.polar.PolarPoint
import io.github.koalaplot.core.polar.rememberAngularValueAxisModel
import io.github.koalaplot.core.polar.rememberFloatRadialAxisModel
import io.github.koalaplot.core.style.AreaStyle
import io.github.koalaplot.core.style.LineStyle
import io.github.koalaplot.core.util.AngularValue
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.util.toDegrees
import io.github.koalaplot.core.xygraph.AxisStyle
import io.github.koalaplot.core.xygraph.CategoryAxisModel
import io.github.koalaplot.core.xygraph.DoubleLinearAxisModel
import io.github.koalaplot.core.xygraph.FloatLinearAxisModel
import io.github.koalaplot.core.xygraph.GridStyle
import io.github.koalaplot.core.xygraph.Point
import kotlin.math.round

typealias ChartDataList =            List<Triple<String, List<Point<Double, Float>>, Map<String, Any>>>
typealias ChartDataListStringFloat = List<Triple<String, Point<String, Float>, Map<String, Any>>>
typealias ChartDataBoxPlot =         Map<String, SeaWaterBoxPlotStat>
typealias ChartEntriesType =        List<String>
typealias ChartValuesGeo =          Triple<String, Point<Double, Double>, Pair<String, String>>
typealias GeoShapeDataType =        Pair<List<Point<Double,Double>>, (Point<Double,Double>)->Unit>
typealias ChartDataGeoPlot =        Triple< ChartEntriesType, List<ChartValuesGeo>, GeoShapeDataType >
typealias ChartDataPolar =          List<Triple< String, Point<Double, Double>, List<PolarPoint<Float, AngularValue> >>>


sealed class ChartData {
    data class TimeSeries(val data: ChartDataList) : ChartData()
    data class XYPlotStringFloat(val data: ChartDataListStringFloat) : ChartData()

    data class XYPlotBoxPlot(val data: ChartDataBoxPlot) : ChartData()

    data class XYPlotGeoPlot(val data: ChartDataGeoPlot): ChartData()

    data class PolarGraphPlot(val data:ChartDataPolar ): ChartData()
}

@Composable
fun ChartFeatureControls(
    isTooltips: Boolean? = null, onTooltipsChange: (Boolean) -> Unit = {},
    isSymbol: Boolean? = null, onSymbolChange: (Boolean) -> Unit = {},
    isLegend: Boolean? = null, onLegendChange: (Boolean) -> Unit = {}
) {
    val options = remember {
        mutableListOf<String>().apply {
            if (isTooltips != null) add("Tooltips")
            if (isSymbol != null) add("Symbol")
            if (isLegend != null) add("Legend")
        }
    }
    if (options.isEmpty()) return

    val selectedOptions = remember {
        mutableStateListOf<Int>().apply { addAll(options.indices) }
    }

    MultiChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                onCheckedChange = { checked ->
                    if (checked) selectedOptions.add(index) else selectedOptions.remove(index)
                    when (label) {
                        "Tooltips" -> onTooltipsChange(checked)
                        "Symbol" -> onSymbolChange(checked)
                        "Legend" -> onLegendChange(checked)
                    }
                },
                checked = index in selectedOptions
            ) {
                Text(label)
            }
        }
    }
}



@Composable
fun ChartScaffold(
    uiState: ChartUiState,
    modifier: Modifier = paddingMod,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    onSuccessChartData:@Composable (SuccessChartData) -> Unit,
) {

    Column(modifier = modifier) {
        topBar() // 탭 로우 등이 들어가는 자리
        when (uiState) {
            Loading -> CircularProgressIndicator()
            is Error -> Text(uiState.message)
            is EmptyChart -> EmptyChart(uiState.chartLayout)
            is SuccessChartData -> onSuccessChartData(uiState)
            else -> {}
        }
        bottomBar()
    }

}


/**
 * 차트 데이터(ChartDataList)를 분석하여 범위(Axis Range)와 레이아웃 설정을 생성하는 함수
 */
fun prepareChartLayout(
    chartType: ChartType,
    chartData: ChartData,
    title: String,
    xTitle: String,
    yTitle: String,
    caption: String,
    isTooltips: Boolean,
    isSymbol: Boolean,
    isLegend: Boolean,
    height: Dp = 400.dp,
    yRangePadding: Float ,
    secondaryKey: String? = null,
    legendTitle:String = "Entry",
    description:String? = null
): LayoutData {

    when(chartData) {

        is ChartData.TimeSeries -> {

            if (chartData.data.isEmpty()) {
                return LayoutData(
                    type = chartType,
                    layout = TitleConfig(true, title),
                    legend = LegendConfig(isLegend, true),
                    xAxis = AxisConfig(xTitle),
                    yAxis = AxisConfig(yTitle),
                    size = SizeConfig(height = height),
                    caption = CaptionConfig(true, caption)
                )
            }


            val mainPoints = chartData.data.flatMap { it.second }
            @Suppress("UNCHECKED_CAST")
            val secondaryPoints = secondaryKey?.let { key ->
                chartData.data.flatMap { it.third[key] as? List<Point<Double, Float>> ?: emptyList() }
            } ?: emptyList()

            val allPoints = mainPoints + secondaryPoints

            val xMax = allPoints.maxOf { it.x }
            val xMin = allPoints.minOf { it.x }
            val yMax = allPoints.maxOf { it.y }
            val yMin = allPoints.minOf { it.y }

            // X축(시간): 데이터 전후로 5분(300,000ms)의 여유를 둠
            val xRange = (xMin - 300 * 1000)..(xMax + 300 * 1000)
            // Y축: 데이터 상하로 yRangePadding만큼 여유를 둠
            val yRange = (yMin - yRangePadding)..(yMax + yRangePadding)

            // 3. 계산된 범위를 바탕으로 LayoutData 반환
            return LayoutData(
                type = chartType,
                layout = TitleConfig(true, title, description),
                legend = LegendConfig(isLegend, true, legendTitle), // 범례 제목
                xAxis = AxisConfig(
                    model = DoubleLinearAxisModel(xRange)
                ),
                yAxis = AxisConfig(
                    yTitle,
                    model = FloatLinearAxisModel(yRange)
                ),
                tooltips = TooltipConfig(isTooltips = isTooltips, isSymbol = isSymbol),
                size = SizeConfig(height = height),
                caption = CaptionConfig(true, caption)
            )

        }

        is ChartData.XYPlotStringFloat -> {

            if (chartData.data.isEmpty()) {
                return LayoutData(
                    type = chartType,
                    layout = TitleConfig(true, title),
                    legend = LegendConfig(isLegend, true, legendTitle),
                    xAxis = AxisConfig(xTitle),
                    yAxis = AxisConfig(yTitle),
                    size = SizeConfig(height = height),
                    caption = CaptionConfig(true, caption)
                )
            }

            val allPoints = chartData.data.map { it.second  }
            val yMax = allPoints.maxOf { it.y }
            val yRange = 0f..(yMax + yRangePadding)


            // 3. 계산된 범위를 바탕으로 LayoutData 반환
            return LayoutData(
                type = chartType,
                layout = TitleConfig(true, title, description),
                legend = LegendConfig(isLegend, true, legendTitle), // 범례 제목
                xAxis = AxisConfig(
                    xTitle,
                    model = CategoryAxisModel(chartData.data.map{ triple -> triple.first }),
                    style = AxisStyle(labelRotation = 45)
                ),
                yAxis = AxisConfig(
                    yTitle,
                    model = FloatLinearAxisModel(yRange)
                ),
                tooltips = TooltipConfig(isTooltips = isTooltips, isSymbol = isSymbol),
                size = SizeConfig(height = height),
                caption = CaptionConfig(true, caption)
            )
        }

        is ChartData.XYPlotBoxPlot -> {
            if (chartData.data.isEmpty()) {
                return LayoutData(
                    type = chartType,
                    layout = TitleConfig(true, title),
                    legend = LegendConfig(isLegend, true, legendTitle),
                    xAxis = AxisConfig(xTitle),
                    yAxis = AxisConfig(yTitle),
                    size = SizeConfig(height = height),
                    caption = CaptionConfig(true, caption)
                )
            }

            val yMax = chartData.data.values.maxOf { entry -> entry.max }
            val yRange = 0f..(yMax * yRangePadding)

            return LayoutData(
                type = chartType,
                layout = TitleConfig(true, title, description),
                legend = LegendConfig(isLegend, true, legendTitle), // 범례 제목
                xAxis = AxisConfig(
                    title = xTitle,
                    model = CategoryAxisModel(chartData.data.keys.toList()),
                    style = AxisStyle(labelRotation = 45)
                ),
                yAxis = AxisConfig(
                    yTitle,
                    model = FloatLinearAxisModel(yRange)
                ),
                tooltips = TooltipConfig(isTooltips = isTooltips, isSymbol = isSymbol),
                size = SizeConfig(height = height),
                caption = CaptionConfig(true, caption)
            )


        }

        is ChartData.XYPlotGeoPlot -> {
            if (chartData.data.first.isEmpty()) {
                return LayoutData(
                    type = chartType,
                    layout = TitleConfig(true, title),
                    legend = LegendConfig(isLegend, true, legendTitle),
                    xAxis = AxisConfig(xTitle, model = DoubleLinearAxisModel(chartData.data.third.first.getRange().first)),
                    yAxis = AxisConfig(yTitle, model = DoubleLinearAxisModel(chartData.data.third.first.getRange().second)),
                    size = SizeConfig(height = height),
                    caption = CaptionConfig(true, caption)
                )
            }

            return LayoutData(
                type = chartType,
                layout = TitleConfig(true, title = "${chartData.data.second.first().third.first} ${title}"),
                legend = LegendConfig(isLegend, true, legendTitle),
                xAxis = AxisConfig(xTitle, model = DoubleLinearAxisModel(chartData.data.third.first.getRange().first)),
                yAxis = AxisConfig(yTitle, model = DoubleLinearAxisModel(chartData.data.third.first.getRange().second)),
                gridStyle = GridStyle(
                    horizontalMajorStyle = LineStyle(brush= SolidColor(Color.Gray)),
                    horizontalMinorStyle = LineStyle(brush= SolidColor(Color.Transparent)),
                    verticalMajorStyle = LineStyle(brush= SolidColor(Color.Gray)),
                    verticalMinorStyle = LineStyle(brush= SolidColor(Color.Transparent)),
                ),
                size = SizeConfig(height = height),
                caption = CaptionConfig(true, caption)
            )




        }

        else -> TODO()
    }

}

@OptIn(ExperimentalKoalaPlotApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChartDataFlow(
    chartData: ChartData,
    title: String,
    caption: String,
    legendTitle: String = "Entry",
    description:String? = null,
    height:Dp = 400.dp,
    maxCrSp: Float,
    onRefresh: () -> Unit,
    topBar: @Composable (() -> Unit) = {},
){
    // 1. 주기적 데이터 갱신 로직 (1분 단위)
    onRefresh()

    val chartLayout = remember { mutableStateOf(LayoutData()) }
    val uiState = remember { mutableStateOf<ChartUiState>(ChartUiState.Loading) }

    LaunchedEffect(chartData){
        chartLayout.value =  when(chartData) {
            is ChartData.PolarGraphPlot ->{
                if(chartData.data.isEmpty()){
                     LayoutData(
                        layout = TitleConfig(true, title),
                        size = SizeConfig(height = height),
                        caption = CaptionConfig(true, caption)
                    )
                }else {
                    LayoutData(
                        layout = TitleConfig(true, title),
                        legend = LegendConfig(true, true, legendTitle),
                        size = SizeConfig(height = height),
                        caption = CaptionConfig(true, caption),
                    )
                }
            }
            else -> {
                LayoutData(
                    layout = TitleConfig(true, title),
                    size = SizeConfig(height = height),
                    caption = CaptionConfig(true, caption)
                )
            }
        }

        val entries = when(chartData){
            is ChartData.PolarGraphPlot ->  chartData.data.map{ triple -> triple.first }
            else -> {emptyList()}
        }

        uiState.value = if(entries.isNotEmpty()) {
            SuccessChartData(
                chartData = chartData,
                entries = entries,
                chartLayout = chartLayout.value
            )
        } else {
            EmptyChart(chartLayout = chartLayout.value)
        }

    }

    ChartScaffold(
        uiState = uiState.value,
        topBar = topBar,
        bottomBar = { },
        onSuccessChartData = { state ->

            Column (modifier = paddingMod.fillMaxWidth(0.5f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val colors = getColors(state.entries)

                Box(  contentAlignment =  Alignment.Center ) {

                    ChartLayout(
                        modifier = paddingMod
                            .sizeIn(minHeight = chartLayout.value.size.minHeight, maxHeight = chartLayout.value.size.maxHeight)
                            .background(color = MaterialTheme.colorScheme.surface),
                        title = {
                            if (chartLayout.value.layout.isTitle) {
                                ChartTitle(chartLayout.value.layout.title, modifier = paddingMod)
                            }
                        },
                        legend = {
                            if(chartLayout.value.legend.isUsable ) {
                                Legend(chartLayout.value, state.entries, colors)
                            }
                        },
                        legendLocation = chartLayout.value.legend.location
                    ) {

                        val angularAxisGridLineStyle =
                            LineStyle(SolidColor(Color.LightGray), strokeWidth = 1.dp)

                        PolarGraph(
                            radialAxisModel = rememberFloatRadialAxisModel(
                                List(5) { i -> round((maxCrSp / 3) * i  ) }
                            ),
                            angularAxisModel = rememberAngularValueAxisModel(
                                angleDirection = AngularAxisModel.AngleDirection.CLOCKWISE ,
                                angleZero = AngularAxisModel.AngleZero.TWELVE_OCLOCK
                            ),
                            radialAxisLabels = {
                                Text("${it}" )
                            },
                            angularAxisLabels = {
                                Text("${it.toDegrees().value}\u00B0")
                            },
                            polarGraphProperties = PolarGraphDefaults.polarGraphPropertyDefaults()
                                .copy(
                                    angularAxisGridLineStyle = angularAxisGridLineStyle,
                                    radialAxisGridLineStyle = angularAxisGridLineStyle,
                                    background = AreaStyle(
                                        SolidColor(Color.Yellow),
                                        alpha = 0.1f,
                                    ),
                                ),
                            ) {

                                when(state.chartData){
                                    is ChartData.PolarGraphPlot -> {
                                        val polarPointList = state.chartData.data.flatMap {
                                            listOf(it.third)
                                        }
                                        polarPointList.forEachIndexed { index, seriesData ->
                                            PolarPlotSeries2(
                                                seriesData,
                                                symbols = {
                                                    TooltipBox(
                                                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                                            TooltipAnchorPosition.Above
                                                        ),
                                                        tooltip = {

                                                            PlainTooltip {
                                                                Column() {
                                                                    Text( text = state.chartData.data[index].first)
                                                                    Text( text =  "latitude:${state.chartData.data[index].second.y}, longitude:${state.chartData.data[index].second.x}")
                                                                    Text( text = "${state.chartData.data[index].third[0].r} 유속(cm/s)")
                                                                    Text( text = "${state.chartData.data[index].third[0].theta.toDegrees()} deg")
                                                                }

                                                            }

                                                        },
                                                        state = rememberTooltipState(),
                                                    ) {

                                                        Symbol(
                                                            shape = CircleShape,
                                                            fillBrush = SolidColor(
                                                                colors[state.entries[index]] ?: Color.LightGray
                                                            )
                                                        )

                                                    }
                                                },
                                            )
                                        }
                                    }

                                    else-> {}
                                }


                        }


                        if (chartLayout.value.caption.isCaption) {
                            Box( modifier = Modifier.fillMaxSize(),
                                contentAlignment = chartLayout.value.caption.location
                            ) {
                                CaptionText(chartLayout.value.caption.title, modifier = paddingMod)
                            }
                        } //-- Caption



                    }

                }
            }

        }
    )

}

@Composable
fun ChartDataFlow(
    chartData: ChartData,
    title: String,
    xTitle: String,
    yTitle: String,
    caption: String,
    chartType: ChartType,
    yRangePadding: Float = 1.0f,
    secondaryKey: String? = null,
    legendTitle: String = "Entry",
    description:String? = null,
    height:Dp = 400.dp,
    onRefresh: () -> Unit,
    topBar: @Composable (() -> Unit) = {},
) {
    // 1. 주기적 데이터 갱신 로직 (1분 단위)
    onRefresh()

    // 2. 내부 상태 관ChartPeriodicRefresh리 (레이아웃 및 UI 옵션)
    var isTooltips by remember { mutableStateOf(true) }
    var isSymbol by remember { mutableStateOf(true) }
    var isLegend by remember { mutableStateOf(true) }

    val chartLayout = remember { mutableStateOf(LayoutData()) }
    val uiState = remember { mutableStateOf<ChartUiState>(ChartUiState.Loading) }

    // 3. 데이터 변경에 따른 레이아웃 및 UI 상태 업데이트 흐름
    LaunchedEffect(chartData, isTooltips, isSymbol, isLegend) {

        chartLayout.value = prepareChartLayout(
            chartType = chartType,
            chartData = chartData ,
            title = title,
            xTitle = xTitle,
            yTitle = yTitle,
            caption = caption,
            isTooltips = isTooltips,
            isSymbol = isSymbol,
            isLegend = isLegend,
            height = height,
            yRangePadding = yRangePadding,
            secondaryKey = secondaryKey,
            legendTitle = legendTitle,
            description = description
        )

        val entries = when(chartData){
            is ChartData.TimeSeries -> chartData.data.map { it.first }
            is ChartData.XYPlotBoxPlot -> chartData.data.keys.toList()
            is ChartData.XYPlotStringFloat -> chartData.data.map { it.first }
            is ChartData.XYPlotGeoPlot -> chartData.data.first
            is ChartData.PolarGraphPlot -> TODO()
        }

        uiState.value = if(entries.isNotEmpty()) {
            SuccessChartData(
                chartData = chartData,
                entries = entries,
                chartLayout = chartLayout.value
            )
        } else {
            when(chartData){
                is ChartData.XYPlotGeoPlot -> {
                    EmptyChart(chartLayout = chartLayout.value, chartData.data.third)
                }
                else -> {
                    EmptyChart(chartLayout = chartLayout.value)
                }
            }
        }

    }



    // 4. 공통 UI 렌더링 흐름 (스캐폴드 활용)
    ChartScaffold(
        uiState = uiState.value,
        topBar = topBar,
        bottomBar = {
            ChartFeatureControls(
                isTooltips = isTooltips, onTooltipsChange = { isTooltips = it },
                isSymbol = isSymbol, onSymbolChange = { isSymbol = it },
                isLegend = isLegend, onLegendChange = { isLegend = it }
            )
        },
        onSuccessChartData = { state ->

            XYPlotChart(
                layout = state.chartLayout,
                chartData = state.chartData,
                entries = state.entries
            )


        }
    )

}

