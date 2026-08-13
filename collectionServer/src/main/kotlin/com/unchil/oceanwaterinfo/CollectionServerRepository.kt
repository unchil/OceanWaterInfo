package com.unchil.oceanwaterinfo


import com.unchil.oceanwaterinfo.CollectionServerConfig.Companion.configData
import io.ktor.client.statement.bodyAsText
import io.ktor.http.encodeURLParameter
import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
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
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.upsert
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.add
import org.jetbrains.kotlinx.dataframe.api.concat
import org.jetbrains.kotlinx.dataframe.api.count
import org.jetbrains.kotlinx.dataframe.api.flatten
import org.jetbrains.kotlinx.dataframe.api.forEach
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
import java.nio.charset.StandardCharsets
import kotlin.io.path.deleteIfExists
import kotlin.math.ceil
import kotlin.time.Clock
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText
import kotlin.io.path.readText
class CollectionServerRepository {
    internal val LOGGER = KtorSimpleLogger( CollectionServerRepository::class.java.name )

    init {
        transaction(CollectionServerConfig.conn) {
            addLogger(StdOutSqlLogger)
        }
    }


     suspend fun getKHNP_PlantStates() {

        val url_PlantStates = "${configData.KHNP?.endPoint}/${configData.KHNP?.subPath?.NuclearPlantStates}?serviceKey=${configData.KHNP?.serviceKey}"

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
              val msg = e.localizedMessage

            }
        }

         transaction(CollectionServerConfig.conn) {
             SchemaUtils.create(KHNP_PlantInfo)
             plantInfo.forEach { item  ->
                 try{
                     KHNP_PlantInfo.insertIgnore { it ->
                         it[siteCd] = item.siteCd
                         it[siteNm] = item.siteNm
                         it[siteMm] = item.siteMm
                     }
                 }catch (e:Exception){
                     LOGGER.error("Exception : [" + e.localizedMessage + "]")
                 }
             }

             SchemaUtils.create(KHNP_PlantOperationInfo)
             unitInfoList.forEach { item  ->
                 try{
                     KHNP_PlantOperationInfo.insertIgnore { it ->
                         it[collectionTime] = item.collectionTime
                         it[siteCd] = item.siteCd
                         it[genName] = item.genName
                         it[unitCd] = item.unitCd
                         it[unitDttm] = item.unitDttm
                         it[unitNm] = item.unitNm
                         it[unitSt] = item.unitSt

                     }
                 }catch (e:Exception){
                     LOGGER.error("Exception : [" + e.localizedMessage + "]")
                 }
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
        val url = "${configData.KHNP?.endPoint}/${configData.KHNP?.subPath?.ThermalWasteWater}?serviceKey=${configData.KHNP?.serviceKey}"
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

        LOGGER.info("\n ${::getKHNP_ThermalWasteWater.name}  Schema[${result.schema()}]")
        LOGGER.info("\n ${::getKHNP_ThermalWasteWater.name}  Count:[${result.count()}]")

        transaction(CollectionServerConfig.conn) {
            SchemaUtils.create(KHNP_ThermalWasteWater)

            result.forEach { item  ->
                try{
                    KHNP_ThermalWasteWater.insertIgnore { it ->
                        it[time] = item["collectionTime"].toString()
                        it[genName] = item["genName"].toString()
                        it[rm001] = item["rm001"].toString()
                        it[rm001_time] = item["rm001_time"].toString()
                        it[rm002] = item["rm002"].toString()
                        it[rm002_time] = item["rm002_time"].toString()
                        it[rm005] = item["rm005"].toString()
                        it[rm005_time] = item["rm005_time"].toString()
                        it[rm006] = item["rm006"].toString()
                        it[rm006_time] = item["rm006_time"].toString()
                    }
                }catch (e:Exception){
                    LOGGER.error("Exception : [" + e.localizedMessage + "]")

                }
            }
        }

    }



    fun getKHNP_WasteWater(){

        val url = "${configData.KHNP?.endPoint}/${configData.KHNP?.subPath?.WasteWater}?serviceKey=${configData.KHNP?.serviceKey}"
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


        LOGGER.info("\n ${::getKHNP_WasteWater.name}  Schema[${result.schema()}]")
        LOGGER.info("\n ${::getKHNP_WasteWater.name}  Count:[${result.count()}]")

        transaction(CollectionServerConfig.conn) {
            SchemaUtils.create(KHNP_WasteWater)

            result.forEach { item  ->
                try{
                    KHNP_WasteWater.insertIgnore { it ->
                        it[time] = item["collectionTime"].toString()
                        it[genName] = item["genName"].toString()
                        it[tm001] = item["tm001"].toString()
                        it[tm001_time] = item["tm001_time"].toString()
                        it[tm002] = item["tm002"].toString()
                        it[tm002_time] = item["tm002_time"].toString()
                    }
                }catch (e:Exception){
                    LOGGER.error("Exception : [" + e.localizedMessage + "]")

                }
            }
        }

    }


    fun getKHNP_RadioRate(){
        val url = "${configData.KHNP?.endPoint}/${configData.KHNP?.subPath?.RadioRate}?serviceKey=${configData.KHNP?.serviceKey}"
        val result = loadKHNP_Service(url,  listOf("WS", "KR", "YK", "SU", "UJ"))


        LOGGER.info("\n ${::getKHNP_RadioRate.name}  Schema[${result.schema()}]")
        LOGGER.info("\n ${::getKHNP_RadioRate.name}  Count:[${result.count()}]")

        transaction(CollectionServerConfig.conn) {
            SchemaUtils.create(KHNP_RadioRate)

            result.forEach { item  ->
                try{
                    KHNP_RadioRate.insertIgnore { it ->
                        it[collectionTime] = item["collectionTime"].toString()
                        it[time] = item["time"].toString()
                        it[genName] = item["genName"].toString()
                        it[name] = item["name"].toString()
                        it[expl] = item["expl"].toString()
                        it[value] = item["value"].toString()
                    }
                }catch (e:Exception){
                    LOGGER.error("Exception : [" + e.localizedMessage + "]")

                }
            }
        }
    }

    fun getKHNP_RadioActiveWaste(){
        val url = "${configData.KHNP?.endPoint}/${configData.KHNP?.subPath?.RadioActiveWaste}?serviceKey=${configData.KHNP?.serviceKey}"
        val result = loadKHNP_Service(url,  listOf("2100", "2200", "2300", "2400", "2800") )


        LOGGER.info("\n ${::getKHNP_RadioActiveWaste.name}  Schema[${result.schema()}]")
        LOGGER.info("\n ${::getKHNP_RadioActiveWaste.name}  Count:[${result.count()}]")

        transaction(CollectionServerConfig.conn) {
            SchemaUtils.create(KHNP_RadioActiveWaste)

            result.forEach { item  ->
                try{
                    KHNP_RadioActiveWaste.insertIgnore { it ->
                        it[collectionTime] = item["collectionTime"].toString()
                        it[spmon] = item["spmon"].toString()
                        it[genName] = item["genName"].toString()
                        it[plant] = item["plant"].toString()
                        it[total] = item["total"].toString()
                    }
                }catch (e:Exception){
                    LOGGER.error("Exception : [" + e.localizedMessage + "]")

                }
            }
        }
    }

    suspend fun loadDataCoastalFlooding(path:String, codeList:List<String>, limit:Int): List<DataFrame<*>> = coroutineScope {
        val numOfRows = 300

        // Dispatchers.IO에서 최대 10개의 스레드만 사용하도록 제한된 디스패처 생성
        val limitedDispatcher = Dispatchers.IO.limitedParallelism(limit)

        LOGGER.info("loadDataCoastalFlooding limitedDispatcher: ${limit}")

        // 각 코드를 비동기(async)로 실행하여 List<Deferred<DataFrame>> 생성
        val deferredResults = codeList.map {  it ->
            async(limitedDispatcher ) { // 네트워크 IO를 위한 IO 디스패처 사용
                val baseUrl = "${path}&numOfRows=${numOfRows}&sggCd=${it}"
                var url = ""

                    try{
                        url = "${baseUrl}&pageNo=1"
                        val df_first = DataFrame.readJson(url)
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

                    } catch (err: Exception) {
                        LOGGER.error("Error processing $url : ", err)
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
                "${configData.WATER_LOGGED?.node}",
                "${configData.WATER_LOGGED?.nodeOption}",
                "${configData.WATER_LOGGED?.mapshaper}",
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
            val path = "${configData.WATER_LOGGED?.endPoint}/${configData.WATER_LOGGED?.subPath}" +
                    "?serviceKey=${configData.WATER_LOGGED?.apikey}&type=json"

            // 1. 시군구 코드 목록 추출 (짧은 트랜잭션)
            val codeList = transaction(CollectionServerConfig.conn) {
                SggCode.select(SggCode.sgg_code).map { it ->
                    it[SggCode.sgg_code].trim()
                }
            }

            // 2. [핵심 수정] 네트워크로부터 데이터 비동기 수집 (트랜잭션 밖에서 수행)
            // List<List<DataFrame>>을 받아오게 되므로 flatten 후 concat

            val limit = configData.WATER_LOGGED?.limitedParallelism ?: 1

            loadDataCoastalFlooding(path, codeList, limit).let { rawDataFrames ->

                val result = rawDataFrames.concat()
                // 1. 데이터 수집 및 초기화 단계
                val updateTargets = suspendTransaction( CollectionServerConfig.conn) {


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
                    val mapShaperLimit = configData.WATER_LOGGED?.mapshaperLimitedParallelism ?: 3
                    LOGGER.info("loadDataCoastalFlooding mapShaperLimitedDispatcher: ${mapShaperLimit}")
                    val mapShaperLimitedDispatcher = Dispatchers.IO.limitedParallelism(mapShaperLimit)

                    updateTargets.forEach { ctpvNm ->

                        listOf("F", "E", "D", "C", "B", "A").forEach { grade ->

                            launch(mapShaperLimitedDispatcher ) { // 네트워크 IO를 위한 IO 디스패처 사용

                                    val geoJsonObject = suspendTransaction( CollectionServerConfig.conn) {
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

                                    suspendTransaction( CollectionServerConfig.conn) {

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
            suspendTransaction( CollectionServerConfig.conn) {
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

         val url = "${configData.SDOT_Gyonggi?.endPoint}" +
                   "/${configData.SDOT_Gyonggi?.subPath}" +
                 "?KEY=${configData.SDOT_Gyonggi?.apikey}" +
                 "&Type=${configData.SDOT_Gyonggi?.type}" +
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

            LOGGER.info("\n"+ result.schema().toString())
            LOGGER.info("\n"+ result.head(5).toString())

            transaction(CollectionServerConfig.conn) {
                SchemaUtils.create(SDoT_EnvInfo_Gyonggi)

                result.forEach {  item  ->
                    try{
                        SDoT_EnvInfo_Gyonggi.insertIgnore { it ->

                            it[obs] = item["MESURSTN_NM"].toString()
                            it[region] = item["MESRNW_NM"].toString()
                            it[sensing_time] = item["MESURE_DAY_TM"].toString()
                            it[so2] = item["SO2"].toString()
                            it[co] = item["CO"].toString()
                            it[no2] = item["NO2"].toString()
                            it[o3] = item["O3"].toString()
                            it[pm10] = item["PM10"].toString()
                            it[pm25] = item["PM2.5"].toString()

                        }
                    } catch (e:Exception){
                        e.localizedMessage?.let { msg ->
                            LOGGER.debug(msg)
                            LOGGER.debug("Exception PRIMARYKEY: [" + item["MESURSTN_NM"].toString() + "," + item["MESURE_DAY_TM"].toString() + "]")

                        }
                    }
                }


            }



        }catch (e: Exception){
            val msg = e.localizedMessage
        }
    }

    suspend fun getSDoTEnvInfo(){

        //http://openapi.seoul.go.kr:8088/6e4a49477579656f393876794a6a63/json/sDoTEnv/1001/1200/
        // 1. 현재 S-DoT 장비의 unique count 값이 1170.
        // 2. 최초 1000 건을 수집하되 SENSING_TIME 이 unique 하면 200 건을 더 수집.
        // 3. 수집된 데이터중 SENSING_TIME 이 MAX(SENSING_TIME) 인 값만 filtering.

        val url = "${configData.SDOT_API?.endPoint}/${configData.SDOT_API?.apikey}/" +
                "${configData.SDOT_API?.type}/" +
                "${configData.SDOT_API?.subPath}/"
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
                transaction(CollectionServerConfig.conn) {
                    SchemaUtils.create(SDoT_EnvInfo)
                    finalData.forEach { item ->
                        try {
                            SDoT_EnvInfo.upsert { it ->
                                it[SDoT_EnvInfo.modelname] = item.MODELNAME
                                it[SDoT_EnvInfo.serial] = item.SERIAL
                                it[SDoT_EnvInfo.sensing_time] = item.SENSING_TIME
                                it[SDoT_EnvInfo.region] = item.REGION
                                it[SDoT_EnvInfo.autonomous_district] = item.AUTONOMOUS_DISTRICT
                                it[SDoT_EnvInfo.administrative_district] = item.ADMINISTRATIVE_DISTRICT
                                it[SDoT_EnvInfo.max_temp] = item.MAX_TEMP
                                it[SDoT_EnvInfo.avg_temp] = item.AVG_TEMP
                                it[SDoT_EnvInfo.min_temp] = item.MIN_TEMP
                                it[SDoT_EnvInfo.max_humi] = item.MAX_HUMI
                                it[SDoT_EnvInfo.avg_humi] = item.AVG_HUMI
                                it[SDoT_EnvInfo.min_humi] = item.MIN_HUMI
                                it[SDoT_EnvInfo.max_wind_speed] = item.MAX_WIND_SPEED
                                it[SDoT_EnvInfo.avg_wind_speed] = item.AVG_WIND_SPEED
                                it[SDoT_EnvInfo.min_wind_speed] = item.MIN_WIND_SPEED
                                it[SDoT_EnvInfo.max_wind_dire] = item.MAX_WIND_DIRE
                                it[SDoT_EnvInfo.avg_wind_dire] = item.AVG_WIND_DIRE
                                it[SDoT_EnvInfo.min_wind_dire] = item.MIN_WIND_DIRE
                                it[SDoT_EnvInfo.max_inte_illu] = item.MAX_INTE_ILLU
                                it[SDoT_EnvInfo.avg_inte_illu] = item.AVG_INTE_ILLU
                                it[SDoT_EnvInfo.min_inte_illu] = item.MIN_INTE_ILLU
                                it[SDoT_EnvInfo.max_ultra_rays] = item.MAX_ULTRA_RAYS
                                it[SDoT_EnvInfo.avg_ultra_rays] = item.AVG_ULTRA_RAYS
                                it[SDoT_EnvInfo.min_ultra_rays] = item.MIN_ULTRA_RAYS
                                it[SDoT_EnvInfo.max_noise] = item.MAX_NOISE
                                it[SDoT_EnvInfo.avg_noise] = item.AVG_NOISE
                                it[SDoT_EnvInfo.min_noise] = item.MIN_NOISE
                                it[SDoT_EnvInfo.max_vibr_x] = item.MAX_VIBR_X
                                it[SDoT_EnvInfo.avg_vibr_x] = item.AVG_VIBR_X
                                it[SDoT_EnvInfo.min_vibr_x] = item.MIN_VIBR_X
                                it[SDoT_EnvInfo.max_vibr_y] = item.MAX_VIBR_Y
                                it[SDoT_EnvInfo.avg_vibr_y] = item.AVG_VIBR_Y
                                it[SDoT_EnvInfo.min_vibr_y] = item.MIN_VIBR_Y
                                it[SDoT_EnvInfo.max_vibr_z] = item.MAX_VIBR_Z
                                it[SDoT_EnvInfo.avg_vibr_z] = item.AVG_VIBR_Z
                                it[SDoT_EnvInfo.min_vibr_z] = item.MIN_VIBR_Z
                                it[SDoT_EnvInfo.max_effe_temp] = item.MAX_EFFE_TEMP
                                it[SDoT_EnvInfo.avg_effe_temp] = item.AVG_EFFE_TEMP
                                it[SDoT_EnvInfo.min_effe_temp] = item.MIN_EFFE_TEMP
                                it[SDoT_EnvInfo.max_no2] = item.MAX_NO2
                                it[SDoT_EnvInfo.avg_no2] = item.AVG_NO2
                                it[SDoT_EnvInfo.min_no2] = item.MIN_NO2
                                it[SDoT_EnvInfo.max_co] = item.MAX_CO
                                it[SDoT_EnvInfo.avg_co] = item.AVG_CO
                                it[SDoT_EnvInfo.min_co] = item.MIN_CO
                                it[SDoT_EnvInfo.max_so2] = item.MAX_SO2
                                it[SDoT_EnvInfo.avg_so2] = item.AVG_SO2
                                it[SDoT_EnvInfo.min_so2] = item.MIN_SO2
                                it[SDoT_EnvInfo.max_nh3] = item.MAX_NH3
                                it[SDoT_EnvInfo.avg_nh3] = item.AVG_NH3
                                it[SDoT_EnvInfo.min_nh3] = item.MIN_NH3
                                it[SDoT_EnvInfo.max_h2s] = item.MAX_H2S
                                it[SDoT_EnvInfo.avg_h2s] = item.AVG_H2S
                                it[SDoT_EnvInfo.min_h2s] = item.MIN_H2S
                                it[SDoT_EnvInfo.max_o3] = item.MAX_O3
                                it[SDoT_EnvInfo.avg_o3] = item.AVG_O3
                                it[SDoT_EnvInfo.min_o3] = item.MIN_O3
                                it[SDoT_EnvInfo.date] = item.DATE
                                it[SDoT_EnvInfo.data_no] = item.DATA_NO

                            }
                        } catch (e: Exception) {
                            e.localizedMessage?.let { msg ->
                                LOGGER.debug(msg)
                            }
                        }

                    }
                }

            }

        } catch (e: Exception){
            val msg = e.localizedMessage
        }
    }

    @OptIn(FormatStringsInDatetimeFormats::class)
    suspend fun getKhoaTidalCurrent(){
        var now = Clock.System.now()
        val interval = configData.KHOA_TIDALCURRENT_API?.interval ?: 5
        val predictedTotalMinute = configData.KHOA_TIDALCURRENT_API?.predictedTotalMinute ?: 60
        val windowSize = predictedTotalMinute / interval

        repeat(windowSize){

            var localDateTime = now.toLocalDateTime(TimeZone.of("Asia/Seoul"))
            localDateTime =  LocalDateTime(
                localDateTime.year,
                localDateTime.month,
                localDateTime.day,
                localDateTime.hour,
                (localDateTime.minute / interval) * interval
            )

            now = now.plus(interval, DateTimeUnit.MINUTE)

            val datetime = localDateTime
                .format(LocalDateTime.Format { byUnicodePattern("yyyyMMddHHmm") })

            val date = datetime.substring(0,8)
            val hour = datetime.substring(8,10)
            val minute = datetime.substring(10,12)

            val url = "${configData.KHOA_TIDALCURRENT_API?.endPoint}/${configData.KHOA_TIDALCURRENT_API?.subPath}?ServiceKey=${configData.KHOA_TIDALCURRENT_API?.apikey}&ResultType=${configData.KHOA_TIDALCURRENT_API?.type}${configData.KHOA_TIDALCURRENT_API?.boundBox}&Date=${date}&Hour=${hour}&Minute=${minute}"

            try {
                CollectionServerRestApi.callKhoaAPI_json(url).let {

                    val response = CollectionServerRestApi.commonJson.decodeFromString<KhonTidalCurrentInfoResponse>(it)

                    LOGGER.info("${::getKhoaTidalCurrent.name} [receive count[${response.result.meta.sch_time}]]")

                    transaction(CollectionServerConfig.conn) {
                        SchemaUtils.create(TidalCurrentInfoKHOA)
                        response.result.data.forEach { item ->
                            try {
                                TidalCurrentInfoKHOA.upsert { it ->
                                    it[TidalCurrentInfoKHOA.sch_time] = response.result.meta.sch_time
                                    it[TidalCurrentInfoKHOA.pre_lon] = item.pre_lon.toDouble()
                                    it[TidalCurrentInfoKHOA.pre_lat] = item.pre_lat.toDouble()
                                    it[TidalCurrentInfoKHOA.current_dir] = item.current_dir.toDouble()
                                    it[TidalCurrentInfoKHOA.current_speed] = item.current_speed.toDouble()
                                }
                            } catch (e: Exception) {
                                e.localizedMessage?.let { msg ->
                                    LOGGER.debug(msg)
                                }
                            }

                        }
                    }

                }
            }catch (e: Exception){
                val msg = e.localizedMessage
            }

        }
    }



     @OptIn(FormatStringsInDatetimeFormats::class)
     suspend fun getKhoaObservation()  {

             val reqDate =
                 kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.of("Asia/Seoul"))
                     .format(LocalDateTime.Format { byUnicodePattern("yyyyMMdd") })

             val url = "${configData.KHOA_API?.endPoint}/${configData.KHOA_API?.subPath}" +
                     "?serviceKey=${configData.KHOA_API?.apikey}" +
                     "&type=${configData.KHOA_API?.type}" +
                     "&reqDate=${reqDate}" +
                     "&min=${configData.KHOA_API?.min}" +
                     "&numOfRows=${configData.KHOA_API?.numOfRows}"


             val result = transaction(CollectionServerConfig.conn) {
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

                                 transaction(CollectionServerConfig.conn) {
                                     SchemaUtils.create(ObservationKHOA)
                                     SchemaUtils.create(ObservatoryKHOA)

                                     recvData.body.items.item.forEach { item ->
                                         try {
                                             ObservationKHOA.insertIgnore { it ->
                                                 it[ObservationKHOA.obsCode] = obsCode
                                                 it[ObservationKHOA.obsrvnDt] = item.obsrvnDt
                                                 it[ObservationKHOA.wndrct] = item.wndrct?.toString()
                                                 it[ObservationKHOA.wspd] = item.wspd?.toString()
                                                 it[ObservationKHOA.maxMmntWspd] =
                                                     item.maxMmntWspd?.toString()
                                                 it[ObservationKHOA.artmp] = item.artmp?.toString()
                                                 it[ObservationKHOA.atmpr] = item.atmpr?.toString()
                                                 it[ObservationKHOA.wvhgt] = item.wvhgt?.toString()
                                                 it[ObservationKHOA.wvpd] = item.wvpd?.toString()
                                                 it[ObservationKHOA.crdir] = item.crdir?.toString()
                                                 it[ObservationKHOA.crsp] = item.crsp?.toString()
                                                 it[ObservationKHOA.wtem] = item.wtem?.toString()
                                                 it[ObservationKHOA.slnty] = item.slnty?.toString()
                                             }
                                         } catch (e: Exception) {
                                             e.localizedMessage?.let { msg ->
                                                 LOGGER.debug(msg)
                                             }
                                         }
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

                        transaction (CollectionServerConfig.conn){
                            SchemaUtils.create( OWQInformationTable)
                            result.forEach {  item  ->
                                try{
                                    OWQInformationTable.insertIgnore { it ->

                                        it[rtmWqWtchDtlDt] = item["rtmWqWtchDtlDt"].toString().substringBefore('.')
                                        it[rtmWqWtchStaCd] = item["rtmWqWtchStaCd"].toString()
                                        it[rtmWtchWtem] =  String.format("%.3f", item["rtmWtchWtem"].toString().toDouble())
                                        it[rtmWqCndctv] = String.format("%.3f", item["rtmWqCndctv"].toString().toFloat())
                                        it[ph] = String.format("%.2f", item["ph"].toString().toFloat())
                                        it[rtmWqDoxn] = String.format("%.3f", item["rtmWqDoxn"].toString().toDouble())
                                        it[rtmWqTu] = item["rtmWqTu"].toString()
                                        it[rtmWqBgalgsQy] = item["rtmWqBgalgsQy"].toString()
                                        it[rtmWqChpla] = String.format("%.3f", item["rtmWqChpla"].toString().toDouble())
                                        it[rtmWqSlnty] = String.format("%.3f", item["rtmWqSlnty"].toString().toFloat())
                                    }
                                } catch (e:Exception){
                                    e.localizedMessage?.let { msg ->
                                        LOGGER.debug(msg)
                                        LOGGER.debug("Exception PRIMARYKEY: [" + item["rtmWqWtchDtlDt"].toString() + "," + item["rtmWqWtchStaCd"].toString() + "]")

                                    }
                                }
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
                    transaction (CollectionServerConfig.conn){

                        SchemaUtils.create( ObservationTable)

                        recvData.body.item.forEach { item ->

                            try{

                                if(!item.wtr_tmp.isNullOrBlank()) {
                                    ObservationTable.insertIgnore { it ->
                                        it[sta_cde] = item.sta_cde
                                        it[sta_nam_kor] = item.sta_nam_kor
                                        it[obs_dat] = item.obs_dat
                                        it[obs_tim] = item.obs_tim
                                        it[obs_datetime] = "${item.obs_dat} ${item.obs_tim}"
                                        it[repair_gbn] = item.repair_gbn
                                        it[obs_lay] = item.obs_lay
                                        it[wtr_tmp] = item.wtr_tmp
                                    }
                                }
                            } catch (e:Exception){

                                e.localizedMessage?.let { msg ->
                                    LOGGER.debug(msg)
                                    LOGGER.debug("Exception PRIMARYKEY: [" + item.sta_cde + "," + item.obs_dat + "," + item.obs_tim + "," + item.obs_lay + "]")
                                }
                            }

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

                    transaction (CollectionServerConfig.conn){
                        SchemaUtils.drop( ObservatoryTable)
                        SchemaUtils.create( ObservatoryTable)
                        recvData.body.item.forEach { item ->
                            try {
                                ObservatoryTable.insertIgnore { it ->
                                    it[sta_cde] = item.sta_cde
                                    it[sta_nam_kor] = item.sta_nam_kor
                                    it[bld_dat] = item.bld_dat
                                    it[end_dat] = item.end_dat
                                    it[gru_nam] = item.gru_nam
                                    it[lon] = item.lon
                                    it[lat] = item.lat
                                    it[sur_tmp_yn] = item.sur_tmp_yn
                                    it[mid_tmp_yn] = item.mid_tmp_yn
                                    it[bot_tmp_yn] = item.bot_tmp_yn
                                    it[sur_dep] = item.sur_dep
                                    it[mid_dep] = item.mid_dep
                                    it[bot_dep] = item.bot_dep
                                    it[sta_des] = item.sta_des
                                }
                            }catch (e:Exception){
                                e.localizedMessage?.let { msg ->
                                    LOGGER.debug(msg)
                                    LOGGER.debug("Exception PRIMARYKEY: [${item.sta_cde}]")
                                }
                            }

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