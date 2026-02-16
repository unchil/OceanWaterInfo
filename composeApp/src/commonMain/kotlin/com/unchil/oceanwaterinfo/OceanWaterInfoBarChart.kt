package com.unchil.oceanwaterinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.unchil.oceanwaterinfo.SEA_AREA.gru_nam
import io.github.koalaplot.core.style.LineStyle
import io.github.koalaplot.core.xygraph.AxisStyle
import io.github.koalaplot.core.xygraph.CategoryAxisModel
import io.github.koalaplot.core.xygraph.DoubleLinearAxisModel
import io.github.koalaplot.core.xygraph.FloatLinearAxisModel
import io.github.koalaplot.core.xygraph.GridStyle
import io.github.koalaplot.core.xygraph.Point
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OceanWaterInfoBarChart(){

    val coroutineScope = rememberCoroutineScope()

    val viewModel: NifsSeaWaterInfoCurrentViewModel = remember {
        NifsSeaWaterInfoCurrentViewModel(  coroutineScope  )
    }

    val uiState: MutableState<ChartUiState> = remember { mutableStateOf(ChartUiState.Loading) }

    LaunchedEffect(key1 = viewModel){
        while(true){
            delay(1 * 60 * 1000L).let{
                viewModel.onEvent(NifsSeaWaterInfoCurrentViewModel.Event.Refresh)
            }
        }
    }

    var selectedOption by remember { mutableStateOf(SEA_AREA.GRU_NAME.entries[0]) }

    val seaWaterInfo = viewModel._seaWaterInfo.collectAsState()
    val data: MutableState< List<Triple< String, Point<String, Float>, Map<String, Any>>>> = remember { mutableStateOf(emptyList() ) }

    val chartLayout = remember { mutableStateOf(LayoutData() )}
    val chartHeight = remember {400.dp}
    val chartTitle = remember { mutableStateOf("")}
    val chartXTitle = remember { "Observatory"}
    val chartYTitle = remember { "Water Temperature °C"}
    val chartCaption = remember {"from https://www.nifs.go.kr (National Institute of Fisheries Science)"}

    var isLegend by remember { mutableStateOf(true) }

    LaunchedEffect( isLegend){
        chartLayout.value = chartLayout.value.copy(
            legend = chartLayout.value.legend.copy(
                isUsable = isLegend,
            )
        )
    }


    LaunchedEffect(key1= seaWaterInfo.value, key2=selectedOption){
        val filteredList = seaWaterInfo.value.filter {
            it.gru_nam.equals(selectedOption.gru_nam()) &&  it.obs_lay == "1"
        }

        if(filteredList.isNotEmpty()){
            chartTitle.value = "${filteredList.first().obs_datetime} Surface Temperature"
            data.value = filteredList.toBarChartTripleList()
        }else{
            chartTitle.value ="Surface Temperature"
            data.value = emptyList()
        }
    }

    LaunchedEffect(data.value){

        uiState.value = when {

            data.value.isNotEmpty() -> {
                val allPoints = data.value.map { it.second  }

                val yMax = allPoints.maxOf { it.y }
                val yMin = allPoints.minOf { it.y }

                val yRange = 0f..(yMax + 1.0f)

                chartLayout.value = LayoutData(
                    type = ChartType.VerticalBar,
                    layout = TitleConfig(true, chartTitle.value),
                    legend = LegendConfig(isLegend, true, chartXTitle),
                    xAxis = AxisConfig(
                        chartXTitle,
                        model = CategoryAxisModel(data.value.map{ triple -> triple.first }),
                        style = AxisStyle(labelRotation = 45)
                    ),
                    yAxis = AxisConfig(
                        chartYTitle,
                        model = FloatLinearAxisModel(yRange)
                    ),
                    gridStyle = GridStyle(
                        horizontalMajorStyle = LineStyle(brush= SolidColor(Color.Transparent)),
                        horizontalMinorStyle = LineStyle(brush= SolidColor(Color.Transparent)),
                        verticalMajorStyle = LineStyle(brush= SolidColor(Color.Transparent)),
                        verticalMinorStyle = LineStyle(brush= SolidColor(Color.Transparent)),
                    ),
                    size = SizeConfig(height = chartHeight),
                    caption = CaptionConfig(true,  chartCaption  ),
                )

                ChartUiState.Success(
                    chartData = data.value,
                    entries = data.value.map{ triple -> triple.first },
                    chartLayout = chartLayout.value
                )
            }
            data.value.isEmpty()-> {
                chartLayout.value = LayoutData(
                    layout = TitleConfig(true, chartTitle.value),
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



    Column (modifier = paddingMod,
        horizontalAlignment = Alignment.CenterHorizontally,
        ) {

        Row {
            SEA_AREA.GRU_NAME.entries.forEach { entrie ->
                Row(
                    Modifier
                        .selectable(
                            selected = (entrie == selectedOption),
                            onClick = { selectedOption = entrie }
                        )
                        .padding(horizontal = 16.dp),
                    horizontalArrangement =  Arrangement.Center ,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (entrie == selectedOption),
                        onClick = { selectedOption = entrie }
                    )
                    Text(
                        text = entrie.name,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
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
                HorizontalDivider()
                Row(modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    ToggleButton(
                        checked = isLegend,
                        colors = ToggleButtonDefaults.toggleButtonColors(
                            checkedContainerColor  = Color.LightGray,
                            checkedContentColor  = Color.Black,
                        ),
                        onCheckedChange = { isLegend = it }
                    ){  Text(  text = "Legend"   )  }
                }

            }
        }



    }



}