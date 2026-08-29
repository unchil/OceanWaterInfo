package com.unchil.oceanwaterinfo


import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.LinearProgressIndicator
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
import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import java.io.File


val LOGGER = KtorSimpleLogger("jvmMain")

val WaterInfoGeoChartPoint = compositionLocalOf<Point<Double,Double>> { error("No Point found!") }
val OceanWaterInfoGeoChartPoint = compositionLocalOf<Point<Double,Double>> { error("No Point found!") }


fun main() = application {

    var downloadProgress by remember { mutableStateOf(-1F) }
    var initialized by remember { mutableStateOf(false) } // if true, KCEF can be used to create clients, browsers etc
    val bundleLocation =  File("/Users/unchil/AndroidStudioProjects/OceanWaterInfo/composeApp/build/")
    var errorMessage by remember {mutableStateOf("")}

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                KCEF.init(
                    builder = {
                       installDir(File(bundleLocation, "kcef-bundle")) // recommended, but not necessary
                        progress {
                            onDownloading {
                                downloadProgress = it
                                // use this if you want to display a download progress for example
                            }
                            onInitialized {
                                initialized = true
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

     //   MainView(modifier = Modifier.fillMaxSize() )

        MaterialTheme(colorScheme = getColorScheme(false)) {

            CompositionLocalProvider(LocalPlatform provides getPlatform()) {

                Column(
                    modifier = Modifier.fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.surface)
                ) {

                    if(initialized){
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
                                    jvmMainAirQuality()
                                }
                                1 -> {
                                    jvmMainOceanWaterQuality( )
                                }
                                2 -> {
                                    jvmMainTidalForecastMap( )
                                }
                                3 -> {
                                    jvmMainOceanCurrentSpeedMap( )
                                }

                                4 -> {
                                    jvmMainHydroNuclearPower( )
                                }
                                5 -> {
                                    jvmMainCoastalFloodingMap( )
                                }
                            }
                        }
                    }else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (downloadProgress > -1f) {
                                    // 다운로드 중일 때: 퍼센트 텍스트와 막대형 게이지 표시
                                    androidx.compose.material.Text("엔진 다운로드 중: ${downloadProgress.toInt()}%")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(progress = downloadProgress / 100f)
                                } else {
                                    // 초기 설정 중일 때: 대기 메시지와 회전형 인디케이터 표시
                                    androidx.compose.material.Text("WebView 엔진을 초기화하고 있습니다...")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    androidx.compose.material.CircularProgressIndicator()
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
