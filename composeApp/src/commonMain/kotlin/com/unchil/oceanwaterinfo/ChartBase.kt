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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unchil.oceanwaterinfo.ChartUiState.*
import com.unchil.oceanwaterinfo.chart.PolarPlotChart
import com.unchil.oceanwaterinfo.chart.XYPlotChart
import io.github.koalaplot.core.legend.LegendLocation
import io.github.koalaplot.core.polar.PolarPoint
import io.github.koalaplot.core.style.LineStyle
import io.github.koalaplot.core.util.AngularValue
import io.github.koalaplot.core.xygraph.AxisStyle
import io.github.koalaplot.core.xygraph.CategoryAxisModel
import io.github.koalaplot.core.xygraph.DoubleLinearAxisModel
import io.github.koalaplot.core.xygraph.FloatLinearAxisModel
import io.github.koalaplot.core.xygraph.GridStyle
import io.github.koalaplot.core.xygraph.IntLinearAxisModel
import io.github.koalaplot.core.xygraph.LongLinearAxisModel
import io.github.koalaplot.core.xygraph.Point

typealias ChartDataList =            List<Triple<String, List<Point<Double, Float>>, Map<String, Any>>>
typealias ChartDataListStringFloat = List<Triple<String, Point<String, Float>, Map<String, Any>>>
typealias ChartDataStringInt =  Triple< List<String>, List<List<Int>>, Map<String, Any>>
typealias ChartDataBoxPlot =         Map<String, SeaWaterBoxPlotStat>
typealias ChartEntriesType =        List<String>
typealias ChartValuesGeo =          Triple<String, Point<Double, Double>, Pair<String, String>>
typealias GeoShapeDataType =        Pair<List<Point<Double,Double>>, (Point<Double,Double>)->Unit>
typealias ChartDataGeoPlot =        Triple< ChartEntriesType, List<ChartValuesGeo>, GeoShapeDataType >
typealias ChartDataPolar =          List<Triple< String, Point<Double, Double>, List<PolarPoint<Float, AngularValue> >>>


sealed class ChartData {
    data class TimeSeries(val data: ChartDataList) : ChartData()
    data class XYPlotStringFloat(val data: ChartDataListStringFloat) : ChartData()


    data class XYPlotStringInt(val data: ChartDataStringInt) : ChartData()

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
    onSuccess:@Composable (Success) -> Unit,
) {

    Column(modifier = modifier) {
        topBar() // 탭 로우 등이 들어가는 자리
        when (uiState) {
            Loading -> CircularProgressIndicator()
            is Error -> Text(uiState.message)
            is EmptyChart -> EmptyChart(uiState.chartLayout)
            is Success -> onSuccess(uiState)

        }
        bottomBar()
    }

}

fun prepareChartLayout(
    chartType: ChartType,
    chartData: ChartData,
    title: String,
    xTitle: String,
    yTitle: String,
    legendTitle:String = "Entry",
    legendColor:LegendColor= LegendColor(),
    caption: String = "",
    description:String? = null,
    isTooltips: Boolean,
    isSymbol: Boolean,
    isLegend: Boolean,
    height: Dp = 400.dp,
    maxCrSp: Float = 0f,
    yRangePadding: Float ,
    selectedOption: WATER_QUALITY.QualityType? = null,
    secondaryKey: String? = null,
): LayoutData {

    return when(chartData) {

        is ChartData.TimeSeries -> {

            val mainPoints = chartData.data.flatMap { it.second }
            @Suppress("UNCHECKED_CAST")
            val secondaryPoints = secondaryKey?.let { key ->
                chartData.data.flatMap { it.third[key] as? List<Point<Double, Float>> ?: emptyList() }
            } ?: emptyList()

            val allPoints = mainPoints + secondaryPoints

            val xMax = allPoints.maxOf { it.x }
            val xMin = allPoints.minOf { it.x }
            val yMax = allPoints.maxOf { it.y }
            val yMin = allPoints.filter { it.y >= 1.0f }.minOfOrNull { it.y } ?: 0f
            // Y축: 데이터 상하로 yRangePadding만큼 여유를 둠
            //val yRange = (yMin - yRangePadding)..(yMax + yRangePadding)

            val yRange =  if(selectedOption == null) {
                (yMin - yRangePadding)..(yMax + yRangePadding)
            } else {

                val max = when (selectedOption) {
                    WATER_QUALITY.QualityType.rtmWqTu -> yMax.coerceAtMost(20f)
                    WATER_QUALITY.QualityType.rtmWqChpla -> yMax.coerceAtMost(10f)
                    else -> yMax
                }
                (yMin - yRangePadding)..(max + yRangePadding)
            }

            // X축(시간): 데이터 전후로 5분(300,000ms)의 여유를 둠
            val xRange = (xMin - 300 * 1000)..(xMax + 300 * 1000)

            // 3. 계산된 범위를 바탕으로 LayoutData 반환
            LayoutData(
                type = chartType,
                layout = TitleConfig(true, title,  description),
                legend = LegendConfig(isLegend, true, legendTitle, legendColor =legendColor), // 범례 제목
                xAxis = AxisConfig( model = DoubleLinearAxisModel(xRange) ),
                yAxis = AxisConfig( yTitle, model = FloatLinearAxisModel(yRange)),
                tooltips = TooltipConfig(isTooltips = isTooltips, isSymbol = isSymbol),
                size = SizeConfig(height = height),
                caption = CaptionConfig(true, caption)
            )

        }
        is ChartData.XYPlotStringFloat -> {
            val allPoints = chartData.data.map { it.second  }
            val yMax = allPoints.maxOf { it.y }
            val yRange = 0f..(yMax + yRangePadding)

            // 3. 계산된 범위를 바탕으로 LayoutData 반환
             LayoutData(
                type = chartType,
                layout = TitleConfig(true, title, description),
                legend = LegendConfig(isLegend, true, legendTitle, legendColor =legendColor), // 범례 제목
                xAxis = AxisConfig( xTitle,
                    model = CategoryAxisModel(chartData.data.map{ triple -> triple.first }),
                    style = AxisStyle(labelRotation = 45)
                ),
                yAxis = AxisConfig( yTitle, model = FloatLinearAxisModel(yRange) ),
                tooltips = TooltipConfig(isTooltips = isTooltips, isSymbol = isSymbol),
                size = SizeConfig(height = height),
                caption = CaptionConfig(true, caption)
            )
        }
        is ChartData.XYPlotBoxPlot -> {
            // 1. 모든 데이터(min, max, outliers)를 하나의 리스트로 합쳐서 전체 범위를 계산합니다.
            val allYValues = chartData.data.values.flatMap { entry ->
                listOf(entry.min, entry.max) + entry.outliers
            }
            val yMax = allYValues.maxOrNull() ?: 30f
            val yMin = allYValues.minOrNull() ?: 0f

            val yRange = (yMin - yRangePadding)..(yMax + yRangePadding)

            LayoutData(
                type = chartType,
                layout = TitleConfig(true, title, description),
                legend = LegendConfig(isLegend, true, legendTitle, legendColor =legendColor), // 범례 제목
                xAxis = AxisConfig(title = xTitle,
                    model = CategoryAxisModel(chartData.data.keys.toList()),
                    style = AxisStyle(labelRotation = 45)
                ),
                yAxis = AxisConfig( yTitle, model = FloatLinearAxisModel(yRange) ),
                tooltips = TooltipConfig(isTooltips = isTooltips, isSymbol = isSymbol),
                size = SizeConfig(height = height),
                caption = CaptionConfig(true, caption)
            )
        }
        is ChartData.XYPlotGeoPlot -> {
            LayoutData(
                type = chartType,
                layout = TitleConfig(true, title = "${chartData.data.second.first().third.first} ${title}"),
                legend = LegendConfig(isLegend, false, legendTitle, location= LegendLocation.BOTTOM, legendColor =legendColor),
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
        is ChartData.PolarGraphPlot -> {
            LayoutData(
                type = chartType,
                layout = TitleConfig(true, title),
                legend = LegendConfig(true, false, legendTitle, location= LegendLocation.BOTTOM, legendColor =legendColor),
                size = SizeConfig(height = height),
                caption = CaptionConfig(true, caption),
                maxCrSp = maxCrSp
            )

        }


        is ChartData.XYPlotStringInt -> {

            // 1. 각 연도별 데이터의 합계 중 최댓값 구하기
            // data.first: entryList(연도들), data.second: 각 카테고리별 데이터 리스트
            val entries = chartData.data.first
            val valuesByCategories = chartData.data.second

            // 연도별 인덱스를 순회하며 모든 카테고리의 값을 합산한 뒤 그 중 최댓값을 찾습니다.
            val yMax = if (entries.isNotEmpty()) {
                entries.indices.maxOf { yearIndex ->
                    valuesByCategories.sumOf { categoryData ->
                        categoryData[yearIndex] // 해당 연도의 특정 카테고리 값
                    }
                }
            } else {
                1200L // 데이터가 없을 경우 기본값
            }

            // 2. 여유 공간(Padding) 추가 (예: 최대값의 10% 정도 상단 여백)
            val bufferedYMax = yMax.toInt()

            val yRange = 0..bufferedYMax

            // 3. 계산된 범위를 바탕으로 LayoutData 반환
            LayoutData(
                type = chartType,
                layout = TitleConfig(true, title, description),
                legend = LegendConfig(isLegend, true, legendTitle, legendColor =legendColor), // 범례 제목
                xAxis = AxisConfig( xTitle,
                    model = CategoryAxisModel(chartData.data.first),
                    style = AxisStyle(labelRotation = 0)
                ),
                yAxis = AxisConfig( yTitle, model = IntLinearAxisModel(yRange)),

                tooltips = TooltipConfig(isTooltips = isTooltips, isSymbol = isSymbol),
                size = SizeConfig(height = height),
                caption = CaptionConfig(true, caption)
            )
        }
    }

}






@Composable
fun ChartDataFlow(
    chartType: ChartType,
    chartData: ChartData,
    title: String,
    xTitle: String,
    yTitle: String,
    legendTitle: String = "Entry",
    legendColor:LegendColor=LegendColor(),
    caption: String = "",
    description:String? = null,
    height:Dp = 400.dp,
    maxCrSp: Float = 0f,
    yRangePadding: Float = 1.0f,
    selectedOption: WATER_QUALITY.QualityType? = null,
    secondaryKey: String? = null,
    visibleBottomBar: Boolean = true,
    onRefresh: (() -> Unit)? = null,
    topBar: @Composable (() -> Unit) = {}
){


    // 2. 내부 상태 관ChartPeriodicRefresh리 (레이아웃 및 UI 옵션)
    var isTooltips by remember { mutableStateOf(true) }
    var isSymbol by remember { mutableStateOf(true) }
    var isLegend by remember { mutableStateOf(true) }


    val uiState = remember { mutableStateOf<ChartUiState>(ChartUiState.Loading) }

    // 3. 데이터 변경에 따른 레이아웃 및 UI 상태 업데이트 흐름
    LaunchedEffect(chartData, isTooltips, isSymbol, isLegend, yTitle) {


        val isEmpty = when(chartData){
            is ChartData.TimeSeries -> chartData.data.isEmpty()
            is ChartData.XYPlotBoxPlot -> chartData.data.keys.isEmpty()
            is ChartData.XYPlotStringFloat -> chartData.data.isEmpty()
            is ChartData.XYPlotGeoPlot -> chartData.data.first.isEmpty()
            is ChartData.PolarGraphPlot -> chartData.data.isEmpty()
            is ChartData.XYPlotStringInt -> chartData.data.first.isEmpty()
        }

        val chartLayout = if(isEmpty){

            when(chartData) {
                is ChartData.PolarGraphPlot -> {
                    LayoutData(
                        type = chartType,
                        layout = TitleConfig(true, title),
                        size = SizeConfig(height = height),
                        caption = CaptionConfig(true, caption)
                    )
                }
                is ChartData.XYPlotGeoPlot -> {
                    LayoutData(
                        type = chartType,
                        layout = TitleConfig(true, title),
                        legend = LegendConfig(isLegend, true, legendTitle),
                        xAxis = AxisConfig(xTitle, model = DoubleLinearAxisModel(chartData.data.third.first.getRange().first)),
                        yAxis = AxisConfig(yTitle, model = DoubleLinearAxisModel(chartData.data.third.first.getRange().second)),
                        size = SizeConfig(height = height),
                        caption = CaptionConfig(true, caption)
                    )
                }
                else -> {
                    LayoutData(
                        type = chartType,
                        layout = TitleConfig(true, title, description),
                        legend = LegendConfig(isLegend, true, legendColor =legendColor),
                        xAxis = AxisConfig(xTitle),
                        yAxis = AxisConfig(yTitle),
                        size = SizeConfig(height = height),
                        caption = CaptionConfig(true, caption)
                    )
                }
            }
        }else {
            prepareChartLayout(
                chartType = chartType,
                chartData = chartData ,
                title = title,
                xTitle = xTitle,
                yTitle = yTitle,
                legendTitle = legendTitle,
                legendColor = legendColor,
                caption = caption,
                description = description,
                isTooltips = isTooltips,
                isSymbol = isSymbol,
                isLegend = isLegend,
                height = height,
                maxCrSp = maxCrSp,
                yRangePadding = yRangePadding,
                selectedOption = selectedOption,
                secondaryKey = secondaryKey,
            )
        }




        val entries = when(chartData){
            is ChartData.TimeSeries -> chartData.data.map { it.first }
            is ChartData.XYPlotBoxPlot -> chartData.data.keys.toList()
            is ChartData.XYPlotStringFloat -> chartData.data.map { it.first }
            is ChartData.XYPlotGeoPlot -> chartData.data.first
            is ChartData.PolarGraphPlot -> chartData.data.map{ triple -> triple.first }
            is ChartData.XYPlotStringInt -> {
                if(isEmpty) {
                    emptyList()
                }else {
                    chartData.data.third["yAxisEntries"] as List<String>
                }
            }
        }

        uiState.value = if(entries.isNotEmpty()) {
            Success(
                chartData = chartData,
                entries = entries,
                chartLayout = chartLayout
            )
        } else {
            when(chartData){
                is ChartData.XYPlotGeoPlot -> {
                    EmptyChart(chartLayout = chartLayout, chartData.data.third)
                }
                else -> {
                    EmptyChart(chartLayout = chartLayout)
                }
            }

        }
    }


    ChartScaffold(
        uiState = uiState.value,
        topBar = topBar,
        bottomBar = {
            if(visibleBottomBar){
                // onRefresh 를 사용할수 있도록 수정.
                ChartFeatureControls(
                    isTooltips = isTooltips, onTooltipsChange = { isTooltips = it },
                    isSymbol = isSymbol, onSymbolChange = { isSymbol = it },
                    isLegend = isLegend, onLegendChange = { isLegend = it }
                )
            }
        },
        onSuccess = { state ->

            when(chartData){
                is ChartData.PolarGraphPlot -> {
                    PolarPlotChart(
                        layout = state.chartLayout,
                        chartData = state.chartData,
                        entries = state.entries
                    )
                }
                else -> {
                    XYPlotChart(
                        layout = state.chartLayout,
                        chartData = state.chartData,
                        entries = state.entries
                    )
                }
            }

        }
    )




}
