package com.unchil.oceanwaterinfo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import com.unchil.oceanwaterinfo.viewmodel.KhoaObservationCurrentViewModel
import com.unchil.oceanwaterinfo.viewmodel.KhoaObservationViewModel
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
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


@OptIn(ExperimentalKoalaPlotApi::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun WaterInfoGeoChart_KHOA(
    onClickPoint:(Point<Double,Double>)->Unit = { }  ,
    sendAddMarkerClusterer:((iframeId:String,  tripleData :Triple<String, String, String> )-> Unit)? = null,
    isReload: Int = 0
){

    val initCenterPoint = remember{ Point(126.934515, 37.385852) }

    val coroutineScope = rememberCoroutineScope()
    val viewModel: KhoaObservationCurrentViewModel = remember {
        KhoaObservationCurrentViewModel()
    }


    val onReload:()->Unit = {

        coroutineScope.launch {
            viewModel.onEvent(KhoaObservationCurrentViewModel.Event.Refresh)
        }
    }

    LaunchedEffect(isReload){
        if(isReload > 0 ) onReload()
    }

    LaunchedEffect(viewModel){
        while(true){

            viewModel.onEvent(KhoaObservationCurrentViewModel.Event.Refresh)
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


    val seaWaterInfo = viewModel._observationStateFlow.collectAsState()


    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(isLoading){
        onClickPoint(initCenterPoint)
    }

    val data = remember { mutableStateOf(emptyList<ChartValuesGeo>() ) }

    LaunchedEffect(seaWaterInfo.value){

        if (seaWaterInfo.value.isNotEmpty()) {
            val transData = transformToMarkerDataFromKhoaObservation(seaWaterInfo.value)
            sendAddMarkerClusterer?.invoke(IFRAME_WATER_INFO, transData)

            data.value = seaWaterInfo.value.map {
                Triple(
                    it.obsvtrNm ,
                    Point(it.lot, it.lat) ,
                    Pair(it.obsrvnDt,it.wtem ?: "")
                )
            }
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

    Box(modifier=Modifier.fillMaxSize(), contentAlignment = Alignment.Center,) {

    // [Reload, Tooltips, Symbol, Legend]
        val bottomBarOpt = listOf(true, false, false, false)

        ChartDataFlow(
            chartData = ChartData.XYPlotGeoPlot(chartData.value),
            title = "Sea Water Temperature",
            xTitle = "Longitude",
            yTitle = "Latitude",
            caption = "from https://www.data.go.kr/data/15155516/openapi.do (행정안전부 공공데이터포털)",
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


