package com.unchil.oceanwaterinfo


import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import dev.datlag.kcef.KCEF
import io.github.koalaplot.core.xygraph.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext



val WaterInfoGeoChartPoint = compositionLocalOf<Point<Double,Double>> { error("No Point found!") }
val OceanWaterInfoGeoChartPoint = compositionLocalOf<Point<Double,Double>> { error("No Point found!") }

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
                },

            )
        }
    }


    val state = WindowState(
        size = DpSize(1400.dp, 1000.dp),
        position = WindowPosition(Alignment.Center)
    )


    var selectedTabIndex by remember { mutableStateOf(0) } // 탭 인덱스 상태


    Window(
        onCloseRequest = ::exitApplication,
        title = "Environmental Observation Information",
        state = state,
    ) {
        MaterialTheme(colorScheme = getColorScheme(false)) {

            CompositionLocalProvider(LocalPlatform provides getPlatform()) {

    //            OceanWaterInfo()

                Column(
                    modifier = Modifier.fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.surface)
                ) {
                    SecondaryTabRow(
                        selectedTabIndex,
                        Modifier.fillMaxWidth(),
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primary,
                         { HorizontalDivider() }
                    ) {
                        MAIN_TAB_ITEMS.forEachIndexed { index, title ->


                            val interactionSource = remember { MutableInteractionSource() }
                            // InteractionSource의 상태 변화를 직접 감지하는 로직
                            LaunchedEffect(interactionSource) {
                                interactionSource.interactions.collectLatest { interaction ->
                                    when (interaction) {
                                        is PressInteraction.Press -> {
                                            if (selectedTabIndex != index) {
                                                selectedTabIndex = index
                                            }
                                        }
                                    }
                                }
                            }

                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = {
                                    // 고수준 onClick도 유지하되, 위 LaunchedEffect가 보조 역할을 수행합니다.
                                    if (selectedTabIndex != index) {
                                        selectedTabIndex = index
                                    }
                                },
                                text = {
                                    Text(
                                        text = title,
                                        fontSize = 16.sp,
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Light,
                                        // 리사이즈 도중 텍스트가 잘려나가는 것을 방지
                                        softWrap = false,
                                        maxLines = 1
                                    )
                                },
                                // interactionSource를 명시적으로 관리하면 시스템 부하 상황에서 더 잘 반응함
                                interactionSource = interactionSource
                            )
                        }
                    }
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (selectedTabIndex) {
                            0 -> {
                                jvmMainAirQuality(initialized, download, errorMessage)
                            }
                            1 -> {
                                jvmMainOceanWaterQuality(
                                    initialized,
                                    download,
                                    errorMessage
                                )
                            }
                            2 -> {
                                jvmMainTidalForecastMap(
                                    initialized,
                                    download,
                                    errorMessage
                                )
                            }
                            3 -> {
                                jvmMainOceanCurrentSpeedMap(
                                    initialized,
                                    download,
                                    errorMessage
                                )
                            }

                            4 -> {
                                jvmMainHydroNuclearPower(
                                    initialized,
                                    download,
                                    errorMessage
                                )
                            }
                            5 -> {
                                jvmMainCoastalFloodingMap(
                                    initialized,
                                    download,
                                    errorMessage
                                )
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

