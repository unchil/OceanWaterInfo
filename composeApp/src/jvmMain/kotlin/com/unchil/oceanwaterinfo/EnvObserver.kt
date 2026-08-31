package com.unchil.oceanwaterinfo


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.TextButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.launch
import java.io.File
import java.lang.management.ManagementFactory


val LOGGER = KtorSimpleLogger("jvmMain")

val WaterInfoGeoChartPoint = compositionLocalOf<Point<Double,Double>> { error("No Point found!") }
val OceanWaterInfoGeoChartPoint = compositionLocalOf<Point<Double,Double>> { error("No Point found!") }


fun main() = application {

    val coroutineScope = rememberCoroutineScope()
    // 1. 시스템 프로퍼티에서 홈 디렉토리 경로를 가져옵니다.
    val userHome = System.getProperty("user.home")

    var initialized by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(-1F) }
    var progressMsg by remember { mutableStateOf("다운로드 중...") }
    var isDownload by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {

        KCEF.init(
            builder = {

                installDir(File(userHome, ".kcef-bundle"))

                progress {

                    onLocating {
                        isDownload = false
                        progressMsg = "엔진 위치 찾는 중..."
                    }
                    onDownloading { percent ->
                        isDownload = true
                        downloadProgress = percent
                        progressMsg = "다운로드 중: ${percent.toInt()}%"
                    }
                    onExtracting {
                        isDownload = false
                        progressMsg = "압축 해제 중..."
                    }
                    onInitializing {
                        isDownload = false
                        progressMsg = "초기화 중..."
                    }

                    onInstall{
                        isDownload = false

                        progressMsg =  "설치 중..."
                        // 1. 소스 및 대상 경로 정의

                        val kcefDir = File(userHome, ".kcef-bundle") // installDir과 동일한 위치
                        val sourcePath = "${kcefDir}/Frameworks/cef_server.app/Contents/Frameworks"
                        val destPath = "${kcefDir}/Frameworks"

                        val sourceDir = File(sourcePath)
                        val destDir = File(destPath)

                        // 2. 소스 경로가 존재할 경우 파일 이동 수행
                        if (sourceDir.exists() && sourceDir.isDirectory) {
                            sourceDir.listFiles()?.forEach { file ->
                                val targetFile = File(destDir, file.name)
                                file.renameTo(targetFile)
                            }

                            // 핵심: 설치 완료를 알리는 lock 파일 생성
                            val lockFile = File(kcefDir, "install.lock")
                            if (!lockFile.exists()) {
                                lockFile.createNewFile()
                            }
                        }

                    }

                    onInitialized {
                        isDownload = false
                        progressMsg = "완료!"
                        initialized = true
                    }

                }
            },
            onError = { error ->
                progressMsg = error?.localizedMessage ?: "알 수 없는 오류"
            },
            onRestartRequired = {

            }
        )

    }

    val restartHandler = {
        try {
            // 2. KCEF 자원 해제 (완료될 때까지 블로킹됨)
            KCEF.disposeBlocking()

            // 3. 현재 실행 환경 정보 수집
            val java = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java"
            //     val vmArguments = ManagementFactory.getRuntimeMXBean().inputArguments
            // ... ProcessBuilder 부분 수정
            val vmArguments = ManagementFactory.getRuntimeMXBean().inputArguments.filter {
                !it.contains("-agentlib:jdwp") && !it.contains("-Xdebug")
            }

            val classpath = System.getProperty("java.class.path")
            val mainClass = "com.unchil.oceanwaterinfo.EnvObserverKt"

            val command = mutableListOf(java)
            command.addAll(vmArguments)
            command.add("-cp")
            command.add(classpath)
            command.add(mainClass)

            // 4. 새 프로세스 시작
            // inheritIO()를 사용하면 새 프로세스의 로그를 현재 콘솔에서도 볼 수 있어 디버깅에 유리합니다.
           // ProcessBuilder(command).inheritIO().start()
            ProcessBuilder(command).start()


            // 5. 현재 프로세스 종료
            System.exit(0)
        } catch (e: Exception) {
            e.printStackTrace()
            System.exit(0)
            // 에러 발생 시 종료하지 않고 사용자에게 알릴 수 있음
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

      //  MainView(modifier = Modifier.fillMaxSize() )


        MaterialTheme(colorScheme = getColorScheme(false)) {

            CompositionLocalProvider(LocalPlatform provides getPlatform()) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                     if (initialized) {

                         if(progressMsg.contains("install.lock") || downloadProgress.equals(100.0f) ){

                             TextButton(
                                 onClick = {
                                     // 1. 코루틴 스코프 내에서 실행 (필요한 경우)
                                     coroutineScope.launch(Dispatchers.IO) {
                                         restartHandler.invoke()
                                     }
                                 } ,
                                 shape = MaterialTheme.shapes.medium,
                                 border = BorderStroke(1.dp, Color.Red),
                             ){
                                 Text("The WebView installation is complete, Restart Application.")
                             }


                         }else{

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
                                             AirQuality()
                                         }
                                         1 -> {
                                             jvmMainOceanWaterQuality( )
                                         }
                                         2 -> {
                                             TidalForecastMap( )
                                         }
                                         3 -> {
                                             OceanCurrentSpeedMap( )
                                         }

                                         4 -> {
                                             jvmMainHydroNuclearPower( )
                                         }
                                         5 -> {
                                             CoastalFloodingMap( )
                                         }
                                     }
                                 }
                             }

                         }




                    }else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ){
                            androidx.compose.material.Text(progressMsg)
                            Spacer(modifier = Modifier.height(8.dp))

                            if(isDownload){
                                LinearProgressIndicator(progress = downloadProgress / 100f)
                            } else {
                                CircularProgressIndicator()
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
