package com.unchil.oceanwaterinfo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unchil.oceanwaterinfo.chart.XYPlotTimeSeries
import io.github.koalaplot.core.xygraph.AxisStyle
import io.github.koalaplot.core.xygraph.CategoryAxisModel
import io.github.koalaplot.core.xygraph.DoubleLinearAxisModel
import io.github.koalaplot.core.xygraph.FloatLinearAxisModel
import io.github.koalaplot.core.xygraph.Point


sealed class ChartData {
    data class TimeSeries(val data: ChartDataList) : ChartData()
    data class XYPlotStringFloat(val data: ChartDataListStringFloat) : ChartData()
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
    onSuccessChartData:@Composable (ChartUiState.SuccessChartData) -> Unit,
) {

    Column(modifier = modifier) {
        topBar() // 탭 로우 등이 들어가는 자리
        when (uiState) {
            ChartUiState.Loading -> CircularProgressIndicator()
            is ChartUiState.Error -> Text(uiState.message)
            is ChartUiState.EmptyChart -> EmptyChart(uiState.chartLayout)
            is ChartUiState.SuccessChartData -> onSuccessChartData(uiState)
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
                    legend = LegendConfig(isLegend, true),
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

    }

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
            yRangePadding = yRangePadding,
            secondaryKey = secondaryKey,
            legendTitle = legendTitle,
            description = description
        )

        uiState.value = when(chartData) {
            is ChartData.TimeSeries -> {

                if(chartData.data.isNotEmpty()){
                    ChartUiState.SuccessChartData(
                        chartData = chartData,
                        entries = chartData.data.map { it.first },
                        chartLayout = chartLayout.value
                    )
                }else {
                    ChartUiState.EmptyChart(chartLayout = chartLayout.value)
                }
            }
            is ChartData.XYPlotStringFloat -> {

                if(chartData.data.isNotEmpty()){
                    ChartUiState.SuccessChartData(
                        chartData = chartData,
                        entries = chartData.data.map { it.first },
                        chartLayout = chartLayout.value
                    )
                }else {
                    ChartUiState.EmptyChart(chartLayout = chartLayout.value)
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

            when(state.chartData){
                is ChartData.TimeSeries -> {
                    XYPlotTimeSeries(
                        layout = state.chartLayout,
                        data = state.chartData.data,
                        entries = state.entries
                    )
                }
                is ChartData.XYPlotStringFloat -> {
                    XYPlotTimeSeries(
                        layout = state.chartLayout,
                        data = state.chartData.data,
                        entries = state.entries
                    )
                }
            }

        }
    )

}

