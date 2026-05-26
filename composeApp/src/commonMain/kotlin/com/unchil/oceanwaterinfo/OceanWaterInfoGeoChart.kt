package com.unchil.oceanwaterinfo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import io.github.koalaplot.core.xygraph.Point
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import oceanwaterinfo.composeapp.generated.resources.Res
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.MultiPolygon
import org.maplibre.spatialk.geojson.Polygon
import org.maplibre.spatialk.geojson.Position

@Composable
fun OceanWaterInfoGeoChart(onClickPoint:(Point<Double, Double>)->Unit = { point -> }  ){
    val coroutineScope = rememberCoroutineScope()

    val viewModel: NifsSeaWaterInfoCurrentViewModel = remember {
        NifsSeaWaterInfoCurrentViewModel(  coroutineScope  )
    }

    val onRefresh:()->Unit = {
        coroutineScope.launch {
            while(true){
                delay(5 * 60 * 1000L).let{
                    viewModel.onEvent(NifsSeaWaterInfoCurrentViewModel.Event.Refresh)
                }
            }
        }
    }

    val geoData = remember { mutableStateOf(emptyList<Point<Double,Double>>()) }
    var featureCollection by remember {
        mutableStateOf<FeatureCollection<Geometry, JsonObject>?>(null)
    }
    val sourthKrShape = mutableListOf<Position>()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val chartData: MutableState< ChartDataGeoPlot> = remember { mutableStateOf(Triple(emptyList(), emptyList(), Pair(emptyList(), {}))) }

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

    val seaWaterInfo = viewModel._seaWaterInfo.collectAsState()

    val data = remember { mutableStateOf(emptyList<ChartValuesGeo>() ) }

    LaunchedEffect(seaWaterInfo.value){
        data.value = seaWaterInfo.value.filter {
            it.obs_lay == "1"
        }.map {
            Triple(
                it.sta_nam_kor ,
                Point(it.lon, it.lat) ,
                Pair(it.obs_datetime,it.wtr_tmp )
            )
        }
    }

    LaunchedEffect(data.value,geoData.value ){
        if(data.value.isNotEmpty() && geoData.value.isNotEmpty()){
            chartData.value = Triple(
                data.value.map{ triple -> triple.first },
                data.value,
                Pair(geoData.value, onClickPoint)
            )
        }
    }

    ChartDataFlow(
        chartData = ChartData.XYPlotGeoPlot(chartData.value),
        title = "Surface Temperature",
        xTitle = "Longitude",
        yTitle = "Latitude",
        caption = "from https://www.nifs.go.kr (National Institute of Fisheries Science)",
        chartType = ChartType.Geo,
        yRangePadding = 0.0f,
        legendTitle = "Observatory",
        height = 600.dp,
        visibleBottomBar = false,
        onRefresh = onRefresh
    )




}
