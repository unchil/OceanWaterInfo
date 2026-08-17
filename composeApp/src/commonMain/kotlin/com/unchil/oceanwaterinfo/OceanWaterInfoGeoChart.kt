package com.unchil.oceanwaterinfo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import com.unchil.oceanwaterinfo.viewmodel.ObservatoryViewModel
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
fun OceanWaterInfoGeoChart(
    onClickPoint:(Point<Double, Double>)->Unit = { point -> }  ,
    sendAddMarkerClusterer:((iframeId:String,  tripleData :Triple<String, String, String>  )-> Unit)? = null,
    isReload: Int = 0
){

    val initCenterPoint = remember{ Point(126.934515, 37.385852) }
    val coroutineScope = rememberCoroutineScope()

    val viewModel: NifsSeaWaterInfoCurrentViewModel = remember {
        NifsSeaWaterInfoCurrentViewModel(    )
    }



    val onReload:()->Unit = {
        coroutineScope.launch {
            viewModel.onEvent(NifsSeaWaterInfoCurrentViewModel.Event.Refresh)
        }
    }

    LaunchedEffect(isReload){
        if(isReload > 0 ) onReload()
    }

    val viewModelObservatory: ObservatoryViewModel = remember {
        ObservatoryViewModel(    )
    }
    LaunchedEffect(key1 = viewModelObservatory){
        viewModelObservatory.onEvent(ObservatoryViewModel.Event.Refresh)
    }



    LaunchedEffect(viewModel){
        while(true){
            viewModel.onEvent(NifsSeaWaterInfoCurrentViewModel.Event.Refresh)
            delay(5 * 60 * 1000L)
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

    val isLoading by viewModel.isLoading.collectAsState()


    LaunchedEffect(isLoading) {
        onClickPoint(initCenterPoint)
    }



    val observatorys = viewModelObservatory._observatoryStateFlow.collectAsState()

    val data = remember { mutableStateOf(emptyList<ChartValuesGeo>() ) }

    LaunchedEffect(seaWaterInfo.value, observatorys.value, geoData.value){

        if ( observatorys.value.isNotEmpty() &&  seaWaterInfo.value.isNotEmpty()) {

            val filteredData = seaWaterInfo.value.filter {
                it.obs_lay == "1"
            }
            if(filteredData.isNotEmpty()) {

                data.value = filteredData.map {
                    Triple(
                        it.sta_nam_kor ,
                        Point(it.lon, it.lat) ,
                        Pair(it.obs_datetime,it.wtr_tmp )
                    )
                }

                if(geoData.value.isNotEmpty()){
                    chartData.value = Triple(
                        data.value.map{ triple -> triple.first },
                        data.value,
                        Pair(geoData.value, onClickPoint)
                    )
                }

                val filteredObservatories = observatorys.value.filter { obs ->
                    filteredData.any { info -> info.sta_cde == obs.sta_cde }
                }

                val transData = transformToMarkerDataFromOceanWater( data.value, filteredObservatories)
                sendAddMarkerClusterer?.invoke(IFRAME_OCEAN_WATER_INFO, transData)

            }

        }
    }



    Box(modifier=Modifier.fillMaxSize(), contentAlignment = Alignment.Center,) {

    // [Reload, Tooltips, Symbol, Legend]
    val bottomBarOpt = listOf(true, false, false, false)

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
        visibleBottomBar = true,
        onReload = onReload,
        bottomBarOpt = bottomBarOpt
    )

        AnimatedVisibility(isLoading){
            CircularProgressIndicator(
                color = Color.DarkGray,
            )
        }

        AnimatedVisibility(seaWaterInfo.value.isEmpty() && !isLoading ){
            NotFoundData()
        }  
        AnimatedVisibility(seaWaterInfo.value.isEmpty() && isLoading ) {
            DataLoading()
        }

    } //Box



}
