package com.unchil.oceanwaterinfo


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
fun main() {

    ComposeViewport(viewportContainerId = "webmain") {

        CompositionLocalProvider(LocalPlatform provides getPlatform()) {

            MaterialTheme(
                typography = getTypography(),
                colorScheme = getColorScheme(false)
            ) {


                    Column(modifier = Modifier.fillMaxSize()
                            .background(color = MaterialTheme.colorScheme.surface)
                    ) {

                        Box(modifier = Modifier.fillMaxHeight()) {
                            Column(
                                modifier = Modifier.verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ){

                                WasteWaterTimeSeries_KHNP()
                                ThermalWasteWaterTimeSeries_KHNP()
                                WaterDegTimeSeries_KHOA()
                                WaterTempTimeSeries_KHOA()
                                OceanWaterInfoTimeSeries()

                                OceanWaterInfoBoxPlotChart()
                                OceanWaterInfoBarChart()
                                OceanWaterInfoDataGrid()

                            }

                        }
                    }





            }

        }
    }
}