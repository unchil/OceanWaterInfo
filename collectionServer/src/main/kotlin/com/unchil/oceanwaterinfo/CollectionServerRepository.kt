package com.unchil.oceanwaterinfo


import io.ktor.client.statement.bodyAsText
import io.ktor.http.encodeURLParameter
import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.statements.api.ExposedBlob
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.batchReplace
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.add
import org.jetbrains.kotlinx.dataframe.api.concat
import org.jetbrains.kotlinx.dataframe.api.count
import org.jetbrains.kotlinx.dataframe.api.flatten
import org.jetbrains.kotlinx.dataframe.api.groupBy
import org.jetbrains.kotlinx.dataframe.api.head
import org.jetbrains.kotlinx.dataframe.api.pivot
import org.jetbrains.kotlinx.dataframe.api.rename
import org.jetbrains.kotlinx.dataframe.api.rows
import org.jetbrains.kotlinx.dataframe.api.schema
import org.jetbrains.kotlinx.dataframe.api.update
import org.jetbrains.kotlinx.dataframe.api.values
import org.jetbrains.kotlinx.dataframe.api.with
import org.jetbrains.kotlinx.dataframe.io.read
import org.jetbrains.kotlinx.dataframe.io.readJson
import org.jetbrains.kotlinx.dataframe.io.toCsvStr
import org.json.XML
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.math.ceil
import kotlin.time.Clock

class CollectionServerRepository {
    internal val LOGGER = KtorSimpleLogger( CollectionServerRepository::class.java.name )

    init {
        transaction(ConfigManager.conn) {
            addLogger(StdOutSqlLogger)
        }
    }


     suspend fun getKHNP_PlantStates() {

        val url_PlantStates = "${ConfigManager.currentConfig.KHNP?.endPoint}/${ConfigManager.currentConfig.KHNP?.subPath?.NuclearPlantStates}?serviceKey=${ConfigManager.currentConfig.KHNP?.serviceKey}"

        val genNames = listOf("WS", "KR", "YK", "SU", "UJ")

        val myCollectionTime = Clock.System.now().toLocalDateTime(TimeZone.of("Asia/Seoul"))
            .format(LocalDateTime.Format { byUnicodePattern("yyyy-MM-dd HH:mm") })

        val plantInfo = mutableListOf<KHNPPlantInfo>()
        val unitInfoList = mutableListOf<KHNPPlantOperationInfo>()

        genNames.forEach{ genName ->

            val url = "${url_PlantStates}&SITE_CD=${genName}"

            try {
                CollectionServerRestApi.callKHNP_PlantStates_xml(url).let {

                    val element = Json.parseToJsonElement( it )
                    val item = element.jsonObject["response"]?.jsonObject?.get("body")?.jsonObject?.get("items")?.jsonObject?.get("item")

                    var siteCd = ""
                    var siteMm = ""
                    var siteNm = ""
                    var unitCd = ""
                    var unitDttm = ""
                    var unitNm = ""
                    var unitSt = ""

                    var index = 0

                    item?.jsonObject?.forEach { (key, value) ->


                        if(key.equals("siteCd")){
                            siteCd = value.toString().removeSurrounding("\"")
                        }

                        if(key.equals("siteMm")){
                            siteMm = value.toString().removeSurrounding("\"")
                        }

                        if(key.equals("siteNm")){
                            siteNm = value.toString().removeSurrounding("\"")
                        }


                        if(index == 2){
                            plantInfo.add(KHNPPlantInfo(siteCd, siteNm, siteMm))
                        }

                        if(key.contains("^unit_[0-9]+Cd$".toRegex())) {
                            unitCd = value.toString().removeSurrounding("\"")
                        }
                        if(key.contains("^unit_[0-9]+Dttm$".toRegex())) {
                            unitDttm = value.toString().removeSurrounding("\"")
                        }
                        if(key.contains("^unit_[0-9]+Nm$".toRegex())) {
                            unitNm = value.toString().removeSurrounding("\"")
                        }
                        if(key.contains("^unit_[0-9]+St$".toRegex())) {
                            unitSt = value.toString().removeSurrounding("\"")
                        }

                        if( index > 3 && index%4 == 2   ){
                            unitInfoList.add( KHNPPlantOperationInfo( myCollectionTime, siteCd, genName, unitCd,unitDttm, unitNm, unitSt))
                            unitCd = ""
                            unitDttm = ""
                            unitNm = ""
                            unitSt = ""
                        }

                        index++

                    }
                }

            } catch(e:Exception ){
                LOGGER.error("KHNP_PlantInfo Batch Insert Error: ${e.localizedMessage}")
            }
        }

         transaction(ConfigManager.conn) {
             SchemaUtils.create(KHNP_PlantInfo)

             try {
                 // 개별 insert 대신 batchInsert 사용 (성능 핵심)
                 KHNP_PlantInfo.batchInsert(plantInfo, true, false) { row ->
                     this[KHNP_PlantInfo.siteCd] = row.siteCd
                     this[KHNP_PlantInfo.siteNm] = row.siteNm
                     this[KHNP_PlantInfo.siteMm] = row.siteMm
                 }

             } catch (e: Exception) {
                 LOGGER.error("KHNP_PlantInfo Batch Insert Error: ${e.localizedMessage}")
             }

             SchemaUtils.create(KHNP_PlantOperationInfo)

             try {
                 // 개별 insert 대신 batchInsert 사용 (성능 핵심)
                 KHNP_PlantOperationInfo.batchInsert(unitInfoList, true, false) { row ->
                     this[KHNP_PlantOperationInfo.collectionTime] = row.collectionTime
                     this[KHNP_PlantOperationInfo.siteCd] = row.siteCd
                     this[KHNP_PlantOperationInfo.genName] = row.genName
                     this[KHNP_PlantOperationInfo.unitCd] = row.unitCd
                     this[KHNP_PlantOperationInfo.unitDttm] = row.unitDttm
                     this[KHNP_PlantOperationInfo.unitNm] = row.unitNm
                     this[KHNP_PlantOperationInfo.unitSt] = row.unitSt
                 }

             } catch (e: Exception) {
                 LOGGER.error("KHNP_PlantOperationInfo Batch Insert Error: ${e.localizedMessage}")
             }

         }

    }


    fun loadKHNP_Service(url:String, genNames:List<String>): DataFrame<*> {
        val now = Clock.System.now()
        val rows = mutableListOf<DataFrame<*>>()
        val myCollectionTime = now.toLocalDateTime(TimeZone.of("Asia/Seoul")).format(LocalDateTime.Format { byUnicodePattern("yyyy-MM-dd HH:mm") })

        genNames.forEach { genName ->
            val urlPath = url + "&genName=${genName}"
            try {
                val df_json = DataFrame.readJson(
                    XML.toJSONObject(DataFrame.read(urlPath).toCsvStr()).toString().byteInputStream()
                )
                val instanceDf =
                    df_json.get("response").get("body").get("items").get("item")[0] as DataFrame<*>

                val updatedDf = instanceDf.add {
                    "collectionTime" from { myCollectionTime }
                    "genName" from {
                        when(genName){
                            "2100" -> "KR"
                            "2200" -> "WS"
                            "2300" -> "YK"
                            "2400" -> "UJ"
                            "2800" -> "SU"
                            else -> genName
                        }
                    }
                }
                rows.add(updatedDf)
            }catch(e:Exception){
                print(e.localizedMessage)
                print(urlPath)
            }
        }

        return rows.concat()
    }


    fun getKHNP_ThermalWasteWater(){
        val url = "${ConfigManager.currentConfig.KHNP?.endPoint}/${ConfigManager.currentConfig.KHNP?.subPath?.ThermalWasteWater}?serviceKey=${ConfigManager.currentConfig.KHNP?.serviceKey}"
        val concatDf = loadKHNP_Service(url,  listOf("WS", "KR", "YK", "SU", "UJ"))

        val updatedDf = concatDf.update ( "name" ).with {
            val currentName = it.toString() // 현재 행의 name 값
            when {
                currentName.contains("RM001")-> "RM001"
                currentName.contains("RM002")-> "RM002"
                currentName.contains("RM005")-> "RM005"
                currentName.contains("RM006")-> "RM006"
                else -> it // 조건에 해당하지 않으면 원래 값 유지
            }
        }
        val pivotedDf = updatedDf.pivot ( "name"  )
            .groupBy ( "collectionTime" , "genName" )
            .values ( "value" , "time" )
            .flatten()


        val result = pivotedDf.rename(
            "value" to "rm001",
            "time" to "rm001_time",
            "value1" to "rm002",
            "time1" to "rm002_time",
            "value2" to "rm005",
            "time2" to "rm005_time",
            "value3" to "rm006",
            "time3" to "rm006_time",
        )

        LOGGER.debug("\n ${::getKHNP_ThermalWasteWater.name}  Schema[${result.schema()}]")
        LOGGER.info("\n ${::getKHNP_ThermalWasteWater.name}  Count:[${result.count()}]")

        transaction(ConfigManager.conn) {
            SchemaUtils.create(KHNP_ThermalWasteWater)


            try {
                // 개별 insert 대신 batchInsert 사용 (성능 핵심)
                KHNP_ThermalWasteWater.batchInsert(result.rows(), true, false) { row ->
                    this[KHNP_ThermalWasteWater.time] = row["collectionTime"].toString()
                    this[KHNP_ThermalWasteWater.genName] = row["genName"].toString()
                    this[KHNP_ThermalWasteWater.rm001] = row["rm001"].toString()
                    this[KHNP_ThermalWasteWater.rm001_time] = row["rm001_time"].toString()
                    this[KHNP_ThermalWasteWater.rm002] = row["rm002"].toString()
                    this[KHNP_ThermalWasteWater.rm002_time] = row["rm002_time"].toString()
                    this[KHNP_ThermalWasteWater.rm005] = row["rm005"].toString()
                    this[KHNP_ThermalWasteWater.rm005_time] = row["rm005_time"].toString()
                    this[KHNP_ThermalWasteWater.rm006] = row["rm006"].toString()
                    this[KHNP_ThermalWasteWater.rm006_time] = row["rm006_time"].toString()
                }

            } catch (e: Exception) {
                LOGGER.error("Batch Insert Error: ${e.localizedMessage}")
            }

        }

    }



    fun getKHNP_WasteWater(){

        val url = "${ConfigManager.currentConfig.KHNP?.endPoint}/${ConfigManager.currentConfig.KHNP?.subPath?.WasteWater}?serviceKey=${ConfigManager.currentConfig.KHNP?.serviceKey}"
        val concatDf = loadKHNP_Service(url,  listOf("WS", "KR", "YK", "SU", "UJ"))

        val updatedDf = concatDf.update ("name" ).with {
            val currentName = it.toString() // 현재 행의 name 값
            when {
                currentName.contains("FLW00") || currentName.contains("TM001") -> "TM001"
                currentName.contains("PHY00") || currentName.contains("TM002") -> "TM002"
                else -> it // 조건에 해당하지 않으면 원래 값 유지
            }
        }

        val pivotedDf = updatedDf.pivot("name")
            .groupBy ( "collectionTime" , "genName" )
            .values( "value", "time" )
            .flatten()

        val result = pivotedDf.rename(
            "value" to "tm001",
            "time" to "tm001_time",
            "value1" to "tm002",
            "time1" to "tm002_time"
        )


        LOGGER.debug("\n ${::getKHNP_WasteWater.name}  Schema[${result.schema()}]")
        LOGGER.info("\n ${::getKHNP_WasteWater.name}  Count:[${result.count()}]")

        transaction(ConfigManager.conn) {
            SchemaUtils.create(KHNP_WasteWater)

            try {
                // 개별 insert 대신 batchInsert 사용 (성능 핵심)
                KHNP_WasteWater.batchInsert(result.rows(), true, false) { row ->
                    this[KHNP_WasteWater.time] = row["collectionTime"].toString()
                    this[KHNP_WasteWater.genName] = row["genName"].toString()
                    this[KHNP_WasteWater.tm001] = row["tm001"].toString()
                    this[KHNP_WasteWater.tm001_time] = row["tm001_time"].toString()
                    this[KHNP_WasteWater.tm002] = row["tm002"].toString()
                    this[KHNP_WasteWater.tm002_time] = row["tm002_time"].toString()
                }

            } catch (e: Exception) {
                LOGGER.error("Batch Insert Error: ${e.localizedMessage}")
            }


        }

    }


    fun getKHNP_RadioRate(){
        val url = "${ConfigManager.currentConfig.KHNP?.endPoint}/${ConfigManager.currentConfig.KHNP?.subPath?.RadioRate}?serviceKey=${ConfigManager.currentConfig.KHNP?.serviceKey}"
        val result = loadKHNP_Service(url,  listOf("WS", "KR", "YK", "SU", "UJ"))


        LOGGER.debug("\n ${::getKHNP_RadioRate.name}  Schema[${result.schema()}]")
        LOGGER.info("\n ${::getKHNP_RadioRate.name}  Count:[${result.count()}]")

        transaction(ConfigManager.conn) {
            SchemaUtils.create(KHNP_RadioRate)


            try {
                // 개별 insert 대신 batchInsert 사용 (성능 핵심)
                KHNP_RadioRate.batchInsert(result.rows(), true, false) { row ->
                    this[KHNP_RadioRate.collectionTime] = row["collectionTime"].toString()
                    this[KHNP_RadioRate.time] = row["time"].toString()
                    this[KHNP_RadioRate.genName] = row["genName"].toString()
                    this[KHNP_RadioRate.name] = row["name"].toString()
                    this[KHNP_RadioRate.expl] = row["expl"].toString()
                    this[KHNP_RadioRate.value] = row["value"].toString()
                }

            } catch (e: Exception) {
                LOGGER.error("Batch Insert Error: ${e.localizedMessage}")
            }


        }
    }

    fun getKHNP_RadioActiveWaste(){
        val url = "${ConfigManager.currentConfig.KHNP?.endPoint}/${ConfigManager.currentConfig.KHNP?.subPath?.RadioActiveWaste}?serviceKey=${ConfigManager.currentConfig.KHNP?.serviceKey}"
        val result = loadKHNP_Service(url,  listOf("2100", "2200", "2300", "2400", "2800") )


        LOGGER.debug("\n ${::getKHNP_RadioActiveWaste.name}  Schema[${result.schema()}]")
        LOGGER.info("\n ${::getKHNP_RadioActiveWaste.name}  Count:[${result.count()}]")

        transaction(ConfigManager.conn) {
            SchemaUtils.create(KHNP_RadioActiveWaste)

            try {
                // 개별 insert 대신 batchInsert 사용 (성능 핵심)
                KHNP_RadioActiveWaste.batchInsert(result.rows(), true, false) { row ->
                    this[KHNP_RadioActiveWaste.collectionTime] = row["collectionTime"].toString()
                    this[KHNP_RadioActiveWaste.spmon] = row["spmon"].toString()
                    this[KHNP_RadioActiveWaste.genName] = row["genName"].toString()
                    this[KHNP_RadioActiveWaste.plant] = row["plant"].toString()
                    this[KHNP_RadioActiveWaste.total] = row["total"].toString()
                }

            } catch (e: Exception) {
                LOGGER.error("Batch Insert Error: ${e.localizedMessage}")
            }

        }
    }

    suspend fun <T> retryIO(
        times: Int = 3,
        initialDelay: Long = 1000,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelay
        repeat(times - 1) {
            try {
                return block()
            } catch (e: Exception) {
                LOGGER.warn("요청 실패, $currentDelay ms 후 재시도... (${e.message})")
                delay(currentDelay)
                currentDelay *= 2 // 점진적으로 대기 시간 증가 (Exponential Backoff)
            }
        }
        return block() // 마지막 시도
    }

    suspend fun loadDataCoastalFlooding(path:String, codeList:List<String>, limit:Int): List<DataFrame<*>> = coroutineScope {
        val numOfRows = 300

        // Dispatchers.IO에서 최대 10개의 스레드만 사용하도록 제한된 디스패처 생성
        val limitedDispatcher = Dispatchers.IO.limitedParallelism(limit)

        LOGGER.info("loadDataCoastalFlooding limitedDispatcher: ${limit}")

        // 각 코드를 비동기(async)로 실행하여 List<Deferred<DataFrame>> 생성
        val deferredResults = codeList.map {  it ->
            async(limitedDispatcher ) { // 네트워크 IO를 위한 IO 디스패처 사용
                retryIO(times = 3) {
                    val baseUrl = "${path}&numOfRows=${numOfRows}&sggCd=${it}"
                    var url = "${baseUrl}&pageNo=1"

                    val df_first = try {
                        DataFrame.readJson(url)
                    } catch (e: Exception) {
                        LOGGER.error("첫 페이지 로드 실패: $url", e)
                        throw e
                    }

                    val data = df_first["body"]["items"]["item"][0] as DataFrame<*>
                    val totalCount = (df_first["body"]["totalCount"][0] as Number).toInt()
                    val totalPages = ceil(totalCount.toDouble() / numOfRows).toInt()
                    LOGGER.info("ssgNm:${it}, 시군구:${data[0][0]}/${data[0][1]}, 총 데이터 개수: $totalCount, 전체 페이지 수: $totalPages")
                    val dataFrames = mutableListOf<DataFrame<*>>()
                    dataFrames.add(data)
                    for (page in 2..totalPages) {
                        url = "$baseUrl&pageNo=$page"
                        val df_page = DataFrame.readJson(url)
                        val data = df_page["body"]["items"]["item"][0] as DataFrame<*>
                        dataFrames.add(data)
                    }
                    dataFrames.concat()

                }
            }

        }
        // 모든 비동기 작업이 완료될 때까지 기다려 리스트 반환
        deferredResults.awaitAll() as List<DataFrame<*>>
    }



    suspend fun simplifyGeoJsonWithMapshaper(inputJson: String, percentage: String = "20%"): String {
        // 1. Kotlin Path API를 사용하여 임시 파일 생성
        val inputPath = createTempFile("mapshaper_in_", ".json")
        val outputPath = createTempFile("mapshaper_out_", ".json")

        return runCatching {
            // 2. 파일 쓰기
            inputPath.writeText(inputJson)
            LOGGER.debug("[Mapshaper] Input: ${inputPath.toAbsolutePath()} (${inputPath.toFile().length() / 1024} KB)")

            // 3. CLI 명령어 리스트 작성 (Kotlin 스타일)
            val command = listOf(
                "${ConfigManager.currentConfig.WATER_LOGGED?.node}",
                "${ConfigManager.currentConfig.WATER_LOGGED?.nodeOption}",
                "${ConfigManager.currentConfig.WATER_LOGGED?.mapshaper}",
                "-i", inputPath.toString(),
                "-clean",
                "-simplify", percentage, "visvalingam", "keep-shapes",
                "-clean",
                "-o", outputPath.toString(), "format=geojson"
            )

            // 4. 실행
            val result = command.runCommand()

            if (result.exitCode != 0) {
                throw RuntimeException("Mapshaper failed: ${result.error}")
            }

            // 5. 결과 읽기
            val simplifiedJson = outputPath.readText()
            LOGGER.debug("[Mapshaper] Success. Reduced Length: ${simplifiedJson.length}")

            simplifiedJson
        }.onFailure { e ->
            LOGGER.error("[Mapshaper] Process Error: ${e.message}")
        }.also {
            // 6. finally 대신 임시 파일 삭제
            inputPath.deleteIfExists()
            outputPath.deleteIfExists()
        }.getOrDefault("")
    }


    suspend fun getCoastalFloodingInfo() {

        try{
            val path = "${ConfigManager.currentConfig.WATER_LOGGED?.endPoint}/${ConfigManager.currentConfig.WATER_LOGGED?.subPath}" +
                    "?serviceKey=${ConfigManager.currentConfig.WATER_LOGGED?.apikey}&type=json"

            // 1. 시군구 코드 목록 추출 (짧은 트랜잭션)
            val codeList = transaction(ConfigManager.conn) {
                SggCode.select(SggCode.sgg_code).map { it ->
                    it[SggCode.sgg_code].trim()
                }
            }

            // 2. [핵심 수정] 네트워크로부터 데이터 비동기 수집 (트랜잭션 밖에서 수행)
            // List<List<DataFrame>>을 받아오게 되므로 flatten 후 concat

            val limit = ConfigManager.currentConfig.WATER_LOGGED?.limitedParallelism ?: 1

            loadDataCoastalFlooding(path, codeList, limit).let { rawDataFrames ->

                val result = rawDataFrames.concat()
                // 1. 데이터 수집 및 초기화 단계
                val updateTargets = suspendTransaction( ConfigManager.conn) {


                    // 1. 원본 데이터 테이블 생성
                    SchemaUtils.create(CoastalFloodingGeoInfo)

                    try {
                        // 개별 insert 대신 batchInsert 사용 (성능 핵심)
                        CoastalFloodingGeoInfo.batchInsert(result.rows(), true, false) { row ->
                            this[CoastalFloodingGeoInfo.ctpvNm] = row["ctpvNm"].toString().trim()
                            this[CoastalFloodingGeoInfo.sggNm] = row["sggNm"].toString().trim()
                            this[CoastalFloodingGeoInfo.flodVlCn] = row["flodVlCn"].toString().trim()
                            this[CoastalFloodingGeoInfo.geom] = row["geom"].toString().trim()
                        }

                    } catch (e: Exception) {
                        LOGGER.error("Batch Insert Error: ${e.localizedMessage}")
                    }

                    LOGGER.info("CoastalFloodingGeoInfo 테이블 갱신 완료.")

                    // ---------------------------------------------------------------------------
                    // 3. 요약 테이블(CoastalFloodingGeoTbl) 생성 및 가공 데이터 삽입 시작
                    // ---------------------------------------------------------------------------

                    // 요약 테이블 생성 및 기존 데이터 삭제
                    SchemaUtils.create(CoastalFloodingGeoTbl)
                    CoastalFloodingGeoTbl.deleteAll()
                    LOGGER.info("CoastalFloodingGeoTbl  테이블 삭제 완료.")

                    // T-SQL/SQLite 스타일의 INSERT INTO ... SELECT 쿼리 실행
                    // 가공 로직(CASE WHEN)을 DB 엔진에서 수행하여 성능 극대화
                    val aggregateSql = """
                INSERT INTO CoastalFloodingGeoTbl (grade, flodVlCn, ctpvNm, geom)
                SELECT
                    MAX(grade) AS grade,
                    MAX(flodVlCn) AS flodVlCn,
                    MAX(ctpvNm) AS ctpvNm,
                    geom
                FROM (
                    SELECT
                        CASE
                            WHEN flodVlCn = '0.0-0.5' THEN 'A'
                            WHEN flodVlCn = '0.5-1.0' THEN 'B'
                            WHEN flodVlCn = '1.0-1.5' THEN 'C'
                            WHEN flodVlCn = '1.5-2.0' THEN 'D'
                            WHEN flodVlCn = '2.0-2.5' THEN 'E'
                            WHEN flodVlCn = '2.5-3.0' THEN 'E'
                            WHEN flodVlCn = '2.0-3.0' THEN 'E'
                            ELSE 'F'
                        END AS grade,
                        geom,
                        ctpvNm,
                        flodVlCn
                    FROM CoastalFloodingGeoInfo
                ) AS A
                GROUP BY A.geom
            """.trimIndent()

                    // Exposed의 exec 함수를 통해 네이티브 쿼리 실행
                    exec(aggregateSql)

                    LOGGER.info("CoastalFloodingGeoTbl 요약 테이블 갱신 완료.")

                    SggCode
                        .select(SggCode.sd_name)
                        .withDistinct() // 중복된 grade-sido 쌍 제거
                        .map { it[SggCode.sd_name] }
                }



                coroutineScope {
                    // SQLite와 CPU 부하를 고려하여 동시 실행 작업 수를 3개로 제한
                    val mapShaperLimit = ConfigManager.currentConfig.WATER_LOGGED?.mapshaperLimitedParallelism ?: 3
                    LOGGER.info("loadDataCoastalFlooding mapShaperLimitedDispatcher: ${mapShaperLimit}")
                    val mapShaperLimitedDispatcher = Dispatchers.IO.limitedParallelism(mapShaperLimit)

                    updateTargets.forEach { ctpvNm ->

                        listOf("F", "E", "D", "C", "B", "A").forEach { grade ->

                            launch(mapShaperLimitedDispatcher ) { // 네트워크 IO를 위한 IO 디스패처 사용

                                    val geoJsonObject = suspendTransaction( ConfigManager.conn) {
                                        CoastalFloodingGeoTbl
                                            .select(
                                                CoastalFloodingGeoTbl.grade,
                                                CoastalFloodingGeoTbl.flodVlCn,
                                                CoastalFloodingGeoTbl.ctpvNm,
                                                CoastalFloodingGeoTbl.geom
                                            )
                                            .where {
                                                (CoastalFloodingGeoTbl.grade eq grade) and
                                                        (CoastalFloodingGeoTbl.ctpvNm eq ctpvNm)
                                            }
                                            .map {
                                                CoastalFloodingGeo(
                                                    grade = it[CoastalFloodingGeoTbl.grade],
                                                    flodVlCn = it[CoastalFloodingGeoTbl.flodVlCn],
                                                    ctpvNm = it[CoastalFloodingGeoTbl.ctpvNm],
                                                    geom = it[CoastalFloodingGeoTbl.geom]
                                                )
                                            }.toGeoJsonObject(Pair(ctpvNm, grade))
                                    } // suspendTransaction

                                    if (geoJsonObject.length < 100) return@launch // 데이터 없으면 스킵



                                    // 20%의 정점만 남기고 단순화 (필요에 따라 10%, 5%로 조정 가능)
                                    val simplifyGeoJsonObject = simplifyGeoJsonWithMapshaper(geoJsonObject, "20%")

                                    LOGGER.info("\nOptimization Done for $ctpvNm $grade :[Original size: ${geoJsonObject.length / 1024} KB => Reduced size: ${simplifyGeoJsonObject.length / 1024} KB]")


                                    if (simplifyGeoJsonObject.isEmpty()) return@launch

                                    suspendTransaction( ConfigManager.conn) {

                                        val originalBlob = ExposedBlob(geoJsonObject.toByteArray(Charsets.UTF_8))



                                        SchemaUtils.create(CoastalFloodingGeoJsonObjectTbl)

                                        CoastalFloodingGeoJsonObjectTbl.deleteWhere {
                                            (CoastalFloodingGeoJsonObjectTbl.grade eq grade) and
                                                    (CoastalFloodingGeoJsonObjectTbl.ctpvNm eq ctpvNm)
                                        }

                                        CoastalFloodingGeoJsonObjectTbl.insert {
                                            it[CoastalFloodingGeoJsonObjectTbl.grade] = grade
                                            it[CoastalFloodingGeoJsonObjectTbl.ctpvNm] = ctpvNm
                                            it[CoastalFloodingGeoJsonObjectTbl.geojson] = originalBlob
                                            it[CoastalFloodingGeoJsonObjectTbl.simplegeojson] = simplifyGeoJsonObject

                                        }
                                    } // suspendTransaction

                                    LOGGER.info("Successfully saved $ctpvNm $grade")

                            } // launch

                        } // grade List
                    } // sido List

                }// coroutineScope

            }


        }catch (err: Exception){
            LOGGER.error("[getCoastalFloodingInfo] Process Error: ${err.message}")
        }finally {
            suspendTransaction( ConfigManager.conn) {
                CoastalFloodingGeoInfo.deleteAll()
                LOGGER.info("CoastalFloodingGeoInfo  테이블 삭제 완료.")
            }
        }


    }



    fun loadDataSDoT(path:String, maxPage:Int): List<DataFrame<*>> {
        val rows = mutableListOf<DataFrame<*>>()
        var requestPage = 1
        do{
            val pagePath = "$path&pIndex=$requestPage"
            val jsonData = DataFrame.readJson(pagePath)
            try {
                val instanceDf = (jsonData["Sidoatmospolutnmesure"][0] as DataFrame<*>)["row"][1] as DataFrame<*>
                requestPage += 1
                rows.add(instanceDf)
            } catch(e: Exception) {
                print(e.localizedMessage)
                break
            }
        } while (requestPage <= maxPage )
        return rows
    }
     @OptIn(FormatStringsInDatetimeFormats::class)
     fun getSDoTEnvInfoGyonggi(){

        val now = Clock.System.now()
        val previous1Hour = now
            .minus(1, DateTimeUnit.HOUR)
            .toLocalDateTime(TimeZone.of("Asia/Seoul"))
            .format(LocalDateTime.Format{byUnicodePattern("yyyy-MM-dd HH")}) + ":00"

         val url = "${ConfigManager.currentConfig.SDOT_Gyonggi?.endPoint}" +
                   "/${ConfigManager.currentConfig.SDOT_Gyonggi?.subPath}" +
                 "?KEY=${ConfigManager.currentConfig.SDOT_Gyonggi?.apikey}" +
                 "&Type=${ConfigManager.currentConfig.SDOT_Gyonggi?.type}" +
                 "&MESURE_DAY_TM=${previous1Hour.encodeURLParameter()}"


        LOGGER.info( "${::getSDoTEnvInfoGyonggi.name} [MESURE_DAY_TM:${previous1Hour}, Url:${url}]")

        try {

            val dfResult = loadDataSDoT(url, 2).concat()

            val result = dfResult.rename(
                "SUA_GAS_DNST_VL" to "SO2",
                "COMNXD_DNST_VL" to "CO",
                "NO2_DNST_VL" to "NO2",
                "OZONE_DNST_VL" to "O3",
                "FINEDUST_PM10_DNST_VL" to "PM10",
                "FINEDUST_PM2_5_DNST_VL" to "PM2.5"
            )

            LOGGER.debug("\n"+ result.schema().toString())
            LOGGER.debug("\n"+ result.head(5).toString())

            transaction(ConfigManager.conn) {
                SchemaUtils.create(SDoT_EnvInfo_Gyonggi)

                try {
                    // 개별 insert 대신 batchInsert 사용 (성능 핵심)
                    SDoT_EnvInfo_Gyonggi.batchInsert(result.rows(), true, false) { row ->
                        this[SDoT_EnvInfo_Gyonggi.obs] = row["MESURSTN_NM"].toString()
                        this[SDoT_EnvInfo_Gyonggi.region] = row["MESRNW_NM"].toString()
                        this[SDoT_EnvInfo_Gyonggi.sensing_time] = row["MESURE_DAY_TM"].toString()
                        this[SDoT_EnvInfo_Gyonggi.so2] = row["SO2"].toString()
                        this[SDoT_EnvInfo_Gyonggi.co] = row["CO"].toString()
                        this[SDoT_EnvInfo_Gyonggi.no2] = row["NO2"].toString()
                        this[SDoT_EnvInfo_Gyonggi.o3] = row["O3"].toString()
                        this[SDoT_EnvInfo_Gyonggi.pm10] = row["PM10"].toString()
                        this[SDoT_EnvInfo_Gyonggi.pm25] = row["PM2.5"].toString()
                    }

                } catch (e: Exception) {
                    LOGGER.error("Batch Insert Error: ${e.localizedMessage}")
                }
            }

        }catch (e: Exception){
            LOGGER.error("${::getSDoTEnvInfoGyonggi.name} Error: ${e.localizedMessage}")
        }

    }

    suspend fun getSDoTEnvInfo(){

        //http://openapi.seoul.go.kr:8088/6e4a49477579656f393876794a6a63/json/sDoTEnv/1001/1200/
        // 1. 현재 S-DoT 장비의 unique count 값이 1170.
        // 2. 최초 1000 건을 수집하되 SENSING_TIME 이 unique 하면 200 건을 더 수집.
        // 3. 수집된 데이터중 SENSING_TIME 이 MAX(SENSING_TIME) 인 값만 filtering.

        val url = "${ConfigManager.currentConfig.SDOT_API?.endPoint}/${ConfigManager.currentConfig.SDOT_API?.apikey}/" +
                "${ConfigManager.currentConfig.SDOT_API?.type}/" +
                "${ConfigManager.currentConfig.SDOT_API?.subPath}/"
        try {

            var uniqueSensingTimeCount = 0
            var receiveData: MutableList<SDoTEnvInformation> = mutableListOf()

            CollectionServerRestApi.callSDoT_EnvInfo_json(url+"1/1000/").let{
                val response = CollectionServerRestApi.commonJson.decodeFromString<SDoTEnvResponse>(it)
                LOGGER.info("[receive code[${response.sDoTEnv.RESULT.CODE}], receive message[${response.sDoTEnv.RESULT.MESSAGE}]]")
                receiveData = response.sDoTEnv.row as MutableList<SDoTEnvInformation>
                uniqueSensingTimeCount = receiveData.map { it.SENSING_TIME }.distinct().size
            }

            if(uniqueSensingTimeCount == 1){
                CollectionServerRestApi.callSDoT_EnvInfo_json(url+"1001/1200/").let{
                    val response = CollectionServerRestApi.commonJson.decodeFromString<SDoTEnvResponse>(it)
                    LOGGER.info("[receive code[${response.sDoTEnv.RESULT.CODE}], receive message[${response.sDoTEnv.RESULT.MESSAGE}]]")
                    receiveData.addAll(response.sDoTEnv.row as MutableList<SDoTEnvInformation>)
                }
            }

            if(receiveData.isNotEmpty()){
                val maxSensingTime = receiveData.maxOfOrNull { it.SENSING_TIME }
                val finalData = receiveData.filter { it.SENSING_TIME == maxSensingTime}
                transaction(ConfigManager.conn) {
                    SchemaUtils.create(SDoT_EnvInfo)

                    try {
                        SDoT_EnvInfo.batchReplace(finalData) { item ->
                            this[SDoT_EnvInfo.modelname] = item.MODELNAME
                            this[SDoT_EnvInfo.serial] = item.SERIAL
                            this[SDoT_EnvInfo.sensing_time] = item.SENSING_TIME
                            this[SDoT_EnvInfo.region] = item.REGION
                            this[SDoT_EnvInfo.autonomous_district] = item.AUTONOMOUS_DISTRICT
                            this[SDoT_EnvInfo.administrative_district] = item.ADMINISTRATIVE_DISTRICT
                            this[SDoT_EnvInfo.max_temp] = item.MAX_TEMP
                            this[SDoT_EnvInfo.avg_temp] = item.AVG_TEMP
                            this[SDoT_EnvInfo.min_temp] = item.MIN_TEMP
                            this[SDoT_EnvInfo.max_humi] = item.MAX_HUMI
                            this[SDoT_EnvInfo.avg_humi] = item.AVG_HUMI
                            this[SDoT_EnvInfo.min_humi] = item.MIN_HUMI
                            this[SDoT_EnvInfo.max_wind_speed] = item.MAX_WIND_SPEED
                            this[SDoT_EnvInfo.avg_wind_speed] = item.AVG_WIND_SPEED
                            this[SDoT_EnvInfo.min_wind_speed] = item.MIN_WIND_SPEED
                            this[SDoT_EnvInfo.max_wind_dire] = item.MAX_WIND_DIRE
                            this[SDoT_EnvInfo.avg_wind_dire] = item.AVG_WIND_DIRE
                            this[SDoT_EnvInfo.min_wind_dire] = item.MIN_WIND_DIRE
                            this[SDoT_EnvInfo.max_inte_illu] = item.MAX_INTE_ILLU
                            this[SDoT_EnvInfo.avg_inte_illu] = item.AVG_INTE_ILLU
                            this[SDoT_EnvInfo.min_inte_illu] = item.MIN_INTE_ILLU
                            this[SDoT_EnvInfo.max_ultra_rays] = item.MAX_ULTRA_RAYS
                            this[SDoT_EnvInfo.avg_ultra_rays] = item.AVG_ULTRA_RAYS
                            this[SDoT_EnvInfo.min_ultra_rays] = item.MIN_ULTRA_RAYS
                            this[SDoT_EnvInfo.max_noise] = item.MAX_NOISE
                            this[SDoT_EnvInfo.avg_noise] = item.AVG_NOISE
                            this[SDoT_EnvInfo.min_noise] = item.MIN_NOISE
                            this[SDoT_EnvInfo.max_vibr_x] = item.MAX_VIBR_X
                            this[SDoT_EnvInfo.avg_vibr_x] = item.AVG_VIBR_X
                            this[SDoT_EnvInfo.min_vibr_x] = item.MIN_VIBR_X
                            this[SDoT_EnvInfo.max_vibr_y] = item.MAX_VIBR_Y
                            this[SDoT_EnvInfo.avg_vibr_y] = item.AVG_VIBR_Y
                            this[SDoT_EnvInfo.min_vibr_y] = item.MIN_VIBR_Y
                            this[SDoT_EnvInfo.max_vibr_z] = item.MAX_VIBR_Z
                            this[SDoT_EnvInfo.avg_vibr_z] = item.AVG_VIBR_Z
                            this[SDoT_EnvInfo.min_vibr_z] = item.MIN_VIBR_Z
                            this[SDoT_EnvInfo.max_effe_temp] = item.MAX_EFFE_TEMP
                            this[SDoT_EnvInfo.avg_effe_temp] = item.AVG_EFFE_TEMP
                            this[SDoT_EnvInfo.min_effe_temp] = item.MIN_EFFE_TEMP
                            this[SDoT_EnvInfo.max_no2] = item.MAX_NO2
                            this[SDoT_EnvInfo.avg_no2] = item.AVG_NO2
                            this[SDoT_EnvInfo.min_no2] = item.MIN_NO2
                            this[SDoT_EnvInfo.max_co] = item.MAX_CO
                            this[SDoT_EnvInfo.avg_co] = item.AVG_CO
                            this[SDoT_EnvInfo.min_co] = item.MIN_CO
                            this[SDoT_EnvInfo.max_so2] = item.MAX_SO2
                            this[SDoT_EnvInfo.avg_so2] = item.AVG_SO2
                            this[SDoT_EnvInfo.min_so2] = item.MIN_SO2
                            this[SDoT_EnvInfo.max_nh3] = item.MAX_NH3
                            this[SDoT_EnvInfo.avg_nh3] = item.AVG_NH3
                            this[SDoT_EnvInfo.min_nh3] = item.MIN_NH3
                            this[SDoT_EnvInfo.max_h2s] = item.MAX_H2S
                            this[SDoT_EnvInfo.avg_h2s] = item.AVG_H2S
                            this[SDoT_EnvInfo.min_h2s] = item.MIN_H2S
                            this[SDoT_EnvInfo.max_o3] = item.MAX_O3
                            this[SDoT_EnvInfo.avg_o3] = item.AVG_O3
                            this[SDoT_EnvInfo.min_o3] = item.MIN_O3
                            this[SDoT_EnvInfo.date] = item.DATE
                            this[SDoT_EnvInfo.data_no] = item.DATA_NO
                        }
                    } catch (e: Exception) {
                        LOGGER.error("Batch Replace Error: ${e.localizedMessage}")
                    }




                }

            }

        } catch (e: Exception){
            val msg = e.localizedMessage
        }
    }




    suspend fun loadDataTidalCurrent(path:String, interval:Int, predictedTotalMinute:Int, limit:Int, loopDelay:Long): Pair<String,List<KhonTidalCurrentInfo>> = coroutineScope {

        val windowSize = predictedTotalMinute / interval
        val startTime = Clock.System.now() // 시작 시점 고정
        // Dispatchers.IO에서 최대 10개의 스레드만 사용하도록 제한된 디스패처 생성
        val limitedDispatcher = Dispatchers.IO.limitedParallelism(limit)

        LOGGER.info("loadDataTidalCurrent limitedDispatcher: ${limit}, delay:${loopDelay}")

        var sch_time = ""

        val deferredResults = (0 until windowSize).map{ i ->

            delay(loopDelay)

            async(limitedDispatcher ) { // 네트워크 IO를 위한 IO 디스패처 사용

                retryIO(times = 3) {

                    val targetTime = startTime.plus(i * interval, DateTimeUnit.MINUTE)

                    var localDateTime = targetTime.toLocalDateTime(TimeZone.of("Asia/Seoul"))
                    localDateTime = LocalDateTime(
                        localDateTime.year,
                        localDateTime.month,
                        localDateTime.day,
                        localDateTime.hour,
                        (localDateTime.minute / interval) * interval
                    )

                    val datetime = localDateTime.format(
                        LocalDateTime.Format { byUnicodePattern("yyyyMMddHHmm") }
                    )

                    val date = datetime.substring(0, 8)
                    val hour = datetime.substring(8, 10)
                    val minute = datetime.substring(10, 12)

                    val url = "${path}&Date=${date}&Hour=${hour}&Minute=${minute}"

                    try {

                        CollectionServerRestApi.callKhoaAPI_json(url).let {
                            val response = CollectionServerRestApi.commonJson.decodeFromString<KhonTidalCurrentInfoResponse>(it)
                            LOGGER.debug("${::getKhoaTidalCurrent.name} [receive count[${response.result.data.size}]]")
                            sch_time = response.result.meta.sch_time
                            response.result.data
                        }

                    } catch (e: Exception) {
                        LOGGER.error("첫 페이지 로드 실패: $url", e)
                        throw e
                    }

                }
            }

        }

        // List<List<KhonTidalCurrentInfo>> 가 반환됨
        val result = deferredResults.awaitAll().flatten()
        Pair(sch_time,  result)

    }


    suspend fun getKhoaTidalCurrent(){

        val interval = ConfigManager.currentConfig.KHOA_TIDALCURRENT_API?.interval ?: 5
        val predictedTotalMinute = ConfigManager.currentConfig.KHOA_TIDALCURRENT_API?.predictedTotalMinute ?: 60
        val url = "${ConfigManager.currentConfig.KHOA_TIDALCURRENT_API?.endPoint}/${ConfigManager.currentConfig.KHOA_TIDALCURRENT_API?.subPath}?ServiceKey=${ConfigManager.currentConfig.KHOA_TIDALCURRENT_API?.apikey}&ResultType=${ConfigManager.currentConfig.KHOA_TIDALCURRENT_API?.type}${ConfigManager.currentConfig.KHOA_TIDALCURRENT_API?.boundBox}"
        val limit = ConfigManager.currentConfig.KHOA_TIDALCURRENT_API?.limitedParallelism ?: 1
        val loopDelay = ConfigManager.currentConfig.KHOA_TIDALCURRENT_API?.loopdelay?.toLong() ?: 500L

        loadDataTidalCurrent(path=url, interval=interval, predictedTotalMinute=predictedTotalMinute, limit=limit, loopDelay = loopDelay).let{ (sch_time, result) ->

            LOGGER.info("${::getKhoaTidalCurrent.name} [schTime[${sch_time}], total count[${result.size}}]]")

            suspendTransaction( ConfigManager.conn) {

                SchemaUtils.create(TidalCurrentInfoKHOA)

                try {
                    TidalCurrentInfoKHOA.batchReplace(result) { item ->
                        this[TidalCurrentInfoKHOA.sch_time] = sch_time
                        this[TidalCurrentInfoKHOA.pre_lon] = item.pre_lon.toDouble()
                        this[TidalCurrentInfoKHOA.pre_lat] = item.pre_lat.toDouble()
                        this[TidalCurrentInfoKHOA.current_dir] = item.current_dir.toDouble()
                        this[TidalCurrentInfoKHOA.current_speed] = item.current_speed.toDouble()
                    }
                } catch (e: Exception) {
                    LOGGER.error("Batch Replace Error: ${e.localizedMessage}")
                }

                LOGGER.info("TidalCurrentInfoKHOA 테이블 갱신 완료.")

            }
        }

    }


    suspend fun loadKhoaObservation(codeList:List<String>, url:String, limit:Int):List<Pair<String,List<KhoaObservation>>>  = coroutineScope {




        val limitedDispatcher = Dispatchers.IO.limitedParallelism(limit)


        val deferredResults = codeList.map { obsCode ->

            val pageUrl = "${url}&pageNo=1&obsCode=${obsCode}"

            async(limitedDispatcher ) { // 네트워크 IO를 위한 IO 디스패처 사용
                retryIO(times = 3) {
                    try {
                        CollectionServerRestApi.callKhoaAPI_json(pageUrl).let {
                            val recvData = CollectionServerRestApi.commonJson.decodeFromString<KhoaObservationResponse>(it)
                            if(recvData.header.resultCode.equals("00")) {
                                LOGGER.info("${::getKhoaObservation.name} [receive count[${recvData.body.totalCount}]]")
                            }else {
                                LOGGER.error( "${::getKhoaObservation.name} [receive message[${recvData.header.resultMsg}]]")
                            }
                            Pair(obsCode , recvData.body.items.item)
                        }
                    }catch (e: Exception){
                        LOGGER.error("첫 페이지 로드 실패: $pageUrl", e)
                        throw e
                    }
                }
            }
        }

        deferredResults.awaitAll()

    }

    suspend fun getKhoaObservationNew()  {

        val reqDate =
            kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.of("Asia/Seoul"))
                .format(LocalDateTime.Format { byUnicodePattern("yyyyMMdd") })


        val url = "${ConfigManager.currentConfig.KHOA_API?.endPoint}/${ConfigManager.currentConfig.KHOA_API?.subPath}" +
                "?serviceKey=${ConfigManager.currentConfig.KHOA_API?.apikey}" +
                "&type=${ConfigManager.currentConfig.KHOA_API?.type}" +
                "&reqDate=${reqDate}" +
                "&min=${ConfigManager.currentConfig.KHOA_API?.min}" +
                "&numOfRows=${ConfigManager.currentConfig.KHOA_API?.numOfRows}"


        val codeList = transaction(ConfigManager.conn) {
            ObservatoryKHOA.select(ObservatoryKHOA.obsCode).where {
                ObservatoryKHOA.obsCode like "HB%"
            }.map { resultRow ->
                resultRow[ObservatoryKHOA.obsCode]
            }
        }


        val limit_RestCall = ConfigManager.currentConfig.KHOA_API?.limitedParallelismREST ?: 1
        val limit_DB = ConfigManager.currentConfig.KHOA_API?.limitedParallelismDB ?: 1

        LOGGER.info("${::getKhoaObservation.name}  limitedParallelismREST[${limit_RestCall}], limit_DB[${limit_DB}]}")

        loadKhoaObservation(codeList, url, limit_RestCall).let { result ->

            LOGGER.info("${::getKhoaObservation.name}  total count[${result.size}}")

            coroutineScope {

                val limitedDispatcher = Dispatchers.IO.limitedParallelism(limit_DB)

                result.forEach { (code, dataList) ->

                    launch(limitedDispatcher) {

                        suspendTransaction(ConfigManager.conn) {

                            SchemaUtils.create(ObservationKHOA)
                            SchemaUtils.create(ObservatoryKHOA)

                            try {
                                ObservationKHOA.batchInsert(dataList, true, false) { row ->

                                    this[ObservationKHOA.obsCode] = code
                                    this[ObservationKHOA.obsrvnDt] = row.obsrvnDt
                                    this[ObservationKHOA.wndrct] = row.wndrct?.toString()
                                    this[ObservationKHOA.wspd] = row.wspd?.toString()
                                    this[ObservationKHOA.maxMmntWspd] =
                                        row.maxMmntWspd?.toString()
                                    this[ObservationKHOA.artmp] = row.artmp?.toString()
                                    this[ObservationKHOA.atmpr] = row.atmpr?.toString()
                                    this[ObservationKHOA.wvhgt] = row.wvhgt?.toString()
                                    this[ObservationKHOA.wvpd] = row.wvpd?.toString()
                                    this[ObservationKHOA.crdir] = row.crdir?.toString()
                                    this[ObservationKHOA.crsp] = row.crsp?.toString()
                                    this[ObservationKHOA.wtem] = row.wtem?.toString()
                                    this[ObservationKHOA.slnty] = row.slnty?.toString()
                                }
                            } catch (e: Exception) {
                                LOGGER.error("Batch Replace Error: ${e.localizedMessage}")
                            }

                            LOGGER.info("ObservationKHOA 테이블 갱신 완료.")

                        }
                    }
                }
            }
        }
    }


     @OptIn(FormatStringsInDatetimeFormats::class)
     suspend fun getKhoaObservation()  {

             val reqDate =
                 kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.of("Asia/Seoul"))
                     .format(LocalDateTime.Format { byUnicodePattern("yyyyMMdd") })

             val url = "${ConfigManager.currentConfig.KHOA_API?.endPoint}/${ConfigManager.currentConfig.KHOA_API?.subPath}" +
                     "?serviceKey=${ConfigManager.currentConfig.KHOA_API?.apikey}" +
                     "&type=${ConfigManager.currentConfig.KHOA_API?.type}" +
                     "&reqDate=${reqDate}" +
                     "&min=${ConfigManager.currentConfig.KHOA_API?.min}" +
                     "&numOfRows=${ConfigManager.currentConfig.KHOA_API?.numOfRows}"


             val result = transaction(ConfigManager.conn) {
                 ObservatoryKHOA.select(ObservatoryKHOA.obsCode).where {
                     ObservatoryKHOA.obsCode like "HB%"
                 }.map { resultRow ->
                     resultRow[ObservatoryKHOA.obsCode]
                 }
             }

             result.forEach { obsCode ->

                 var requestPage = 1

              //   do{
                    val pageUrl = "${url}&pageNo=${requestPage}&obsCode=${obsCode}"

                     try {
                            CollectionServerRestApi.callKhoaAPI_json(pageUrl).let {
                             val recvData = CollectionServerRestApi.commonJson.decodeFromString<KhoaObservationResponse>(it)

                             if(recvData.header.resultCode.equals("00")) {
                             //    requestPage += 1

                                 LOGGER.info("${::getKhoaObservation.name} [receive count[${recvData.body.totalCount}]]")

                                 transaction(ConfigManager.conn) {
                                     SchemaUtils.create(ObservationKHOA)
                                     SchemaUtils.create(ObservatoryKHOA)


                                     try {
                                         // 개별 insert 대신 batchInsert 사용 (성능 핵심)
                                         ObservationKHOA.batchInsert(recvData.body.items.item, true, false) { row ->

                                             this[ObservationKHOA.obsCode] = obsCode
                                             this[ObservationKHOA.obsrvnDt] = row.obsrvnDt
                                             this[ObservationKHOA.wndrct] = row.wndrct?.toString()
                                             this[ObservationKHOA.wspd] = row.wspd?.toString()
                                             this[ObservationKHOA.maxMmntWspd] =
                                                 row.maxMmntWspd?.toString()
                                             this[ObservationKHOA.artmp] = row.artmp?.toString()
                                             this[ObservationKHOA.atmpr] = row.atmpr?.toString()
                                             this[ObservationKHOA.wvhgt] = row.wvhgt?.toString()
                                             this[ObservationKHOA.wvpd] = row.wvpd?.toString()
                                             this[ObservationKHOA.crdir] = row.crdir?.toString()
                                             this[ObservationKHOA.crsp] = row.crsp?.toString()
                                             this[ObservationKHOA.wtem] = row.wtem?.toString()
                                             this[ObservationKHOA.slnty] = row.slnty?.toString()
                                         }

                                     } catch (e: Exception) {
                                         LOGGER.error("Batch Insert Error: ${e.localizedMessage}")
                                     }


                                     if (recvData.body.totalCount > 0) {
                                         try {
                                             ObservatoryKHOA.update({ ObservatoryKHOA.obsCode eq obsCode }) { it ->
                                                 it[ObservatoryKHOA.obsvtrNm] =
                                                     recvData.body.items.item[0].obsvtrNm
                                                 it[ObservatoryKHOA.longitude] =
                                                     recvData.body.items.item[0].lot
                                                 it[ObservatoryKHOA.latitude] =
                                                     recvData.body.items.item[0].lat
                                             }
                                         } catch (e: Exception) {
                                             e.localizedMessage?.let { msg ->
                                                 LOGGER.debug(msg)
                                             }
                                         }
                                     }


                                 }
                             } else if(recvData.header.resultCode.equals("03")){
                             //    break
                             }else{
                                 LOGGER.error( "${::getKhoaObservation.name} [receive message[${recvData.header.resultMsg}]]")
                             //    break
                             }

                         }

                     }catch(e: Exception) {
                         e.localizedMessage?.let { msg ->
                             LOGGER.error( "${::getKhoaObservation.name} [${msg}]")
                      //       break
                         }
                     }

               //  } while (requestPage < 100 )

             }
    }




    @Suppress("DefaultLocale")
    suspend fun  getRealTimeOceanWaterQuality(){
        try {
            CollectionServerRestApi.callMofAPI_xml().let { response ->
                if(response.status.value == 200){
                    XML.toJSONObject(response.bodyAsText()).let { jsonData ->
                 //   response.bodyAsText().let{ jsonData ->
                        // val df = DataFrame.readJson(jsonData.byteInputStream())
                     //  val result = df.get("body").get("items").get("item")[0] as DataFrame<*>
                        val df = DataFrame.readJson( jsonData.toString().byteInputStream())
                        val result = df.get("response").get("body").get("items").get("item")[0] as DataFrame<*>

                        LOGGER.info( "${::getRealTimeOceanWaterQuality.name} [receive count[${result.count()}]]")

                        transaction (ConfigManager.conn){
                            SchemaUtils.create( OWQInformationTable)

                            try {
                                // 개별 insert 대신 batchInsert 사용 (성능 핵심)
                                OWQInformationTable.batchInsert(result.rows(), true, false) { row ->
                                    this[OWQInformationTable.rtmWqWtchDtlDt] = row["rtmWqWtchDtlDt"].toString().substringBefore('.')
                                    this[OWQInformationTable.rtmWqWtchStaCd] = row["rtmWqWtchStaCd"].toString()
                                    this[OWQInformationTable.rtmWtchWtem] =  String.format("%.3f", row["rtmWtchWtem"].toString().toDouble())
                                    this[OWQInformationTable.rtmWqCndctv] = String.format("%.3f", row["rtmWqCndctv"].toString().toFloat())
                                    this[OWQInformationTable.ph] = String.format("%.2f", row["ph"].toString().toFloat())
                                    this[OWQInformationTable.rtmWqDoxn] = String.format("%.3f", row["rtmWqDoxn"].toString().toDouble())
                                    this[OWQInformationTable.rtmWqTu] = row["rtmWqTu"].toString()
                                    this[OWQInformationTable.rtmWqBgalgsQy] = row["rtmWqBgalgsQy"].toString()
                                    this[OWQInformationTable.rtmWqChpla] = String.format("%.3f", row["rtmWqChpla"].toString().toDouble())
                                    this[OWQInformationTable.rtmWqSlnty] = String.format("%.3f", row["rtmWqSlnty"].toString().toFloat())
                                }

                            } catch (e: Exception) {
                                LOGGER.error("Batch Insert Error: ${e.localizedMessage}")
                            }

                        }
                    }
                }else{
                    LOGGER.error( "${::getRealTimeOceanWaterQuality.name} [${response.status.description}]")
                }
            }
        }catch(e: Exception) {
            e.localizedMessage?.let { msg ->
                LOGGER.error( "${::getRealTimeObservation.name} [${msg}]")
            }
        }

    }

    @Suppress("DefaultLocale")
    suspend fun getRealTimeObservation(){
        try{

            CollectionServerRestApi.callNifsAPI_json("list").let {
                val recvData = CollectionServerRestApi.commonJson.decodeFromString<ObservationResponse>(it)
                if(recvData.header.resultCode.equals("00")){
                    LOGGER.info( "${::getRealTimeObservation.name} [receive count[${recvData.body.item.size}]]")
                    transaction (ConfigManager.conn){

                        SchemaUtils.create( ObservationTable)

                        try {
                            // 개별 insert 대신 batchInsert 사용 (성능 핵심)
                            ObservationTable.batchInsert(recvData.body.item, true, false) { row ->
                                this[ObservationTable.sta_cde] = row.sta_cde
                                this[ObservationTable.sta_nam_kor] = row.sta_nam_kor
                                this[ObservationTable.obs_dat] = row.obs_dat
                                this[ObservationTable.obs_tim] = row.obs_tim
                                this[ObservationTable.obs_datetime] = "${row.obs_dat} ${row.obs_tim}"
                                this[ObservationTable.repair_gbn] = row.repair_gbn
                                this[ObservationTable.obs_lay] = row.obs_lay
                                this[ObservationTable.wtr_tmp] = row.wtr_tmp.toString()
                            }

                        } catch (e: Exception) {
                            LOGGER.error("Batch Insert Error: ${e.localizedMessage}")
                        }
                    }
                }else{
                    LOGGER.error( "${::getRealTimeObservation.name} [receive message[${recvData.header.resultMsg}]]")
                }
            }
        } catch(e: Exception) {
            e.localizedMessage?.let { msg ->
                LOGGER.error( "${::getRealTimeObservation.name} [${msg}]")
            }
        }
    }

    @Suppress("DefaultLocale")
    suspend fun getRealTimeObservatory(){
        try{
            CollectionServerRestApi.callNifsAPI_json("code").let {
                val recvData = CollectionServerRestApi.commonJson.decodeFromString<ObservatoryResponse>(it)
                if(recvData.header.resultCode.equals("00")) {

                    LOGGER.info( "${::getRealTimeObservatory.name} [receive count[${recvData.body.item.size}]]")

                    transaction (ConfigManager.conn){
                        SchemaUtils.drop( ObservatoryTable)
                        SchemaUtils.create( ObservatoryTable)


                        try {
                            // 개별 insert 대신 batchInsert 사용 (성능 핵심)
                            ObservatoryTable.batchInsert(recvData.body.item, true, false) { row ->
                                this[ObservatoryTable.sta_cde] = row.sta_cde
                                this[ObservatoryTable.sta_nam_kor] = row.sta_nam_kor
                                this[ObservatoryTable.bld_dat] = row.bld_dat
                                this[ObservatoryTable.end_dat] = row.end_dat
                                this[ObservatoryTable.gru_nam] = row.gru_nam
                                this[ObservatoryTable.lon] = row.lon
                                this[ObservatoryTable.lat] = row.lat
                                this[ObservatoryTable.sur_tmp_yn] = row.sur_tmp_yn
                                this[ObservatoryTable.mid_tmp_yn] = row.mid_tmp_yn
                                this[ObservatoryTable.bot_tmp_yn] = row.bot_tmp_yn
                                this[ObservatoryTable.sur_dep] = row.sur_dep
                                this[ObservatoryTable.mid_dep] = row.mid_dep
                                this[ObservatoryTable.bot_dep] = row.bot_dep
                                this[ObservatoryTable.sta_des] = row.sta_des
                            }

                        } catch (e: Exception) {
                            LOGGER.error("Batch Insert Error: ${e.localizedMessage}")
                        }

                    }

                }else{
                    LOGGER.error( "${::getRealTimeObservatory.name} [receive message[${recvData.header.resultMsg}]]")
                }
            }
        } catch (e: Exception){
            e.localizedMessage?.let { msg ->
                LOGGER.error( "${::getRealTimeObservatory.name} [${msg}]")
            }
        }
    }


}