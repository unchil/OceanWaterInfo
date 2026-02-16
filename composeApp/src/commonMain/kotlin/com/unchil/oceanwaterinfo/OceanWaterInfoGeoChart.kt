package com.unchil.oceanwaterinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
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
import oceanwaterinfo.composeapp.generated.resources.Res
import io.github.koalaplot.core.style.LineStyle
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.DoubleLinearAxisModel
import io.github.koalaplot.core.xygraph.GridStyle
import io.github.koalaplot.core.xygraph.Point
import kotlinx.coroutines.delay
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position


@OptIn(ExperimentalKoalaPlotApi::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun OceanWaterInfoGeoChart(){


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

    val seaWaterInfo = viewModel._seaWaterInfo.collectAsState()
    val chartLayout = remember { mutableStateOf(LayoutData() )}
    val chartTitle = remember { mutableStateOf("Surface Temperature")}
    val legendTitle = remember { "Observatory"}
    val chartXTitle = remember { "Longitude"}
    val chartYTitle = remember { "Latitude"}
    val chartHeight = remember { 600.dp}
    val chartCaption = remember {"from https://www.nifs.go.kr (National Institute of Fisheries Science)"}

    var isLegend by remember { mutableStateOf(true) }

    LaunchedEffect( isLegend){
        chartLayout.value = chartLayout.value.copy(
            legend = chartLayout.value.legend.copy(
                isUsable = isLegend,
            )
        )
    }

    var featureCollection by remember {
        mutableStateOf<FeatureCollection<Geometry, JsonObject>?>(null)
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val sourthKrShape = mutableListOf<Position>()

    val data = remember { mutableStateOf(emptyList<Triple<String, Point<Double, Double>, Pair<String, Float>>>()) }
    val geoData = remember { mutableStateOf(emptyList<Point<Double,Double>>()) }

    LaunchedEffect(seaWaterInfo.value){
        data.value = seaWaterInfo.value.filter {
            it.obs_lay == "1"
        }.map {
            Triple(
                it.sta_nam_kor ,
                Point(it.lon, it.lat) ,
                Pair(it.obs_datetime,it.wtr_tmp.toFloat())
            )
        }
    }

    LaunchedEffect(Unit) {
        try {
            featureCollection = FeatureCollection.fromJson<Geometry, JsonObject>(
                Res.readBytes("files/southkorea.json").decodeToString()
            )
            featureCollection!!.features.forEach { feature ->
                if(feature.id?.intOrNull == 0){
                    when (feature.geometry) {
                        is MultiPolygon -> {
                            val multiPolygon = (feature.geometry as MultiPolygon).coordinates.flatten().flatten() as MutableList<Position>
                            sourthKrShape += multiPolygon
                        }
                        is Polygon -> {
                            val polygon = (feature.geometry as Polygon).coordinates.flatten()
                            sourthKrShape += polygon
                        }
                        else -> {}
                    }
                }
            }
            geoData.value = sourthKrShape.map {
                Point(it.longitude, it.latitude)
            }

        } catch (e: Exception) {
            errorMessage = e.message
        }
    }

    LaunchedEffect(data.value, geoData.value, errorMessage){

        uiState.value = when {
            geoData.value.isNotEmpty()  && data.value.isEmpty()-> {

                chartLayout.value = LayoutData(
                    type = ChartType.Geo,
                    layout = TitleConfig(true, chartTitle.value),
                    legend = LegendConfig(isLegend, true, legendTitle),
                    xAxis = AxisConfig(
                        chartXTitle,
                        model = DoubleLinearAxisModel(geoData.value.getRange().first)
                    ),
                    yAxis = AxisConfig(
                        chartYTitle,
                        model = DoubleLinearAxisModel(geoData.value.getRange().second)
                    ),
                    size = SizeConfig(height = chartHeight),
                    caption = CaptionConfig(true,  chartCaption ),
                )

                ChartUiState.EmptyChart(chartLayout = chartLayout.value, geoData = geoData.value)
            }
            geoData.value.isNotEmpty() && data.value.isNotEmpty() -> {
                chartLayout.value = LayoutData(
                    type = ChartType.Geo,
                    layout = TitleConfig(true,  "${data.value.first().third.first} ${chartTitle.value}" ),
                    legend = LegendConfig(isLegend, true, legendTitle),
                    xAxis = AxisConfig(
                        chartXTitle,
                        model = DoubleLinearAxisModel(geoData.value.getRange().first)
                    ),
                    yAxis = AxisConfig(
                        chartYTitle,
                        model = DoubleLinearAxisModel(geoData.value.getRange().second)
                    ),
                    gridStyle = GridStyle(
                        horizontalMajorStyle = LineStyle(brush= SolidColor(Color.Gray)),
                        horizontalMinorStyle = LineStyle(brush= SolidColor(Color.Transparent)),
                        verticalMajorStyle = LineStyle(brush= SolidColor(Color.Gray)),
                        verticalMinorStyle = LineStyle(brush= SolidColor(Color.Transparent)),
                    ),
                    size = SizeConfig(height = chartHeight),
                    caption = CaptionConfig(true,  chartCaption ),
                )
                ChartUiState.Success(
                    chartData = data.value,
                    entries = data.value.map{ triple -> triple.first },
                    chartLayout = chartLayout.value,
                    geoData = geoData.value
                )
            }
            errorMessage != null -> {
                ChartUiState.Error(errorMessage?: "")
            }
            else -> {
                ChartUiState.Loading
            }
        }
    }

    Column(modifier = paddingMod.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        when (val state = uiState.value) {
            is ChartUiState.Success -> {

                ComposeXYPlot(
                    layout = chartLayout.value,
                    data = Triple(state.entries, state.chartData, state.geoData),
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
            is ChartUiState.EmptyChart -> {
                GeoEmptyChart(
                    layout = chartLayout.value,
                    data = state.geoData ?: emptyList()
                )
            }
            is ChartUiState.Error -> {
                Text(state.message)
            }
            is ChartUiState.Loading -> {
                CircularProgressIndicator()
            }
        }
    }

}
