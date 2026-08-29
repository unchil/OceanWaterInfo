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
import androidx.compose.material.CircularProgressIndicator
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
import kotlinx.coroutines.flow.collectLatest
import java.io.File

enum class KCEF_PROGRESS_STATE {
    onDownloading, onLocating, onExtracting, onInstall, onInitializing, onInitialized
}


val LOGGER = KtorSimpleLogger("jvmMain")

val WaterInfoGeoChartPoint = compositionLocalOf<Point<Double,Double>> { error("No Point found!") }
val OceanWaterInfoGeoChartPoint = compositionLocalOf<Point<Double,Double>> { error("No Point found!") }


fun main() = application {


    val bundleLocation =  File("/Users/unchil/AndroidStudioProjects/OceanWaterInfo/composeApp/build/")

    var initialized by remember { mutableStateOf(false) }
    var initError by remember { mutableStateOf("") }
    var isRestartRequired by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(-1F) }
    var progressState by remember{mutableStateOf(KCEF_PROGRESS_STATE.onDownloading)}


    LaunchedEffect(Unit) {
        try {
            // KCEF.init은 JBR에 내장된 JCEF를 자동으로 탐색합니다.
            KCEF.init(
                builder = {

                    installDir(File(bundleLocation, "kcef-bundle"))

                    progress {
                        onDownloading { percent ->
                            downloadProgress = percent
                        }
                        onLocating {
                            progressState = KCEF_PROGRESS_STATE.onLocating
                        }
                        onExtracting {
                            progressState = KCEF_PROGRESS_STATE.onExtracting
                        }
                        onInstall {
                            progressState = KCEF_PROGRESS_STATE.onInstall
                        }

                        onInitializing {
                            progressState = KCEF_PROGRESS_STATE.onInitializing
                        }

                        onInitialized {
                            progressState = KCEF_PROGRESS_STATE.onInitialized
                            initialized = true
                        }

                    }
                },
                onError = { error ->
                    initError = error?.localizedMessage ?: "KCEF 초기화 실패"
                },
                onRestartRequired = {
                    // 다운로드가 완료되어 재시작이 필요한 상태임을 기록
                    isRestartRequired = true
                }
            )
        } catch (e: Exception) {
            initError = e.localizedMessage ?: "알 수 없는 오류"
        }
    }



    val state = WindowState(
        size = DpSize(1400.dp, 1000.dp),
        position = WindowPosition(Alignment.Center)
    )


    var selectedTabIndex by remember { mutableStateOf(0) } // 탭 인덱스 상태


    Window(
        onCloseRequest = {
            KCEF.disposeBlocking()
            exitApplication()
        },
        title = "Environmental Observation Information",
        state = state,
    ) {

     //   MainView(modifier = Modifier.fillMaxSize() )

        MaterialTheme(colorScheme = getColorScheme(false)) {

            CompositionLocalProvider(LocalPlatform provides getPlatform()) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    if (isRestartRequired) {
                        androidx.compose.material.Text("안전한 브라우저 실행을 위해 앱을 다시 시작해 주세요.")
                    } else if (initialized) {
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
                        }
                    } else if(initError.isNotEmpty()){
                        androidx.compose.material.Text(initError)
                    }else {
                        when(progressState){
                            KCEF_PROGRESS_STATE.onDownloading -> {
                                androidx.compose.material.Text("엔진 다운로드 중: ${downloadProgress.toInt()}%")
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(progress = downloadProgress / 100f)
                            }
                            KCEF_PROGRESS_STATE.onLocating -> {
                                androidx.compose.material.Text("엔진 위치 찾는 중...")
                            }
                            KCEF_PROGRESS_STATE.onExtracting -> {
                                androidx.compose.material.Text("압축 해제 중...")
                            }
                            KCEF_PROGRESS_STATE.onInitializing -> {
                                androidx.compose.material.Text("초기화 중...")
                            }
                            KCEF_PROGRESS_STATE.onInstall -> {
                                androidx.compose.material.Text("설치 중...")
                            }
                            KCEF_PROGRESS_STATE.onInitialized -> {
                                androidx.compose.material.Text("완료!")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        CircularProgressIndicator()
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
