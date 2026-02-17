package com.unchil.oceanwaterinfo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unchil.oceanwaterinfo.WATER_QUALITY.desc
import com.unchil.oceanwaterinfo.WATER_QUALITY.name
import com.unchil.oceanwaterinfo.WATER_QUALITY.unit
import io.github.koalaplot.core.xygraph.AxisStyle
import io.github.koalaplot.core.xygraph.DoubleLinearAxisModel
import io.github.koalaplot.core.xygraph.FloatLinearAxisModel
import io.github.koalaplot.core.xygraph.Point
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OceanWaterInfoLineChart_MOF(){

    val coroutineScope = rememberCoroutineScope()

    val viewModel: MofSeaWaterInfoViewModel = remember {
        MofSeaWaterInfoViewModel( coroutineScope )
    }

    val uiState: MutableState<ChartUiState> = remember { mutableStateOf(ChartUiState.Loading) }

    LaunchedEffect(viewModel){
        while(true){
            delay(1 * 60 * 1000L).let{
                viewModel.onEvent(MofSeaWaterInfoViewModel.Event.Refresh)
            }
        }
    }

    var selectedOption by remember { mutableStateOf(WATER_QUALITY.QualityType.entries[0]) }

    val seaWaterInfo = viewModel._seaWaterInfo.collectAsState()
    val data: MutableState< List<Triple< String, List<Point<Double, Float>>, Map<String, Any>>>> = remember { mutableStateOf(emptyList() ) }

    val chartLayout = remember { mutableStateOf(LayoutData() )}
    val maxTurbidity = remember { 500f}
    val minElectricalConductivity = remember { 20f}
    val minDissolvedOxygen = remember { 7f }
    val minSalinity = remember { 15f }
    val minHydrogenIonConcentration = remember { 6f }
    val chartHeight = remember {500.dp}
    val chartTitle = remember {"24-hour Ocean Water Information"}
    val chartXTitle = remember { "DateTime"}
    val chartCaption = remember {"from https://www.mof.go.kr (Ministry of Oceans and Fisheries)"}

    var isTooltips by remember { mutableStateOf(true) }
    var isSymbol by remember { mutableStateOf(true) }
    var isLegend by remember { mutableStateOf(true) }

    LaunchedEffect(key1= seaWaterInfo.value, key2=selectedOption){
        data.value = seaWaterInfo.value.toMofLineTripleList(selectedOption)
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
                val legendTitle = "Observatory"
                // 모든 포인트를 리스트 하나로 합칩니다.
                val allPoints = data.value.flatMap { it.second }
                // 한 번씩만 호출하여 결과 도출
                val xMax = allPoints.maxOf { it.x }
                val xMin = allPoints.minOf { it.x }
                val yMax = allPoints.maxOf { it.y }
                val yMin = allPoints.minOf { it.y }

                val min = when (selectedOption) {
                    WATER_QUALITY.QualityType.rtmWqCndctv -> yMin.coerceAtLeast(minElectricalConductivity)
                    WATER_QUALITY.QualityType.ph -> yMin.coerceAtLeast(minHydrogenIonConcentration)
                    WATER_QUALITY.QualityType.rtmWqDoxn -> yMin.coerceAtLeast(minDissolvedOxygen)
                    WATER_QUALITY.QualityType.rtmWqSlnty -> yMin.coerceAtLeast(minSalinity)
                    else -> yMin
                }

                val max = when (selectedOption) {
                    WATER_QUALITY.QualityType.rtmWqTu -> yMax.coerceAtMost(maxTurbidity)
                    else -> yMax
                }

                val xRange = xMin-300 * 1000..xMax + 300*1000
                val yRange = min-1.0f..max+1.0f

                chartLayout.value = LayoutData(
                    type = ChartType.Line,
                    layout = TitleConfig(true, "${chartTitle} (${selectedOption.name()})", description = selectedOption.desc()),
                    legend = LegendConfig(isLegend, true, legendTitle),
                    xAxis = AxisConfig(
                        model = DoubleLinearAxisModel(xRange) ,
                        style = AxisStyle(labelRotation = 0),
                    ),
                    yAxis = AxisConfig(
                        selectedOption.unit(),
                        model = FloatLinearAxisModel(yRange)
                    ),
                    size = SizeConfig(chartHeight),
                    caption = CaptionConfig(true,chartCaption  ),
                    tooltips = TooltipConfig(isTooltips, isSymbol)
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
                    legend = LegendConfig(false, true, chartXTitle),
                    xAxis = AxisConfig(chartXTitle),
                    yAxis = AxisConfig( selectedOption.unit()),
                    size = SizeConfig(height = chartHeight),
                    caption = CaptionConfig(true,  chartCaption  )
                )
                ChartUiState.EmptyChart(chartLayout = chartLayout.value)
            }
            else -> {ChartUiState.Loading}
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


                var selectedTabIndex by remember { mutableIntStateOf(0) }

                SecondaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface, // 배경색 설정
                    contentColor = MaterialTheme.colorScheme.primary,   // 선택된 탭의 콘텐츠 색상
                ) {
                    WATER_QUALITY.QualityType.entries.forEachIndexed { index, entrie ->

                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index
                                selectedOption = entrie},
                            text = {
                                Text(
                                    text = entrie.name(),
                                    style = MaterialTheme.typography.titleSmall // 보조 탭에 맞는 스타일
                                )
                            }
                        )
                    }
                }


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
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = optionList.size),
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

