package com.unchil.oceanwaterinfo


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.jdbc.Database
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY

object ConfigManager {
    // 1. 반드시 'src' 경로가 아닌 실제 실행 환경에서 접근 가능한 경로를 사용하거나
    //    개발 환경이라면 전체 경로를 절대 경로로 지정합니다.

    private const val CONFIG_FILENAME = "application.json"
    private const val FULL_PATH = "/Users/unchil/AndroidStudioProjects/OceanWaterInfo/collectionServer/src/main/resources/${CONFIG_FILENAME}"

    private val configFile = File(FULL_PATH)
    private val parentDir = configFile.parentFile.toPath()
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    // 현재 메모리에 로드된 설정 정보
    var currentConfig: ConfigData = loadConfig()
        private set

    val conn = Database.connect(
        url = currentConfig.SQLITE_DB?.jdbcURL ?: "",
        driver = currentConfig.SQLITE_DB?.driverClassName ?: "",
    )

    // 최초 로드 함수
    private fun loadConfig(): ConfigData {
        return try {
            // ClassLoader가 아닌 File 객체로 직접 읽어야 실시간 변경분이 반영됩니다.
            val content = configFile.readText()
            json.decodeFromString<ConfigData>(content).also {
                println("[ConfigManager] application.json 로드 성공")
            }
        }catch (e:Exception){
            println("[ConfigManager] 로드 실패: ${e.message}")
            currentConfig
        }
    }

    private var watchJob: Job? = null
    private var watchService: java.nio.file.WatchService? = null

/*
    // 파일 변경 감시 시작
    fun startWatching(scope: CoroutineScope) {
        // 이미 실행 중인 경우 중복 실행 방지
        if (watchJob?.isActive == true) return

        watchJob = scope.launch(Dispatchers.IO) {
            try {
                val service = FileSystems.getDefault().newWatchService()
                watchService = service // 전역 변수에 할당

                service?.let { it ->
                    // 수정(MODIFY) 이벤트 등록
                    parentDir.register(it,  ENTRY_MODIFY, ENTRY_CREATE)

                    println("[ConfigManager] 설정 파일 감시 시작...")

                    while (isActive) {
                        val key = it.take() // 이벤트가 발생할 때까지 대기
                        for (event in key.pollEvents()) {
                            val context = event.context() as Path
                            if (context.toString() == CONFIG_FILENAME) {
                                println("[ConfigManager] 파일 변경 감지! 다시 로드합니다...")

                                // 파일 쓰기가 완료될 때까지 잠시 대기 (안전장치)
                                delay(500)
                                currentConfig = loadConfig()
                            }
                        }
                        if (!key.reset()) break
                    }

                }

            }  catch (e: java.nio.file.ClosedWatchServiceException) {
                println("[ConfigManager] WatchService가 닫혔습니다.")
            } catch (e: Exception) {
                println("[ConfigManager] 감시 중 에러 발생: ${e.message}")
            } finally {
                watchService?.close()
                watchJob = null
                println("[ConfigManager] 감시 리소스 정리 완료.")
            }
        }
    }


    // --- 추가된 종료 함수 ---
    fun stopWatching() {
        println("[ConfigManager] 감시 종료 요청...")
        watchJob?.cancel() // 코루틴 취소
        watchService?.close() // 블로킹 중인 take()를 깨우기 위해 서비스 닫기
        watchJob = null
    }

*/
}