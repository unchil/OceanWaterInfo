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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unchil.oceanwaterinfo.viewmodel.KhnpWasteWaterViewModel
import io.github.koalaplot.core.xygraph.DoubleLinearAxisModel
import io.github.koalaplot.core.xygraph.FloatLinearAxisModel
import io.github.koalaplot.core.xygraph.Point
import kotlinx.coroutines.delay
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.minutes
import kotlinx.datetime.toLocalDateTime

@OptIn(FormatStringsInDatetimeFormats::class)
@Composable
fun WasteWater_PointLineChart_KHNP(){
    val coroutineScope = rememberCoroutineScope()

    val viewModel: KhnpWasteWaterViewModel = remember {
        KhnpWasteWaterViewModel(  coroutineScope  )
    }

    val uiState: MutableState<ChartUiState> = remember { mutableStateOf(ChartUiState.Loading) }

    LaunchedEffect(key1 = viewModel){
        while(true){
            delay(1 * 60 * 1000L).let{
                viewModel.onEvent(KhnpWasteWaterViewModel.Event.Refresh)
            }
        }
    }

    val wasterWaterInfo = viewModel._khnpWasteWaterStateFlow.collectAsState()
    val data: MutableState< ChartDataList> = remember { mutableStateOf(emptyList() ) }
    val chartLayout = remember { mutableStateOf(LayoutData() )}
    val chartHeight = remember {400.dp}
    val chartTitle = remember {"3-hour WasteWater Current"}
    val chartXTitle = remember { "DateTime"}
    val chartYTitle = remember { "Quality(PH)"}
    val chartCaption = remember {"from https://www.data.go.kr/data/15157700/openapi.do (행정안전부 공공데이터포털)"}

    var isTooltips by remember { mutableStateOf(true) }
    var isSymbol by remember { mutableStateOf(true) }
    var isLegend by remember { mutableStateOf(true) }




    LaunchedEffect(key1= wasterWaterInfo.value){

        val previousHour = kotlin.time.Clock.System.now()
            .minus(3, DateTimeUnit.HOUR)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .toInstant(TimeZone.UTC)

        val checkTime_Wastewater = 10.minutes

        val filteredList = wasterWaterInfo.value.filter { item ->
            val time = LocalDateTime.parse(item.time.replace(" ", "T")).toInstant(TimeZone.UTC)
            val tm01 = LocalDateTime.parse(item.tm001_time.replace(" ", "T")).toInstant(TimeZone.UTC)
            val tm02 = LocalDateTime.parse(item.tm002_time.replace(" ", "T")).toInstant(TimeZone.UTC)

            time >= previousHour &&
            (time - tm01).absoluteValue <= checkTime_Wastewater &&
            (time - tm02).absoluteValue <= checkTime_Wastewater
        }

        if(filteredList.isNotEmpty()){
            data.value = filteredList.toChartTripleList(
                nameSelector = { it.genName },
                timeSelector = { it.time },
                timePattern = "yyyy-MM-dd HH:mm",
                primaryValueSelector = { it.tm002.trim().toFloatOrNull() ?: 0f },   // PH
                secondaryValueSelector = { it.tm001.trim().toFloatOrNull() ?: 0f }, // 유량
                secondaryKey = "tm001"
            )
        }else{
           data.value = emptyList()
        }
    }

    LaunchedEffect(isTooltips, isSymbol){
        chartLayout.value = chartLayout.value.copy(
            tooltips = chartLayout.value.tooltips.copy(
                isTooltips = isTooltips,
                isSymbol = isSymbol
            )
        )
    }

    LaunchedEffect( isLegend){
        chartLayout.value = chartLayout.value.copy(
            legend = chartLayout.value.legend.copy(
                isUsable = isLegend,
            )
        )
    }

    LaunchedEffect(data.value){

        uiState.value = when {
            data.value.isNotEmpty() -> {
                ChartUiState.Success(
                    chartData = data.value,
                    entries = data.value.map{ triple -> triple.first },
                    chartLayout = chartLayout.value
                )
            }
            data.value.isEmpty()-> {
                ChartUiState.EmptyChart( chartLayout =  chartLayout.value)
            }
            else -> { ChartUiState.Loading }
        }

        chartLayout.value = when {
            data.value.isNotEmpty() -> {
                // 모든 포인트를 리스트 하나로 합칩니다.
                val allPoints = data.value.flatMap { it.second }
                // 한 번씩만 호출하여 결과 도출
                val xMax = allPoints.maxOf { it.x }
                val xMin = allPoints.minOf { it.x }
                val yMax = allPoints.maxOf { it.y }
                val yMin = allPoints.minOf { it.y }

                val xRange = xMin-300 * 1000..xMax+ 300*1000
                val yRange = yMin-0.1f..yMax+0.1f

                LayoutData(
                    type = ChartType.Point,
                    layout = TitleConfig(true, chartTitle),
                    legend = LegendConfig(isLegend, true, "Power Plant"),
                    xAxis = AxisConfig(
                        model = DoubleLinearAxisModel(xRange) ,
                    ),
                    yAxis = AxisConfig(
                        chartYTitle,
                        model = FloatLinearAxisModel(yRange)
                    ),
                    tooltips = TooltipConfig(isTooltips = isTooltips, isSymbol = isSymbol),
                    size = SizeConfig(height = chartHeight),
                    caption = CaptionConfig(true,chartCaption ),
                )

            }
            else -> {
                LayoutData(
                    layout = TitleConfig(true, chartTitle),
                    legend = LegendConfig(isLegend, true, chartXTitle),
                    xAxis = AxisConfig(chartXTitle),
                    yAxis = AxisConfig( chartYTitle),
                    size = SizeConfig(height = chartHeight),
                    caption = CaptionConfig(true,  chartCaption  )
                )
            }
        }


    }



    Column (modifier = paddingMod) {

        when( val state = uiState.value){
            is ChartUiState.EmptyChart -> {
                EmptyChart(chartLayout.value )
            }
            is ChartUiState.Error -> {
                Text(state.message)
            }
            ChartUiState.Loading -> {
                CircularProgressIndicator()
            }
            is ChartUiState.Success -> {


                ComposeXYPlot(
                    layout = chartLayout.value,
                    data = state.chartData,
                    entries = state.entries
                )

                val optionList = listOf("Tooltips", "Symbol", "Legend")

                val selectedOptions = remember {
                    mutableStateListOf<Int>().apply { addAll(optionList.indices) }
                }

                MultiChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    optionList.forEachIndexed { index, label ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count =optionList.size),
                            onCheckedChange = {
                                if (index in selectedOptions) selectedOptions.remove(index)
                                else selectedOptions.add(index)

                                when(index){
                                    0 -> isTooltips = it
                                    1 -> isSymbol = it
                                    2 -> isLegend = it
                                }
                            },
                            checked = index in selectedOptions
                        ) {
                            Text(label)
                        }
                    }
                }

            }

        }


    }



}