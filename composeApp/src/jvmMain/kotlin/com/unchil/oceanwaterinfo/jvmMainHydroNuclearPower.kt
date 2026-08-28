package com.unchil.oceanwaterinfo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.koalaplot.core.xygraph.Point
import kotlin.Double

@Composable
fun jvmMainHydroNuclearPower(){

    val initCenterPoint = remember{ Point(126.934515, 37.385852) }

    val clickPointWaterInfoGeoChart_KHOA = mutableStateOf(initCenterPoint )

    val onClickPointWaterInfoGeoChart_KHOA = { point:Point<Double, Double> ->
        clickPointWaterInfoGeoChart_KHOA.value = point
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Text(
            "Information regarding Korea Hydro & Nuclear Power",
            modifier = Modifier.fillMaxWidth()
                .padding(vertical = 20.dp),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

            val totalWidth = this.maxWidth.value

            Column(
                modifier = Modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                NuclearPlantStatePieChart_KHNP()
                RadioActiveWastePlantStatStackedBarChart_KHNP()
                KHNPRadioActiveWasteStackBarChart()
                WaterTempTimeSeries_KHOA()
                RadioRateBarChart()
                WasteWaterTimeSeries_KHNP()
                ThermalWasteWaterTimeSeries_KHNP()

                var splitFractionVertical by remember {
                    mutableStateOf(
                        0.35f
                    )
                }
                val mapScreenHeight = remember{700.dp}

                Row(
                    modifier = Modifier.fillMaxWidth().height(mapScreenHeight).border(BorderStroke(1.dp, Color.LightGray)).padding(6.dp)
                ) {


                    Box(
                        modifier = Modifier
                            .fillMaxWidth(splitFractionVertical)
                            .fillMaxHeight(),
                        contentAlignment= Alignment.Center
                    ) {

                        WindPolarChart_KHOA()

                    }

                    DraggableVerticalDivider(
                        onDrag = { deltaPx ->
                            val deltaWeight = deltaPx / totalWidth
                            splitFractionVertical =
                                (splitFractionVertical + deltaWeight).coerceIn(
                                    0.1f,
                                    0.9f
                                )
                        }
                    )

                    Row(modifier=Modifier.fillMaxSize()){

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .fillMaxHeight(),
                            contentAlignment= Alignment.Center
                        ) {

                            WaterInfoGeoChart_KHOA(
                                onClickPointWaterInfoGeoChart_KHOA
                            )
                        }

                        CompositionLocalProvider(
                            WaterInfoGeoChartPoint provides clickPointWaterInfoGeoChart_KHOA.value
                        ) {
                            //WaterInfoGeoChart_KHOA_MapScreen 은 항상 height 값이 fix 되어야 표시됨.
                            WaterInfoGeoChart_KHOA_MapScreen(
                                height = mapScreenHeight
                            )
                        }
                    }



                }


                WaterDegTimeSeries_KHOA()


            }

        }


    }
}