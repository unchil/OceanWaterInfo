package com.unchil.oceanwaterinfo


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.jdbc.Database
import java.nio.file.StandardWatchEventKinds
import kotlin.io.path.Path
import kotlin.io.path.name

object ConfigManager {
    // 1. 반드시 'src' 경로가 아닌 실제 실행 환경에서 접근 가능한 경로를 사용하거나
    //  개발 환경이라면 전체 경로를 절대 경로로 지정합니다.

    private const val CONFIG_FILENAME = "application.json"

    private const val CONFIG__FILEPATH = "/Users/unchil/AndroidStudioProjects/OceanWaterInfo/collectionServer/src/main/resources"

    private  val configFilePath = Path("${CONFIG__FILEPATH}/${CONFIG_FILENAME}")
    private val configFile = configFilePath.toFile()

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    // 현재 메모리에 로드된 설정 정보
    var currentConfig: ConfigData = loadConfig()
        private set

    // 코루틴 관리용 Job
    private var watchJob: Job? = null
    private var watchService: java.nio.file.WatchService? = null


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

    /**
     * 파일 변화 감지 시작
     */
    fun startWatching(scope: CoroutineScope) {
        // 이미 실행 중이면 중복 실행 방지
        if (watchJob?.isActive == true) return

        watchJob = scope.launch(Dispatchers.IO) {
            try {
                // 1. WatchService 생성
                watchService =  configFilePath.parent.fileSystem.newWatchService()

                // 2. 부모 디렉토리를 감시 대상으로 등록 (수정 및 생성 이벤트 모두 감지)
                // IntelliJ의 'Safe Write' 기능은 파일을 삭제 후 생성하므로 ENTRY_CREATE가 필수입니다.
                configFilePath.parent.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_CREATE
                )

                println("[ConfigManager] 감시 시작: ${configFilePath.parent.name}")

                while (isActive) {
                    // 3. 이벤트 대기 (Blocking 호출)
                    watchService?.take()?.let { key ->
                        for (event in key.pollEvents()) {
                            // 4. 감시 중인 파일 이름과 일치하는지 확인
                            if ( event.context().toString().equals(CONFIG_FILENAME)) {
                                // 파일이 완전히 저장될 때까지 아주 잠시 대기 (파일 잠금 방지)
                                delay(500)
                                currentConfig = loadConfig()
                            }
                        }

                        // 5. 키 리셋 (실패 시 감시 종료)
                        if (!key.reset()) break
                    }
                }


            } catch (e: Exception) {
                if (e is java.nio.file.ClosedWatchServiceException) {
                    println("[ConfigManager] 감시 코루틴이 취소되었습니다.")
                } else {
                    println("[ConfigManager] 감시 중 에러 발생: ${e.message}")
                }
            } finally {
                watchService?.close()
                println("[ConfigManager] 감시 리소스 정리 중...")
            }
        }

    }


    /**
     * 파일 변화 감지 종료
     */
    fun stopWatching() {
        println("[ConfigManager] 감시 종료 요청")
        watchJob?.cancel()
        watchService?.close()
        watchJob = null
    }

}