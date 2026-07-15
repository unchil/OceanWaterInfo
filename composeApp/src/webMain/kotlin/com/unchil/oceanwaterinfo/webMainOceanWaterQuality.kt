package com.unchil.oceanwaterinfo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun webMainOceanWaterQuality(){

    val density = LocalDensity.current
    val bottomBarHeight = remember{100.dp}

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        val isReloadOceanWaterInfoMap = remember { mutableStateOf(0) }

        Text(
            "Korea Ocean Water Information",
            modifier = Modifier.fillMaxWidth()
                .padding(vertical = 15.dp),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val totalWidth = this.maxWidth.value

            Column(
                modifier = Modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(color = MaterialTheme.colorScheme.surface),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OceanWaterInfo_MOF()
                OceanWaterInfoTimeSeries()
                OceanWaterInfoBoxPlotChart()
                OceanWaterInfoBarChart()
                OceanWaterInfoDataGrid()

                var splitFractionVertical by remember {
                    mutableStateOf(
                        0.5f
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
                        OceanWaterInfoGeoChart(
                            onClickPoint = sendFlyToTargetOceanWater,
                            sendAddMarkerClusterer = sendAddMarkerClusterer,
                            onClickPointOceanWaterInfoGeoChart = sendFlyToTargetOceanWater,
                            isReload = isReloadOceanWaterInfoMap.value
                        )
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

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {

                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .height(mapScreenHeight - bottomBarHeight)
                                .onGloballyPositioned { coordinates ->
                                    syncHtmlElementPosition(
                                        coordinates,
                                        density,
                                        DIV_WEB_MAIN,
                                        DIV_OCEAN_WATER_INFO
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            // 여기는 비어있지만, 실제로는 iframe_waterInfo div가 이 위를 덮게 됩니다.
                        }

                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {// [Reload, Tooltips, Symbol, Legend]
                            val bottomBarOpt =
                                listOf(true, false, false, false)
                            ChartFeatureControls(
                                onChangeFlag = { label, value ->
                                    when (label) {
                                        "Reload" -> isReloadOceanWaterInfoMap.value = kotlin.time.Clock.System.now().nanosecondsOfSecond
                                    }
                                },
                                bottomBarOpt = bottomBarOpt
                            )

                        }
                    }
                }
            }
        }
    }

}