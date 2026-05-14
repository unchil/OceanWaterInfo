package com.unchil.oceanwaterinfo


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeViewport
import com.unchil.oceanwaterinfo.viewmodel.KhoaObservationCurrentViewModel
import io.github.koalaplot.core.xygraph.Point
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLIFrameElement



@OptIn(ExperimentalComposeUiApi::class, ExperimentalWasmJsInterop::class)
fun main() {

    val mainHtmlElementId = "webmain"
    val waterInfoMapHtmlElementId = "waterInfoMap"

    ComposeViewport(viewportContainerId = mainHtmlElementId) {


        val coroutineScope = rememberCoroutineScope()
        val viewModel: KhoaObservationCurrentViewModel = remember {
            KhoaObservationCurrentViewModel(coroutineScope)
        }
        LaunchedEffect(key1 = viewModel){
            viewModel.onEvent(KhoaObservationCurrentViewModel.Event.Refresh)
        }
        val seaWaterInfo = viewModel._observationStateFlow.collectAsState()
        val markerData = remember { mutableStateOf<Triple<String, String, String>?>(null) }

        LaunchedEffect( seaWaterInfo.value){
            if (seaWaterInfo.value.isNotEmpty()) {
                markerData.value = transformToMarkerData(seaWaterInfo.value)
            }
        }


        LaunchedEffect(markerData.value){
            markerData.value?.let { (locs, lbs, cnts) ->
            val message = """
                {
                    "action": "ADD_Marker_Clusterer",
                    "target": { "locations": $locs, "labels": $lbs, "content": $cnts }
                }
                """.trimIndent()
                postIframeMessage("iframe_waterInfo", message)
            }
        }


        val clickPointOceanWaterInfoGeoChart = mutableStateOf(Point(126.934515, 37.385852))

        val onClickPointOceanWaterInfoGeoChart = { point:Point<Double, Double> ->
            clickPointOceanWaterInfoGeoChart.value = point
            val message = """
                {
                    "action": "FLY_TO",
                    "target": { "lat": ${point.y}, "lng": ${point.x} }
                }
                """.trimIndent()
            postIframeMessage("iframe_waterInfo", message)
        }


        CompositionLocalProvider(LocalPlatform provides getPlatform()) {

            MaterialTheme(
                typography = getTypography(),
                colorScheme = getColorScheme(false)
            ) {
                val density = LocalDensity.current

                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                        .background(color = MaterialTheme.colorScheme.surface),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

//                       OceanWaterInfo()

                    NuclearPlantStatePieChart_KHNP()
                    RadioActiveWastePlantStatStackedBarChart_KHNP()
                    KHNPRadioActiveWasteStackBarChart()
                    RadioRateBarChart()
                    WasteWaterTimeSeries_KHNP()
                    ThermalWasteWaterTimeSeries_KHNP()
                    OceanWaterInfo_MOF()
                    WaterTempTimeSeries_KHOA()
                    OceanWaterInfoTimeSeries()
                    OceanWaterInfoBoxPlotChart()
                    OceanWaterInfoBarChart()
                    OceanWaterInfoDataGrid()
                    WaterDegTimeSeries_KHOA()

                    Row(
                        modifier = Modifier.fillMaxWidth().height(600.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {

                        Box(
                            modifier = Modifier.fillMaxWidth(0.35f),
                            contentAlignment = Alignment.Center,

                            ){
                            WindPolarChart_KHOA()
                        }

                        Box(
                            modifier = Modifier.fillMaxWidth(0.35f),
                            contentAlignment = Alignment.Center,
                        ){
                            WaterInfoGeoChart_KHOA(onClickPointOceanWaterInfoGeoChart)
                        }

                        val paddingRight = 16
                        Box(
                            modifier = Modifier.fillMaxSize()
                                .onGloballyPositioned { coordinates ->
                                    syncHtmlElementPosition(coordinates, density, mainHtmlElementId, waterInfoMapHtmlElementId, paddingRight)
                                },
                            contentAlignment =Alignment.Center
                        ) {
                            // 여기는 비어있지만, 실제로는 iframe_waterInfo div가 이 위를 덮게 됩니다.
                        }

                    }


                }

            }

        }




        DisposableEffect(Unit) {
            onDispose {
                val htmlElement = document.getElementById(waterInfoMapHtmlElementId) as? HTMLElement
                htmlElement?.style?.display = "none"
            }
        }


    }
}