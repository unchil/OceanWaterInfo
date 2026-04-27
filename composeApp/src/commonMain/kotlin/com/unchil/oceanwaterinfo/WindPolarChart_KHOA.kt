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
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
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
import com.unchil.oceanwaterinfo.viewmodel.KhoaObservationViewModel
import io.github.koalaplot.core.ChartLayout
import io.github.koalaplot.core.Symbol
import io.github.koalaplot.core.polar.AngularAxisModel
import io.github.koalaplot.core.polar.DefaultPolarPoint
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
import io.github.koalaplot.core.util.deg
import io.github.koalaplot.core.util.toDegrees
import io.github.koalaplot.core.xygraph.Point
import kotlinx.coroutines.delay
import kotlin.math.round
import kotlin.math.roundToInt

@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalKoalaPlotApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WindPolarChart(){

    val coroutineScope = rememberCoroutineScope()

    val viewModel: KhoaObservationViewModel = remember {
        KhoaObservationViewModel(  coroutineScope  )
    }

    val uiState: MutableState<ChartUiState> = remember { mutableStateOf(ChartUiState.Loading) }


    LaunchedEffect(key1 = viewModel){
        while(true){
            delay(1 * 60 * 1000L).let{
                viewModel.onEvent(KhoaObservationViewModel.Event.Refresh)
            }
        }
    }

    val windInfo = viewModel._observationStateFlow.collectAsState()

    val data: MutableState<List<Triple< String, Point<Double, Double>,List<PolarPoint<Float, AngularValue> >>> >  =  remember { mutableStateOf(emptyList() ) }


    val chartLayout = remember { mutableStateOf(LayoutData() )}
    val chartHeight = remember {500.dp}
    val chartTitle = remember { mutableStateOf("")}
    val maxCrSp = remember { mutableStateOf(0f)}

    val chartCaption = remember {"from https://www.data.go.kr/data/15155516/openapi.do (행정안전부 공공데이터포털)"}

    var isLegend by remember { mutableStateOf(true) }

    LaunchedEffect( isLegend){
        chartLayout.value = chartLayout.value.copy(
            legend = chartLayout.value.legend.copy(
                isUsable = isLegend,
            )
        )
    }

    LaunchedEffect(windInfo.value){

        val maxDate = windInfo.value.maxOfOrNull { it.obsrvnDt }


        chartTitle.value = (maxDate ?: "" ) + " 실시간 유향/유속 현황 (cm/s)"

        val filteredList = windInfo.value.filter{ it ->
            it.obsrvnDt.equals(maxDate)
        }

        maxCrSp.value =  filteredList.maxOfOrNull { it.crsp?.toFloat() ?: 0f} ?: 0f


        if(filteredList.isNotEmpty()){
            data.value = filteredList.map {
                Triple(
                    it.obsvtrNm,
                    Point(it.lat, it.lot),
                listOf(DefaultPolarPoint(it.crsp?.toFloat() ?: 0f, it.crdir?.toDouble()?.deg ?: 0.deg)   )
                )
            }
        }else{
            data.value = emptyList()
        }
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
                LayoutData(
                    type = ChartType.VerticalBar,
                    layout = TitleConfig(true, chartTitle.value),
                    legend = LegendConfig(isLegend, true, "관측소"),
                    size = SizeConfig(height = chartHeight),
                    caption = CaptionConfig(true, chartCaption),
                )
            }
            else -> {
                LayoutData(
                    layout = TitleConfig(true, chartTitle.value),
                    size = SizeConfig(height = chartHeight),
                    caption = CaptionConfig(true, chartCaption)
                )
            }
        }
    }


    Column (modifier = paddingMod.fillMaxWidth(0.5f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {


        when( val state = uiState.value) {
            is ChartUiState.EmptyChart -> {
                EmptyChart(chartLayout.value)
            }

            is ChartUiState.Error -> {
                Text(state.message)
            }

            ChartUiState.Loading -> {
                CircularProgressIndicator()
            }

            is ChartUiState.Success -> {

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
                            radialAxisModel = rememberFloatRadialAxisModel( List(5) { i -> round((maxCrSp.value / 3) * i  ) }
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
                            val data = (state.chartData as List<Triple< String, Point<Double, Double>, List<PolarPoint<Float, AngularValue> >>> )

                            val polarPointList = (state.chartData as List<Triple< String, Point<Double, Double>, List<PolarPoint<Float, AngularValue> >>> ).flatMap {
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
                                                            Text( text = data[index].first)
                                                            Text( text =  "latitude:${data[index].second.y}, longitude:${data[index].second.x}")
                                                            Text( text = "${data[index].third[0].r} 유속(cm/s)")
                                                            Text( text = "${data[index].third[0].theta.toDegrees()} deg")
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

    }








}