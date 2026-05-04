package com.unchil.oceanwaterinfo

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.unchil.oceanwaterinfo.viewmodel.KhoaObservationCurrentViewModel
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
fun WaterInfoGeoChart_KHOA(onClickPoint:(Point<Double,Double>)->Unit = { point -> }  ){
    val coroutineScope = rememberCoroutineScope()
    val viewModel: KhoaObservationCurrentViewModel = remember {
        KhoaObservationCurrentViewModel(coroutineScope)
    }

    val onRefresh:()->Unit = {
        coroutineScope.launch {
            while(true){
                delay(1 * 60 * 1000L).let{
                    viewModel.onEvent(KhoaObservationCurrentViewModel.Event.Refresh)
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

    val data = remember { mutableStateOf(emptyList<ChartValuesGeo>() ) }

    LaunchedEffect(seaWaterInfo.value){
        data.value = seaWaterInfo.value.map {
            Triple(
                it.obsvtrNm ,
                Point(it.lot, it.lat) ,
                Pair(it.obsrvnDt,it.wtem ?: "")
            )
        }
    }

    if(data.value.isNotEmpty() && geoData.value.isNotEmpty()){

        val chartData = ChartData.XYPlotGeoPlot(
            Triple(
                    data.value.map{ triple -> triple.first },
                    data.value,
                    Pair(geoData.value, onClickPoint)
                )
            )

        ChartDataFlow(
            chartData = chartData,
            title = "Sea Water Temperature",
            xTitle = "Longitude",
            yTitle = "Latitude",
            caption = "from https://www.data.go.kr/data/15155516/openapi.do (행정안전부 공공데이터포털)",
            chartType = ChartType.Geo,
            yRangePadding = 0.0f,
            legendTitle = "Observatory",
            height = 600.dp,
            onRefresh = onRefresh
        )
    }



}


