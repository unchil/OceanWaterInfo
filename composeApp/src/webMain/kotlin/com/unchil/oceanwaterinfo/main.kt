package com.unchil.oceanwaterinfo


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeViewport
import com.unchil.oceanwaterinfo.viewmodel.KhoaObservationCurrentViewModel


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

        LaunchedEffect( seaWaterInfo.value){
            if (seaWaterInfo.value.isNotEmpty()) {
                sendAddMarkerClusterer(transformToMarkerData(seaWaterInfo.value))
            }
        }

        CompositionLocalProvider(LocalPlatform provides getPlatform()) {
            val density = LocalDensity.current

            MaterialTheme(
                typography = getTypography(),
                colorScheme = getColorScheme(false)
            ) {
                Column(modifier = Modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState())
                        .background(color = MaterialTheme.colorScheme.surface),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    NuclearPlantStatePieChart_KHNP()
                    RadioActiveWastePlantStatStackedBarChart_KHNP()
                    KHNPRadioActiveWasteStackBarChart()

                    Row(
                        modifier = Modifier.fillMaxWidth().height(600.dp)
                            .border(BorderStroke(1.dp, Color.LightGray)).padding(10.dp)
                        ,
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {

                        Box(
                            modifier = Modifier.fillMaxWidth(0.3f),
                            contentAlignment = Alignment.Center,

                            ){
                            WindPolarChart_KHOA()
                        }

                        Box(
                            modifier = Modifier.fillMaxWidth(0.3f),
                            contentAlignment = Alignment.Center,
                        ){
                            WaterInfoGeoChart_KHOA(onClickPointOceanWaterInfoGeoChart)
                        }


                        Box(
                            modifier = Modifier.fillMaxSize()
                                .onGloballyPositioned { coordinates ->
                                    syncHtmlElementPosition(coordinates, density, mainHtmlElementId, waterInfoMapHtmlElementId)
                                },
                            contentAlignment =Alignment.Center
                        ) {
                            // 여기는 비어있지만, 실제로는 iframe_waterInfo div가 이 위를 덮게 됩니다.
                        }

                    }

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

                }

            }

        } //CompositionLocalProvider


        DisposableEffect(Unit) {
            onDispose {
                disposeHtmlElements(listOf(waterInfoMapHtmlElementId))
            }
        }

    }// ComposeViewport
}