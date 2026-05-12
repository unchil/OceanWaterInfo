package com.unchil.oceanwaterinfo


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeViewport
import io.github.koalaplot.core.xygraph.Point

import kotlinx.browser.document
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLIFrameElement





@OptIn(ExperimentalComposeUiApi::class, ExperimentalWasmJsInterop::class)
fun main() {

    ComposeViewport(viewportContainerId = "webmain") {



        val clickPointOceanWaterInfoGeoChart = mutableStateOf(Point(126.934515, 37.385852))
        val onClickPointOceanWaterInfoGeoChart = { point:Point<Double, Double> ->
            clickPointOceanWaterInfoGeoChart.value = point
            val iframe = document.getElementById("map-waterInfo") as HTMLIFrameElement
            val message = "{action: 'FLY_TO', target: { lat: ${point.y}, lng: ${point.x} } }"
            println(message)


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
                                            val htmlElement = document.getElementById("iframe_waterInfo") as? HTMLElement

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

        // 화면을 벗어날 때 HTML 요소 숨기기
        DisposableEffect(Unit) {
            onDispose {
                val htmlElement = document.getElementById("iframe_waterInfo") as? HTMLElement
                htmlElement?.style?.display = "none"
            }
        }
    }
}