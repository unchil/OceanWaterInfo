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
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLIFrameElement


@OptIn(ExperimentalComposeUiApi::class, ExperimentalWasmJsInterop::class)
fun main() {

    ComposeViewport(viewportContainerId = "webmain") {


        val coroutineScope = rememberCoroutineScope()
        val viewModel: KhoaObservationCurrentViewModel = remember {
            KhoaObservationCurrentViewModel(coroutineScope)
        }
        LaunchedEffect(key1 = viewModel){
            viewModel.onEvent(KhoaObservationCurrentViewModel.Event.Refresh)
        }
        val seaWaterInfo = viewModel._observationStateFlow.collectAsState()
        val locations = remember{ mutableStateOf( "" )}
        val labels = remember{ mutableStateOf("" )}
        val content = remember{ mutableStateOf("" )}

        LaunchedEffect( seaWaterInfo.value){

            if( seaWaterInfo.value.size > 0 ) {

                val data = seaWaterInfo.value.map {
                    Triple(
                        it.obsvtrNm,
                        Point(it.lot, it.lat),
                        Pair(
                            it.obsrvnDt,
                            Triple(
                                it.wtem ?: "0" ,
                                it.crdir ?: "0",
                                it.crsp ?: "0"
                            )
                        )
                    )
                }

                locations.value = data.map { triple ->
                    triple.second
                }.joinToString(
                    separator = ",",
                    prefix = "[",
                    postfix = "]"
                ) { point ->
                    "{ \"lat\": ${point.y}, \"lng\": ${point.x} }"
                }

                labels.value = data.map { triple ->
                    triple.first
                }.joinToString(
                    separator = ",",
                    prefix = "[",
                    postfix = "]"
                ) { sta_nam_kor ->
                    "\"${sta_nam_kor}\""
                }

                content.value = data.map { triple ->
                    triple
                }.joinToString(
                    separator = ",",
                    prefix = "[",
                    postfix = "]"
                ) { triple ->
                    // 2. buildString을 사용하여 문자열 조립 (가독성 및 안전성)
                    val desc = buildString {
                        append("\"DateTime :${triple.third.first}<br>")
                        append("Temperature: ${triple.third.second.first } °C<br>")
                        append("Direction  : ${triple.third.second.second} \u00B0<br>")
                        append("Speed      : ${triple.third.second.third} (cm/sec)<br>")
                        append("\"")
                    }
                    desc

                }
            }
        }


        LaunchedEffect(locations.value, labels.value, content.value){
            if(locations.value.isNotEmpty() && labels.value.isNotEmpty() ){
                val iframe = document.getElementById("iframe_waterInfo") as? HTMLIFrameElement
                val message = """
{
    "action": "ADD_Marker_Clusterer",
    "target": {
        "locations": ${locations.value},
        "labels": ${labels.value},
        "content": ${content.value}
    }
}
""".trimIndent()

                val jsString = message.toJsString()
                println("Sent to iframe jsString: $jsString")
                iframe?.contentWindow?.postMessage(jsString, "*")


            }
        }


        val clickPointOceanWaterInfoGeoChart = mutableStateOf(Point(126.934515, 37.385852))

        val onClickPointOceanWaterInfoGeoChart = { point:Point<Double, Double> ->
            clickPointOceanWaterInfoGeoChart.value = point
            // 1. DOM에서 iframe 요소 찾기
            val iframe = document.getElementById("iframe_waterInfo") as? HTMLIFrameElement

            // 2. 메시지 구성 (JSON 문자열 또는 객체)
            val message = """
        {
            "action": "FLY_TO",
            "target": {
                "lat": ${point.y},
                "lng": ${point.x}
            }
        }
    """.trimIndent()

            // 3. iframe 내부로 전송
            // targetOrigin 에 보안을 위해 실제 서비스 시에는 iframe의 도메인을 입력하는 것이 좋습니다 (예: "https://your-map-site.com")
            iframe?.contentWindow?.postMessage(message.toJsString(), "*")
            println("Sent to iframe: $message")
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


                                Box(
                                    modifier = Modifier.fillMaxSize()
                                        .onGloballyPositioned { coordinates ->
                                            // Compose 영역의 절대 좌표와 크기를 계산
                                            val windowPos = coordinates.localToWindow(androidx.compose.ui.geometry.Offset.Zero)
                                            val width = coordinates.size.width
                                            val height = coordinates.size.height

                                            // 브라우저 DOM 요소를 찾아 위치 동기화
                                            val htmlElement = document.getElementById("waterInfoMap") as? HTMLElement

                                            htmlElement?.let {
                                                it.style.display = "block"
                                                it.style.left = "${windowPos.x / density.density}px"
                                                it.style.top = "${ (windowPos.y  / density.density) + 100 }px"
                                                it.style.width = "${width  / density.density}px"
                                                it.style.height = "${height / density.density}px"
                                            }


                                        }
                                ) {
                                    // 여기는 비어있지만, 실제로는 iframe_waterInfo div가 이 위를 덮게 됩니다.
                                }



                            }


                    }

            }

        }




        DisposableEffect(Unit) {
            onDispose {
                val htmlElement = document.getElementById("waterInfoMap") as? HTMLElement
                htmlElement?.style?.display = "none"
            }
        }


    }
}