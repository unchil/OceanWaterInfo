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
import com.unchil.oceanwaterinfo.viewmodel.KhnpThermalWasteWaterViewModel
import com.unchil.oceanwaterinfo.viewmodel.KhnpWasteWaterViewModel
import io.github.koalaplot.core.xygraph.DoubleLinearAxisModel
import io.github.koalaplot.core.xygraph.FloatLinearAxisModel
import io.github.koalaplot.core.xygraph.Point
import kotlinx.coroutines.delay
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.minutes

@Composable
fun ThermalWasteWater_AreaLineChart_KHNP(){

    val coroutineScope = rememberCoroutineScope()

    val viewModel: KhnpThermalWasteWaterViewModel = remember {
        KhnpThermalWasteWaterViewModel(coroutineScope)
    }

    val uiState: MutableState<ChartUiState> = remember { mutableStateOf(ChartUiState.Loading) }

    LaunchedEffect(key1 = viewModel){
        while(true){
            delay(1 * 60 * 1000L).let{
                viewModel.onEvent(KhnpThermalWasteWaterViewModel.Event.Refresh)
            }
        }
    }

    val thermalWasterWaterInfo = viewModel._khnpThermalWasteWaterStateFlow.collectAsState()
    val data: MutableState< List<Triple< String, List<Point<Double, Float>>, Map<String, Any>>>> = remember { mutableStateOf(emptyList() ) }
    val chartLayout = remember { mutableStateOf(LayoutData() )}
    val chartHeight = remember {400.dp}
    val chartTitle = remember {"24-hour ThermalWasteWater Current"}
    val chartXTitle = remember { "DateTime"}
    val chartYTitle = remember { "Water Temperature(°C)"}
    val chartCaption = remember {"from https://www.data.go.kr/data/15157696/openapi.do (행정안전부 공공데이터포털)"}

    var isTooltips by remember { mutableStateOf(true) }
    var isSymbol by remember { mutableStateOf(true) }
    var isLegend by remember { mutableStateOf(true) }

    LaunchedEffect(key1= thermalWasterWaterInfo.value){

        val previousHour = kotlin.time.Clock.System.now()
            .minus(24, DateTimeUnit.HOUR)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .toInstant(TimeZone.UTC)

        val checkTime_Wastewater = 30.minutes

        val filteredList = thermalWasterWaterInfo.value.filter { item ->
            val time = LocalDateTime.parse(item.time.replace(" ", "T")).toInstant(TimeZone.UTC)
            val rm01 = LocalDateTime.parse(item.rm001_time.replace(" ", "T")).toInstant(TimeZone.UTC)
            val rm05 = LocalDateTime.parse(item.rm005_time.replace(" ", "T")).toInstant(TimeZone.UTC)


  //          time >= previousHour &&
            (time - rm01).absoluteValue <= checkTime_Wastewater &&
            (time - rm05).absoluteValue <= checkTime_Wastewater
        }

        if(filteredList.isNotEmpty()){
            data.value = filteredList.toLineTripleListThermalWasteWater()
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
                // 모든 포인트를 리스트 하나로 합칩니다.
                val inputPoints = data.value.flatMap { it.second }
                val outputPoints = data.value.flatMap { it.third["rm005"] as List<Point<Double, Float>> }
                val allPoints = inputPoints + outputPoints
                val xMax = allPoints.maxOf { it.x }
                val xMin = allPoints.minOf { it.x }
                val yMax = allPoints.maxOf { it.y }
                val yMin = allPoints.minOf { it.y }

                val xRange = xMin-300 * 1000..xMax+ 300*1000
                val yRange = yMin-1.0f..yMax+1.0f

                chartLayout.value = LayoutData(
                    type = ChartType.Area,
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

                ChartUiState.Success(
                    chartData = data.value,
                    entries = data.value.map{ triple -> triple.first },
                    chartLayout = chartLayout.value
                )
            }
            data.value.isEmpty()-> {
                chartLayout.value = LayoutData(
                    layout = TitleConfig(true, chartTitle),
                    legend = LegendConfig(isLegend, true, chartXTitle),
                    xAxis = AxisConfig(chartXTitle),
                    yAxis = AxisConfig( chartYTitle),
                    size = SizeConfig(height = chartHeight),
                    caption = CaptionConfig(true,  chartCaption  )
                )

                ChartUiState.EmptyChart( chartLayout =  chartLayout.value)
            }
            else -> { ChartUiState.Loading }
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