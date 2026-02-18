package com.unchil.oceanwaterinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import dev.datlag.kcef.KCEF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


fun main() = application {

    var initialized by remember { mutableStateOf(false) }
    var download by remember { mutableStateOf(-1) }
    var errorMessage by remember {mutableStateOf("")}

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            KCEF.init(
                builder = {
                    progress {
                        onInitialized {
                            initialized = true
                        }
                        onDownloading {
                            download = it.toInt()
                        }
                    }
                },
                onError = {
                    errorMessage = it?.printStackTrace().toString()
                }
            )
        }
    }


    val state = WindowState(
        size = DpSize(1400.dp, 1000.dp),
        position = WindowPosition(Alignment.Center)
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "OceanWaterInformation",
        state = state,
    ) {
        MaterialTheme(
            colorScheme = getColorScheme(false)
        ) {
            CompositionLocalProvider(LocalPlatform provides getPlatform()) {


                var splitFractionHorizontal by remember { mutableStateOf(0.5f) }


                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val totalHeight = constraints.maxHeight.toFloat()

                    Column(
                        modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colorScheme.surface)
                    ) {

                        Text(
                            "Korea Ocean Water Information",
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Box(modifier = Modifier.fillMaxHeight(splitFractionHorizontal)) {

                            Column(
                                modifier = paddingMod.fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .safeContentPadding(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {

                                OceanWaterInfoBoxPlotChart()

                                OceanWaterInfoLineChart()

                                OceanWaterInfoBarChart()

                                OceanWaterInfoLineChart_MOF()

                                OceanWaterInfoDataGrid()

                                OceanWaterInfoGeoChart()

                            }
                        }


                        DraggableHorizontalDivider(
                            onDrag = { deltaPx ->
                                val deltaWeight = deltaPx / totalHeight
                                // 박스 2의 크기를 조절
                                splitFractionHorizontal = (splitFractionHorizontal + deltaWeight).coerceIn(0.1f, 0.9f)
                            }
                        )

                        // 1. 첫 번째와 두 번째 박스의 비율 (초기값: 각 0.3f)
                        var weight1 by remember { mutableStateOf(0.4f) }
                        var weight2 by remember { mutableStateOf(0.3f) }

                        // 전체 너비를 계산하기 위해 BoxWithConstraints 사용
                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                            val totalWidthPx = with(LocalDensity.current) { maxWidth.toPx() }

                            Row(
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                // 2. 왼쪽 영역
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(weight1),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Column(
                                        modifier = Modifier.verticalScroll(rememberScrollState()),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {

                                        OceanWaterInfoGeoChart()
                                    }
                                }


                                // --- 첫 번째 구분선 (Box 1과 Box 2 사이 조절) ---
                                DraggableVerticalDivider(
                                    onDrag = { deltaPx ->
                                        val deltaWeight = deltaPx / totalWidthPx
                                        // 박스 1의 크기를 조절 (박스 2의 영역을 침범하거나 늘림)
                                        // 박스 1과 2의 합이 너무 커지지 않도록 적절히 제한 가능
                                        weight1 = (weight1 + deltaWeight).coerceIn(0.1f, 0.8f)
                                    }
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(weight2),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Column(
                                        modifier = Modifier.verticalScroll(rememberScrollState()),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ){
                                        OceanWaterInfoBarChart()
                                        HorizontalDivider()
                                        OceanWaterInfoBoxPlotChart()
                                    }
                                }


                                // --- 두 번째 구분선 (Box 2와 Box 3 사이 조절) ---
                                DraggableVerticalDivider(
                                    onDrag = { deltaPx ->
                                        val deltaWeight = deltaPx / totalWidthPx
                                        // 박스 2의 크기를 조절
                                        weight2 = (weight2 + deltaWeight).coerceIn(0.1f, 0.8f)
                                    }
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight((1f - weight1 - weight2).coerceIn(0.1f, 0.8f)),
                                    contentAlignment = Alignment.Center,
                                ) {

                                    Surface(
                                        shadowElevation = 2.dp,
                                        modifier = Modifier.padding(10.dp),
                                        shape = RoundedCornerShape(6.dp)

                                    ) {
                                        SimpleMapScreen(initialized, download, errorMessage)
                                  //      DesktopMapScreen(initialized, download, errorMessage)
                                    }




                                }
                            }

                        }

                    }

                }







            }
        }
    }


    DisposableEffect(Unit) {
        onDispose {
            KCEF.disposeBlocking()
        }
    }

}

