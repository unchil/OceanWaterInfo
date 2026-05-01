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
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeViewport
import io.github.koalaplot.core.xygraph.Point

@OptIn(ExperimentalComposeUiApi::class)
fun main() {

    ComposeViewport(viewportContainerId = "webmain") {

        val onClickPoint = { point:Point<Double, Double> ->

        }

        CompositionLocalProvider(LocalPlatform provides getPlatform()) {

            MaterialTheme(
                typography = getTypography(),
                colorScheme = getColorScheme(false)
            ) {


                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                            .background(color = MaterialTheme.colorScheme.surface),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

 //                       OceanWaterInfo()

                            WasteWaterTimeSeries_KHNP()
                            ThermalWasteWaterTimeSeries_KHNP()
                            WaterDegTimeSeries_KHOA()
                            WaterTempTimeSeries_KHOA()
                            OceanWaterInfoTimeSeries()

                            OceanWaterInfoBoxPlotChart()
                            OceanWaterInfoBarChart()
                            OceanWaterInfoDataGrid()




                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {

                            Box(modifier= Modifier.fillMaxWidth(0.5f),
                                contentAlignment = Alignment.Center,

                            ){
                                WindPolarChart_KHOA()
                            }
                            Box(
                                contentAlignment = Alignment.Center,
                            ){
                                WaterInfoGeoChart_KHOA(onClickPoint)
                            }

                        }



                    }

            }

        }
    }
}