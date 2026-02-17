package com.unchil.oceanwaterinfo


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButtonShapes
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.unchil.oceanwaterinfo.SEA_AREA.gru_nam
import io.github.koalaplot.core.xygraph.DoubleLinearAxisModel
import io.github.koalaplot.core.xygraph.FloatLinearAxisModel
import io.github.koalaplot.core.xygraph.Point
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OceanWaterInfoLineChart(){

    val coroutineScope = rememberCoroutineScope()

    val viewModel: NifsSeaWaterInfoViewModel = remember {
        NifsSeaWaterInfoViewModel( coroutineScope )
    }

    val uiState: MutableState<ChartUiState> = remember { mutableStateOf(ChartUiState.Loading) }


    LaunchedEffect(key1 =viewModel){
        while(true){
            delay(1 * 60 * 1000L).let{

                viewModel.onEvent(NifsSeaWaterInfoViewModel.Event.Refresh)

            }
        }
    }


    var selectedOption by remember { mutableStateOf(SEA_AREA.GRU_NAME.entries[0]) }

    val seaWaterInfo = viewModel._seaWaterInfo.collectAsState()
    val data: MutableState< List<Triple< String, List<Point<Double, Float>>, Map<String, Any>>>> = remember { mutableStateOf(emptyList() ) }
    val chartLayout = remember { mutableStateOf(LayoutData() )}
    val chartHeight = remember {400.dp}
    val chartTitle = remember {"24-hour Surface Sea Temperature"}
    val chartXTitle = remember { "DateTime"}
    val chartYTitle = remember { "Water Temperature °C"}
    val chartCaption = remember {"from https://www.nifs.go.kr (National Institute of Fisheries Science)"}

    var isTooltips by remember { mutableStateOf(true) }
    var isSymbol by remember { mutableStateOf(true) }
    var isLegend by remember { mutableStateOf(true) }


    LaunchedEffect(key1= seaWaterInfo.value, key2=selectedOption){
        val filteredList = seaWaterInfo.value.filter {
            it.gru_nam.equals(selectedOption.gru_nam()) &&  it.obs_lay == "1"
        }

        if(filteredList.isNotEmpty()){
            data.value = filteredList.toLineTripleList()
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
                val allPoints = data.value.flatMap { it.second }
                // 한 번씩만 호출하여 결과 도출
                val xMax = allPoints.maxOf { it.x }
                val xMin = allPoints.minOf { it.x }
                val yMax = allPoints.maxOf { it.y }
                val yMin = allPoints.minOf { it.y }

                val xRange = xMin-1800 * 1000..xMax+ 1800*1000
                val yRange = yMin-1.0f..yMax+1.0f

                chartLayout.value = LayoutData(
                    type = ChartType.Line,
                    layout = TitleConfig(true, chartTitle),
                    legend = LegendConfig(isLegend, true, "Observatory"),
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

        var selectedTabIndex by remember { mutableIntStateOf(0) }

        SecondaryTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface, // 배경색 설정
            contentColor = MaterialTheme.colorScheme.primary,   // 선택된 탭의 콘텐츠 색상
        ) {
            SEA_AREA.GRU_NAME.entries.forEachIndexed { index, entrie ->

                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index
                        selectedOption = entrie},
                    text = {
                        Text(
                            text = entrie.name,
                            style = MaterialTheme.typography.titleSmall // 보조 탭에 맞는 스타일
                        )
                    }
                )
            }
        }

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

