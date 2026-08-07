package com.unchil.oceanwaterinfo


import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.FloatColumnType
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.avg
import org.jetbrains.exposed.v1.core.castTo
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.max
import org.jetbrains.exposed.v1.core.min
import org.jetbrains.exposed.v1.core.substring
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime


// 캐시를 저장할 ConcurrentHashMap. 스레드 안전성을 보장합니다.
private val cacheStorage_SeawaterInfo = ConcurrentHashMap<String, Pair<List<SeawaterInformationByObservationPoint>, Long>>()
private val cacheStorage_SeawaterInfo_Mof = ConcurrentHashMap<String, Pair<List<SeaWaterInformation>, Long>>()
private val cacheStorage_SeaWaterInfoStatistics = ConcurrentHashMap<String, Pair<List<SeaWaterInfoByOneHourStat>, Long>>()
private val cacheStorage_SeaWaterInfoBoxPlot = ConcurrentHashMap<String, Pair<List<SeaWaterBoxPlotStat>, Long>>()
private val cacheStorage_KhoaObservationInfo = ConcurrentHashMap<String, Pair<List<KhoaObservation>, Long>>()
private val cacheStorage_KhoaObservationInfoCurrent = ConcurrentHashMap<String, Pair<List<KhoaObservation>, Long>>()
private val cacheStorage_KhoaObservatoryInfo = ConcurrentHashMap<String, Pair<List<KhonObservatory>, Long>>()
private val cacheStorage_KhoaTidalCurrentInfo = ConcurrentHashMap<String, Pair<List<TidalCurrentInfo>, Long>>()
private val cacheStorage_SDoTEnvInfo = ConcurrentHashMap<String, Pair<List<SDoTEnvInformation>, Long>>()
private val cacheStorage_SDoTEnvInfoGyonggi = ConcurrentHashMap<String, Pair<List<SDoTEnvInformationGyonggi>, Long>>()
private val cacheStorage_SDoTEnvInfoUnion = ConcurrentHashMap<String, Pair<List<SDoTEnvInfoUnion>, Long>>()
private val cacheStorage_KHNPWasteWater = ConcurrentHashMap<String, Pair<List<KHNPWasteWater>, Long>>()
private val cacheStorage_KHNPThermalWasteWater = ConcurrentHashMap<String, Pair<List<KHNPThermalWasteWater>, Long>>()
private val cacheStorage_KHNPRadioRate = ConcurrentHashMap<String, Pair<List<KHNPRadioRate>, Long>>()
private val cacheStorage_KHNPRadioActiveWaste = ConcurrentHashMap<String, Pair<List<KHNPRadioActiveWaste>, Long>>()
private val cacheStorage_KHNPPlantState = ConcurrentHashMap<String, Pair<List<KHNPPlantOperationInfo>, Long>>()

private val cacheStorage_CoastalFloodingGeoJsonObject = ConcurrentHashMap<String, Pair<List<CoastalFloodingGeoJsonObject>, Long>>()

private const val CACHE_EXPIRY_SECONDS =  1 * 60L

class Repository {


    fun coastalFloodingGeoJsonObject( grade:String, sido:String, type:String ):List<CoastalFloodingGeoJsonObject> {
        /*
        val key = "cache_coastalFloodingGeoJsonObject_${grade}_${sido}"
        val now = System.currentTimeMillis()
        val duration =  12 * 60 * 60L

        cacheStorage_CoastalFloodingGeoJsonObject[key]?.let { cachedData ->
            if ((now - cachedData.second) < TimeUnit.SECONDS.toMillis(duration)) {
                LOGGER.info("Serving from cache for ID:${key}")
                return cachedData.first
            }
        }
*/
        val resultFromDb = fetchCoastalFloodingGeoJsonObjectFromDb(grade, sido, type)
/*
        if (resultFromDb.isNotEmpty() ) {
            cacheStorage_CoastalFloodingGeoJsonObject[key] = Pair(resultFromDb, now)
        }

 */

        return resultFromDb
    }


    fun coastalFloodingGeo(page: Int, size: Int, grade:String, sido:String):List<CoastalFloodingGeo> {
        /*
        val key = "cache_coastalFloodingGeo"
        val now = System.currentTimeMillis()

        // 캐시에서 데이터 조회 (suspendTransaction 외부)

        cacheStorage_CoastalFloodingGeo[key]?.let { cachedData ->
            if ((now - cachedData.second) < TimeUnit.SECONDS.toMillis(CACHE_EXPIRY_SECONDS)) {
                LOGGER.info("Serving from cache for ID:${key}")
                return cachedData.first
            }
        }

         */

        val resultFromDb = fetchCoastalFloodingGeoFromDb(page, size, grade, sido)
        /*
        if (resultFromDb.isNotEmpty() ) {
            cacheStorage_CoastalFloodingGeo[key] = Pair(resultFromDb, now)
        }

         */
        return resultFromDb

    }


    fun khnp_PlantState():List<KHNPPlantOperationInfo> {
        val key = "cache_khnp_plantstate"
        val now = System.currentTimeMillis()

        // 캐시에서 데이터 조회 (suspendTransaction 외부)
        cacheStorage_KHNPPlantState[key]?.let { cachedData ->
            if ((now - cachedData.second) < TimeUnit.SECONDS.toMillis(CACHE_EXPIRY_SECONDS)) {
                LOGGER.info("Serving from cache for ID:${key}")
                return cachedData.first
            }
        }

        val resultFromDb = fetchKHNPPlantStateFromDb()
        if (resultFromDb.isNotEmpty() ) {
            cacheStorage_KHNPPlantState[key] = Pair(resultFromDb, now)
        }
        return resultFromDb

    }


    fun khnp_RadioActiveWaste():List<KHNPRadioActiveWaste> {
        val key = "cache_khnp_radioactivewaste"
        val now = System.currentTimeMillis()

        // 캐시에서 데이터 조회 (suspendTransaction 외부)
        cacheStorage_KHNPRadioActiveWaste[key]?.let { cachedData ->
            if ((now - cachedData.second) < TimeUnit.SECONDS.toMillis(CACHE_EXPIRY_SECONDS)) {
                LOGGER.info("Serving from cache for ID:${key}")
                return cachedData.first
            }
        }

        val resultFromDb = fetchKHNPRadioActiveWasteFromDb()
        if (resultFromDb.isNotEmpty() ) {
            cacheStorage_KHNPRadioActiveWaste[key] = Pair(resultFromDb, now)
        }
        return resultFromDb

    }


    fun khnp_RadioRate():List<KHNPRadioRate> {
        val key = "cache_khnp_radiorate"
        val now = System.currentTimeMillis()

        // 캐시에서 데이터 조회 (suspendTransaction 외부)
        cacheStorage_KHNPRadioRate[key]?.let { cachedData ->
            if ((now - cachedData.second) < TimeUnit.SECONDS.toMillis(CACHE_EXPIRY_SECONDS)) {
                LOGGER.info("Serving from cache for ID:${key}")
                return cachedData.first
            }
        }

        val resultFromDb = fetchKHNPRadioRateFromDb()
        if (resultFromDb.isNotEmpty() ) {
            cacheStorage_KHNPRadioRate[key] = Pair(resultFromDb, now)
        }
        return resultFromDb

    }



    fun khnp_ThermalWasteWater():List<KHNPThermalWasteWater> {
        val key = "cache_khnp_thermalwastewater"
        val now = System.currentTimeMillis()

        // 캐시에서 데이터 조회 (suspendTransaction 외부)
        cacheStorage_KHNPThermalWasteWater[key]?.let { cachedData ->
            if ((now - cachedData.second) < TimeUnit.SECONDS.toMillis(CACHE_EXPIRY_SECONDS)) {
                LOGGER.info("Serving from cache for ID:${key}")
                return cachedData.first
            }
        }

        val resultFromDb = fetchKHNPThermalWasteWaterFromDb()
        if (resultFromDb.isNotEmpty() ) {
            cacheStorage_KHNPThermalWasteWater[key] = Pair(resultFromDb, now)
        }
        return resultFromDb

    }


    fun khnp_WasteWater():List<KHNPWasteWater> {
        val key = "cache_khnp_wastewater"
        val now = System.currentTimeMillis()

        // 캐시에서 데이터 조회 (suspendTransaction 외부)
        cacheStorage_KHNPWasteWater[key]?.let { cachedData ->
            if ((now - cachedData.second) < TimeUnit.SECONDS.toMillis(CACHE_EXPIRY_SECONDS)) {
                LOGGER.info("Serving from cache for ID:${key}")
                return cachedData.first
            }
        }

        val resultFromDb = fetchKHNPWasteWaterFromDb()
        if (resultFromDb.isNotEmpty() ) {
            cacheStorage_KHNPWasteWater[key] = Pair(resultFromDb, now)
        }
        return resultFromDb

    }


    fun sDoTEnvInfoUnion():List<SDoTEnvInfoUnion> {
        val key = "cache_sdot_envinfo_union"
        val now = System.currentTimeMillis()

        // 캐시에서 데이터 조회 (suspendTransaction 외부)
        cacheStorage_SDoTEnvInfoUnion[key]?.let { cachedData ->
            if ((now - cachedData.second) < TimeUnit.SECONDS.toMillis(CACHE_EXPIRY_SECONDS)) {
                LOGGER.info("Serving from cache for ID:${key}")
                return cachedData.first
            }
        }

        val resultFromDb = fetchSDoTEnvInfoUnionFromDb()
        if (resultFromDb.isNotEmpty() ) {
            cacheStorage_SDoTEnvInfoUnion[key] = Pair(resultFromDb, now)
        }
        return resultFromDb

    }



    fun sDoTEnvInfoGyonggi():List<SDoTEnvInformationGyonggi> {
        val key = "cache_sdot_envinfo_gyonggi"
        val now = System.currentTimeMillis()

        // 캐시에서 데이터 조회 (suspendTransaction 외부)
        cacheStorage_SDoTEnvInfoGyonggi[key]?.let { cachedData ->
            if ((now - cachedData.second) < TimeUnit.SECONDS.toMillis(CACHE_EXPIRY_SECONDS)) {
                LOGGER.info("Serving from cache for ID:${key}")
                return cachedData.first
            }
        }

        val resultFromDb = fetchSDoTEnvInfoGyonggiFromDb()
        if (resultFromDb.isNotEmpty() ) {
            cacheStorage_SDoTEnvInfoGyonggi[key] = Pair(resultFromDb, now)
        }
        return resultFromDb

    }



    fun sDoTEnvInfo():List<SDoTEnvInformation> {
        val key = "cache_sdot_envinfo"
        val now = System.currentTimeMillis()

        // 캐시에서 데이터 조회 (suspendTransaction 외부)
        cacheStorage_SDoTEnvInfo[key]?.let { cachedData ->
            if ((now - cachedData.second) < TimeUnit.SECONDS.toMillis(CACHE_EXPIRY_SECONDS)) {
                LOGGER.info("Serving from cache for ID:${key}")
                return cachedData.first
            }
        }

        val resultFromDb = fetchSDoTEnvInfoFromDb()
        if (resultFromDb.isNotEmpty() ) {
            cacheStorage_SDoTEnvInfo[key] = Pair(resultFromDb, now)
        }
        return resultFromDb

    }


    fun khoaTidalCurrentInfo():List<TidalCurrentInfo>{
        val key = "cache_khoa_tidal"
        val now = System.currentTimeMillis()

        // 캐시에서 데이터 조회 (suspendTransaction 외부)
        cacheStorage_KhoaTidalCurrentInfo[key]?.let { cachedData ->
            if ((now - cachedData.second) < TimeUnit.SECONDS.toMillis(CACHE_EXPIRY_SECONDS)) {
                LOGGER.info("Serving from cache for ID: khoa_tidal")
                return cachedData.first
            }
        }

        val resultFromDb = fetchKhoaTidalCurrentInfoFromDb()
        if (resultFromDb.isNotEmpty() ) {
            cacheStorage_KhoaTidalCurrentInfo[key] = Pair(resultFromDb, now)
        }
        return resultFromDb

    }


    fun khoaObservationInfoCurrent(): List<KhoaObservation> {
        val key = "cache_khoa_current"
        val now = System.currentTimeMillis()

        // 캐시에서 데이터 조회 (suspendTransaction 외부)
        cacheStorage_KhoaObservationInfoCurrent[key]?.let { cachedData ->
            if ((now - cachedData.second) < TimeUnit.SECONDS.toMillis(CACHE_EXPIRY_SECONDS)) {
                LOGGER.info("Serving from cache for ID: khoa_current")
                return cachedData.first
            }
        }
        // 캐시에 없거나 만료된 경우 DB에서 데이터 조회 (suspendTransaction 내부 호출)
        val resultFromDb = fetchKhoaObservationCurrentFromDb()
        if (resultFromDb.isNotEmpty() ) {
            cacheStorage_KhoaObservationInfoCurrent[key] = Pair(resultFromDb, now)
        }
        return resultFromDb
    }

    fun khoaObservationInfo(): List<KhoaObservation> {
        val key = "cache_khoa"
        val now = System.currentTimeMillis()

        // 캐시에서 데이터 조회 (suspendTransaction 외부)
        cacheStorage_KhoaObservationInfo[key]?.let { cachedData ->
            if ((now - cachedData.second) < TimeUnit.SECONDS.toMillis(CACHE_EXPIRY_SECONDS)) {
                LOGGER.info("Serving from cache for ID: khoa")
                return cachedData.first
            }
        }
        // 캐시에 없거나 만료된 경우 DB에서 데이터 조회 (suspendTransaction 내부 호출)
        val resultFromDb = fetchKhoaObservationFromDb()
        if (resultFromDb.isNotEmpty() ) {
            cacheStorage_KhoaObservationInfo[key] = Pair(resultFromDb, now)
        }
        return resultFromDb
    }

    fun khoaObservatoryInfo(): List<KhonObservatory> {
        val key = "cache_khoa_observatory"
        val now = System.currentTimeMillis()

        // 캐시에서 데이터 조회 (suspendTransaction 외부)
        cacheStorage_KhoaObservatoryInfo[key]?.let { cachedData ->
            if ((now - cachedData.second) < TimeUnit.SECONDS.toMillis(CACHE_EXPIRY_SECONDS)) {
                LOGGER.info("Serving from cache for ID: khoa_observatory")
                return cachedData.first
            }
        }
        // 캐시에 없거나 만료된 경우 DB에서 데이터 조회 (suspendTransaction 내부 호출)
        val resultFromDb = fetchKhoaObservatoryFromDb()
        if (resultFromDb.isNotEmpty() ) {
            cacheStorage_KhoaObservatoryInfo[key] = Pair(resultFromDb, now)
        }
        return resultFromDb
    }



    // 캐시 로직과 DB 조회 호출을 담당하는 메인 함수
    fun seaWaterInfo(division: String): List<SeawaterInformationByObservationPoint> {
        val key = "cache_$division"
        val now = System.currentTimeMillis()

        // 캐시에서 데이터 조회 (suspendTransaction 외부)
        cacheStorage_SeawaterInfo[key]?.let { cachedData ->
            if ((now - cachedData.second) < TimeUnit.SECONDS.toMillis(CACHE_EXPIRY_SECONDS)) {
                LOGGER.info("Serving from cache for ID: $division")
                return cachedData.first
            }
        }
        // 캐시에 없거나 만료된 경우 DB에서 데이터 조회 (suspendTransaction 내부 호출)
        val resultFromDb = fetchSeaWaterInfoFromDb(division)
        if (resultFromDb.isNotEmpty() ) {
            cacheStorage_SeawaterInfo[key] = Pair(resultFromDb, now)
        }
        return resultFromDb
    }

    fun swi(division:String):List<SeaWaterInformation?>{
        val key = "cache_$division"
        val now = System.currentTimeMillis()

        // 캐시에서 데이터 조회 (suspendTransaction 외부)
        cacheStorage_SeawaterInfo_Mof[key]?.let { cachedData ->
            if ((now - cachedData.second) < TimeUnit.SECONDS.toMillis(CACHE_EXPIRY_SECONDS)) {
                LOGGER.info("Serving from cache for ID: $division")
                return cachedData.first
            }
        }

        // 캐시에 없거나 만료된 경우 DB에서 데이터 조회 (suspendTransaction 내부 호출)
        val resultFromDb = fetchSeaWaterInfoFromDb_Mof(division)
        if (resultFromDb.isNotEmpty() ) {
            cacheStorage_SeawaterInfo_Mof[key] = Pair(resultFromDb, now)
        }
        return resultFromDb
    }


    fun seaWaterInfoOneDayBoxPlot(division:String):List<SeaWaterBoxPlotStat?>{
        val key = "cache_$division"
        val now = System.currentTimeMillis()

        // 캐시에서 데이터 조회 (suspendTransaction 외부)
        cacheStorage_SeaWaterInfoBoxPlot[key]?.let { cachedData ->
            if ((now - cachedData.second) < TimeUnit.SECONDS.toMillis(CACHE_EXPIRY_SECONDS)) {
                LOGGER.info("Serving from cache for ID: $division")
                return cachedData.first
            }
        }

        // 캐시에 없거나 만료된 경우 DB에서 데이터 조회 (suspendTransaction 내부 호출)
        val resultFromDb = fetchSeaWaterInfoOneDayBoxPlotFromDb()
        if (resultFromDb.isNotEmpty() ) {
            cacheStorage_SeaWaterInfoBoxPlot[key] = Pair(resultFromDb, now)
        }
        return resultFromDb
    }

    fun fetchCoastalFloodingGeoJsonObjectFromDb(grade:String, ctpvNm:String, type:String): List<CoastalFloodingGeoJsonObject> = transaction {
        LOGGER.info("Serving from DB for : fetchCoastalFloodingGeoJsonObjectFromDb Start")

        val result = when(type){
            "create" -> {

                val geoJsonObject = CoastalFloodingGeoTbl
                    .select(
                        CoastalFloodingGeoTbl.grade,
                        CoastalFloodingGeoTbl.flodVlCn,
                        CoastalFloodingGeoTbl.ctpvNm,
                        CoastalFloodingGeoTbl.geom
                    )
                    .where {
                        (CoastalFloodingGeoTbl.grade eq grade)  and
                                (CoastalFloodingGeoTbl.ctpvNm eq ctpvNm)
                    }
                    .map{
                        CoastalFloodingGeo(
                            grade = it[CoastalFloodingGeoTbl.grade],
                            flodVlCn = it[CoastalFloodingGeoTbl.flodVlCn],
                            ctpvNm = it[CoastalFloodingGeoTbl.ctpvNm],
                            geom = it[CoastalFloodingGeoTbl.geom]
                        )
                    }.toGeoJsonObject(Pair(ctpvNm, grade))


                SchemaUtils.create(CoastalFloodingGeoJsonObjectTbl)

                try {

                    // Exposed v0.41+ 기준 deleteWhere 문법
                    CoastalFloodingGeoJsonObjectTbl.deleteWhere {
                        (CoastalFloodingGeoJsonObjectTbl.grade eq grade) and
                                (CoastalFloodingGeoJsonObjectTbl.ctpvNm eq ctpvNm)
                    }

                    // 4. 새로운 데이터 Insert
                    CoastalFloodingGeoJsonObjectTbl.insert {
                        it[CoastalFloodingGeoJsonObjectTbl.grade] = grade
                        it[CoastalFloodingGeoJsonObjectTbl.ctpvNm] = ctpvNm
                        it[CoastalFloodingGeoJsonObjectTbl.geojson] = geoJsonObject
                    }

                } catch (e: Exception) {
                    e.localizedMessage?.let { msg ->
                        LOGGER.debug(msg)
                    }
                }

                listOf(
                    CoastalFloodingGeoJsonObject(
                        grade = grade,
                        ctpvNm = ctpvNm,
                        geojson = geoJsonObject
                    )
                )

            }

            else -> {
                CoastalFloodingGeoJsonObjectTbl
                    .select(
                        CoastalFloodingGeoJsonObjectTbl.grade,
                        CoastalFloodingGeoJsonObjectTbl.ctpvNm,
                        CoastalFloodingGeoJsonObjectTbl.geojson
                    )
                    .where {
                        (CoastalFloodingGeoJsonObjectTbl.grade eq grade)  and
                        (CoastalFloodingGeoJsonObjectTbl.ctpvNm eq ctpvNm)
                    }
                    .map{
                        CoastalFloodingGeoJsonObject(
                            grade = it[CoastalFloodingGeoJsonObjectTbl.grade],
                            ctpvNm = it[CoastalFloodingGeoJsonObjectTbl.ctpvNm],
                            geojson = it[CoastalFloodingGeoJsonObjectTbl.geojson]
                        )
                    }

            }
        }
        LOGGER.info("Serving from DB for : fetchCoastalFloodingGeoJsonObjectFromDb End")
        return@transaction result
    }

    fun fetchCoastalFloodingGeoFromDb(page: Int, size: Int, grade:String, ctpvNm:String): List<CoastalFloodingGeo> = transaction {
        LOGGER.info("Serving from DB for : fetchCoastalFloodingGeoFromDb")


        val offset = ((page - 1) * size).toLong()

        val result = CoastalFloodingGeoTbl
            .select(
                CoastalFloodingGeoTbl.grade,
                CoastalFloodingGeoTbl.flodVlCn,
                CoastalFloodingGeoTbl.ctpvNm,
                CoastalFloodingGeoTbl.geom
            )
            .where {
                // 1. 기본적으로 grade 조건은 필수
                val condition = CoastalFloodingGeoTbl.grade eq grade
                // 2. sido가 비어있지 않을 경우에만 AND 조건 추가
                if (ctpvNm.isNotEmpty()) {
                    condition and (CoastalFloodingGeoTbl.ctpvNm eq ctpvNm)
                } else {
                    condition
                }
            }
            .offset(offset)
            .limit(size)
            .map{
                CoastalFloodingGeo(
                    grade = it[CoastalFloodingGeoTbl.grade],
                    flodVlCn = it[CoastalFloodingGeoTbl.flodVlCn],
                    ctpvNm = it[CoastalFloodingGeoTbl.ctpvNm],
                    geom = it[CoastalFloodingGeoTbl.geom]
                )
            }
        return@transaction result
    }

    fun fetchKHNPPlantStateFromDb(): List<KHNPPlantOperationInfo> = transaction {
        LOGGER.info("Serving from DB for : fetchKHNPPlantStateFromDb")


        val result = KHNP_PlantOperationInfo.selectAll()
            .map { it ->
                KHNPPlantOperationInfo(
                    it[KHNP_PlantOperationInfo.collectionTime],
                    it[KHNP_PlantOperationInfo.siteCd],
                    it[KHNP_PlantOperationInfo.unitCd],
                    it[KHNP_PlantOperationInfo.unitDttm],
                    it[KHNP_PlantOperationInfo.genName],
                    it[KHNP_PlantOperationInfo.unitNm],
                    it[KHNP_PlantOperationInfo.unitSt]
                )
            }

        return@transaction result
    }


    fun fetchKHNPRadioActiveWasteFromDb(): List<KHNPRadioActiveWaste> = transaction {
        LOGGER.info("Serving from DB for : fetchKHNPRadioActiveWasteFromDb")


        val result = KHNP_RadioActiveWaste.selectAll()
            .map { it ->
                val year = it[KHNP_RadioActiveWaste.spmon].substring(0, 4)
                val month = it[KHNP_RadioActiveWaste.spmon].substring(4, 6)
                KHNPRadioActiveWaste(
                    it[KHNP_RadioActiveWaste.spmon],
                    year,
                    month,
                    it[KHNP_RadioActiveWaste.genName],
                    it[KHNP_RadioActiveWaste.plant],
                    it[KHNP_RadioActiveWaste.total]
                )
            }

        return@transaction result
    }

    fun fetchKHNPRadioRateFromDb(): List<KHNPRadioRate> = transaction {
        LOGGER.info("Serving from DB for : fetchKHNPRadioRateFromDb")
        val maxCollectionTime = KHNP_RadioRate.collectionTime.max()
        val lastTime = KHNP_RadioRate.select(maxCollectionTime).limit(1).map {
            it[maxCollectionTime]
        }.firstOrNull()

        val result = KHNP_RadioRate.selectAll().where { KHNP_RadioRate.collectionTime eq (lastTime ?: "") }
            .map { it ->
                KHNPRadioRate(
                    it[KHNP_RadioRate.time],
                    it[KHNP_RadioRate.genName],
                    it[KHNP_RadioRate.name],
                    it[KHNP_RadioRate.expl],
                    it[KHNP_RadioRate.value],
                )
            }

        return@transaction result
    }



    fun fetchKHNPThermalWasteWaterFromDb():  List<KHNPThermalWasteWater> = transaction {
        LOGGER.info("Serving from DB for : fetchKHNPThermalWasteWaterFromDb")
        val previous24Hour =
            kotlin.time.Clock.System.now()
                .minus(24, DateTimeUnit.HOUR)
                .toLocalDateTime(TimeZone.of("Asia/Seoul"))
                .format(LocalDateTime.Format{byUnicodePattern("yyyy-MM-dd HH:mm")})

        val result = KHNP_ThermalWasteWater.select(
            KHNP_ThermalWasteWater.time,
            KHNP_ThermalWasteWater.genName,
            KHNP_ThermalWasteWater.rm001,
            KHNP_ThermalWasteWater.rm001_time,
            KHNP_ThermalWasteWater.rm002,
            KHNP_ThermalWasteWater.rm002_time,
            KHNP_ThermalWasteWater.rm005,
            KHNP_ThermalWasteWater.rm005_time,
            KHNP_ThermalWasteWater.rm006,
            KHNP_ThermalWasteWater.rm006_time
        ).where { KHNP_ThermalWasteWater.time greaterEq previous24Hour }
            .map {
                KHNPThermalWasteWater(
                    it[KHNP_ThermalWasteWater.time],
                    it[KHNP_ThermalWasteWater.genName],
                    it[KHNP_ThermalWasteWater.rm001],
                    it[KHNP_ThermalWasteWater.rm001_time],
                    it[KHNP_ThermalWasteWater.rm002],
                    it[KHNP_ThermalWasteWater.rm002_time],
                    it[KHNP_ThermalWasteWater.rm005],
                    it[KHNP_ThermalWasteWater.rm005_time],
                    it[KHNP_ThermalWasteWater.rm006],
                    it[KHNP_ThermalWasteWater.rm006_time]
                )
            }
        return@transaction result

    }

    fun fetchKHNPWasteWaterFromDb(): List<KHNPWasteWater> = transaction {
        LOGGER.info("Serving from DB for : fetchKHNPWasteWaterFromDb")
        val previous24Hour =
            kotlin.time.Clock.System.now()
                .minus(6, DateTimeUnit.HOUR)
                .toLocalDateTime(TimeZone.of("Asia/Seoul"))
                .format(LocalDateTime.Format{byUnicodePattern("yyyy-MM-dd HH:mm")})

        val result = KHNP_WasteWater.select(
            KHNP_WasteWater.time,
            KHNP_WasteWater.genName,
            KHNP_WasteWater.tm001,
            KHNP_WasteWater.tm001_time,
            KHNP_WasteWater.tm002,
            KHNP_WasteWater.tm002_time
        ).where { KHNP_WasteWater.time greaterEq previous24Hour }
            .map {
                KHNPWasteWater(
                    it[KHNP_WasteWater.time],
                    it[KHNP_WasteWater.genName],
                    it[KHNP_WasteWater.tm001],
                    it[KHNP_WasteWater.tm001_time],
                    it[KHNP_WasteWater.tm002],
                    it[KHNP_WasteWater.tm002_time]
                )
            }
        return@transaction result

    }

    fun fetchSDoTEnvInfoUnionFromDb(): List<SDoTEnvInfoUnion> = transaction {
        LOGGER.info("Serving from DB for : fetchSDoTEnvInfoUnionFromDb")

        // 1. 서울(SDoT) 최신 시간 조회
        val maxSensingTime = SDoT_EnvInfo.sensing_time.max()
        val seoulLastTime = SDoT_EnvInfo.select(maxSensingTime).limit(1).map {
            it[maxSensingTime]
        }.firstOrNull()

        // 2. 경기도(Gyonggi) 최신 시간 조회
        val maxSensingTimeGyonggi = SDoT_EnvInfo_Gyonggi.sensing_time.max()
        val gyonggiLastTime = SDoT_EnvInfo_Gyonggi.select(maxSensingTimeGyonggi).limit(1).map {
            it[maxSensingTimeGyonggi]
        }.firstOrNull()

        // 3. 서울 데이터 쿼리 (Join + Select)
        // 경기도에만 있는 pm10, pm25는 빈 값(stringLiteral)으로 대체
        val seoulQuery = SDoT_EnvInfo.join(
            SDoT_Location,
            JoinType.INNER,
            onColumn = SDoT_EnvInfo.serial,
            otherColumn = SDoT_Location.serial
        ).select(
                SDoT_EnvInfo.sensing_time, SDoT_EnvInfo.serial, SDoT_EnvInfo.region,
                SDoT_Location.addr, SDoT_Location.lat, SDoT_Location.lng,
                SDoT_EnvInfo.max_so2, SDoT_EnvInfo.max_co, SDoT_EnvInfo.max_no2,
                SDoT_EnvInfo.max_o3, SDoT_EnvInfo.max_nh3, SDoT_EnvInfo.max_h2s
            ).where { SDoT_EnvInfo.sensing_time eq (seoulLastTime ?: "") }
            .map { resultRow ->
                toSDoTEnvInfoUnionFromSDoT_EnvInfo(resultRow)
            }


        // 4. 경기도 데이터 쿼리 (Join + Select)
        // 서울에만 있는 nh3, h2s는 빈 값(stringLiteral)으로 대체
        val gyonggiQuery = SDoT_EnvInfo_Gyonggi.join(
            SDoT_Location_Gyonggi,
            JoinType.INNER,
            onColumn = SDoT_EnvInfo_Gyonggi.obs,
            otherColumn = SDoT_Location_Gyonggi.obs
        ).select(
                SDoT_EnvInfo_Gyonggi.sensing_time, SDoT_EnvInfo_Gyonggi.obs, SDoT_EnvInfo_Gyonggi.region,
                SDoT_Location_Gyonggi.addr, SDoT_Location_Gyonggi.lat, SDoT_Location_Gyonggi.lng,
                SDoT_EnvInfo_Gyonggi.so2, SDoT_EnvInfo_Gyonggi.co, SDoT_EnvInfo_Gyonggi.no2,
                SDoT_EnvInfo_Gyonggi.o3,
                SDoT_EnvInfo_Gyonggi.pm10, SDoT_EnvInfo_Gyonggi.pm25
            ).where { SDoT_EnvInfo_Gyonggi.sensing_time eq (gyonggiLastTime ?: "") }
            .map { resultRow ->
                toSDoTEnvInfoUnionFromSDoT_EnvInfoGyonggi(resultRow)
            }

        // 5. Union 실행 및 결과 매핑
        // Union을 위해 컬럼 순서와 개수를 동일하게 맞췄습니다.
        return@transaction seoulQuery + gyonggiQuery
    }

    fun fetchSDoTEnvInfoGyonggiFromDb():List<SDoTEnvInformationGyonggi> = transaction {
        LOGGER.info("Serving from DB for : fetchSDoTEnvInfoGyonggiFromDb")

        val lastTimeExpression = SDoT_EnvInfo_Gyonggi.sensing_time.max()

        val maxSensingTime = SDoT_EnvInfo_Gyonggi.select(lastTimeExpression).limit(1).map {
            it[lastTimeExpression]
        }.firstOrNull()

        println("가장 최근 sensing_time: $maxSensingTime")

        val result = SDoT_EnvInfo_Gyonggi.join(
            SDoT_Location_Gyonggi,
            JoinType.INNER,
            onColumn = SDoT_EnvInfo_Gyonggi.obs,
            otherColumn = SDoT_Location_Gyonggi.obs
        ).select(
            SDoT_EnvInfo_Gyonggi.sensing_time,
            SDoT_EnvInfo_Gyonggi.obs,  SDoT_EnvInfo_Gyonggi.region,
            SDoT_Location_Gyonggi.addr, SDoT_Location_Gyonggi.lat, SDoT_Location_Gyonggi.lng,
            SDoT_EnvInfo_Gyonggi.so2, SDoT_EnvInfo_Gyonggi.co, SDoT_EnvInfo_Gyonggi.no2,
            SDoT_EnvInfo_Gyonggi.o3, SDoT_EnvInfo_Gyonggi.pm10, SDoT_EnvInfo_Gyonggi.pm25,

        ).where{
            SDoT_EnvInfo_Gyonggi.sensing_time.eq(maxSensingTime ?: "")
        }.map { resultRow ->
            toSDoTEnvInformationGyonggi(resultRow)
        }

        return@transaction result
    }


    fun fetchSDoTEnvInfoFromDb():List<SDoTEnvInformation> = transaction {
        LOGGER.info("Serving from DB for : fetchSDoTEnvInfoFromDb")

        val lastTimeExpression = SDoT_EnvInfo.sensing_time.max()

        val maxSensingTime = SDoT_EnvInfo.select(lastTimeExpression).limit(1).map {
            it[lastTimeExpression]
        }.firstOrNull()

        println("가장 최근 sensing_time: $maxSensingTime")

        val result = SDoT_EnvInfo.join(
            SDoT_Location,
            JoinType.INNER,
            onColumn = SDoT_EnvInfo.serial,
            otherColumn = SDoT_Location.serial
        ).select(
            SDoT_EnvInfo.sensing_time,
            SDoT_EnvInfo.serial, SDoT_EnvInfo.region,
            SDoT_EnvInfo.autonomous_district, SDoT_EnvInfo.administrative_district,
            SDoT_Location.addr, SDoT_Location.lat, SDoT_Location.lng,
            SDoT_EnvInfo.max_temp, SDoT_EnvInfo.min_temp, SDoT_EnvInfo.avg_temp,
            SDoT_EnvInfo.max_humi, SDoT_EnvInfo.min_humi, SDoT_EnvInfo.avg_humi,
            SDoT_EnvInfo.max_ultra_rays, SDoT_EnvInfo.min_ultra_rays, SDoT_EnvInfo.avg_ultra_rays,
            SDoT_EnvInfo.max_noise, SDoT_EnvInfo.min_noise, SDoT_EnvInfo.avg_noise,
            SDoT_EnvInfo.max_vibr_x, SDoT_EnvInfo.min_vibr_x, SDoT_EnvInfo.avg_vibr_x,
            SDoT_EnvInfo.max_vibr_y, SDoT_EnvInfo.min_vibr_y, SDoT_EnvInfo.avg_vibr_y,
            SDoT_EnvInfo.max_vibr_z, SDoT_EnvInfo.min_vibr_z, SDoT_EnvInfo.avg_vibr_z,
            SDoT_EnvInfo.max_no2, SDoT_EnvInfo.min_no2, SDoT_EnvInfo.avg_no2,
            SDoT_EnvInfo.max_co, SDoT_EnvInfo.min_co, SDoT_EnvInfo.avg_co,
            SDoT_EnvInfo.max_so2, SDoT_EnvInfo.min_so2, SDoT_EnvInfo.avg_so2,
            SDoT_EnvInfo.max_nh3, SDoT_EnvInfo.min_nh3, SDoT_EnvInfo.avg_nh3,
            SDoT_EnvInfo.max_h2s, SDoT_EnvInfo.min_h2s, SDoT_EnvInfo.avg_h2s,
            SDoT_EnvInfo.max_o3, SDoT_EnvInfo.min_o3, SDoT_EnvInfo.avg_o3
        ).where{
            SDoT_EnvInfo.sensing_time.eq(maxSensingTime ?: "")
        }.map { resultRow ->
            toSDoTEnvInformation(resultRow)
        }

        return@transaction result
    }



    @OptIn(FormatStringsInDatetimeFormats::class)
    fun fetchSeaWaterInfoFromDb_Mof(division: String): List<SeaWaterInformation>  = transaction {
        LOGGER.info("Serving from DB for ID: $division")

        val result = when(division) {
            "mof_oneday" -> {
                val previous24Hour = kotlin.time.Clock.System.now()
                    .minus(24, DateTimeUnit.HOUR)
                    .toLocalDateTime(TimeZone.of("Asia/Seoul"))
                    .format(LocalDateTime.Format { byUnicodePattern("yyyy-MM-dd HH:mm:ss") })

                OWQInformationTable.join(
                    QWQObservatoryTable,
                    JoinType.INNER,
                    onColumn = OWQInformationTable.rtmWqWtchStaCd,
                    otherColumn = QWQObservatoryTable.sta_code
                ).select(
                    OWQInformationTable.rtmWqWtchDtlDt,
                    OWQInformationTable.rtmWqWtchStaCd,
                    QWQObservatoryTable.sta_name,
                    OWQInformationTable.rtmWtchWtem,
                    OWQInformationTable.rtmWqCndctv,
                    OWQInformationTable.ph,
                    OWQInformationTable.rtmWqDoxn,
                    OWQInformationTable.rtmWqTu,
                    OWQInformationTable.rtmWqChpla,
                    OWQInformationTable.rtmWqSlnty,
                    QWQObservatoryTable.lon,
                    QWQObservatoryTable.lat
                ).where {
                    OWQInformationTable.rtmWqWtchDtlDt greaterEq previous24Hour
                }
                    .orderBy(
                        OWQInformationTable.rtmWqWtchDtlDt to SortOrder.ASC,
                        QWQObservatoryTable.sta_name to SortOrder.ASC
                    )
                    .map {
                        toSeaWaterInformation(it)
                    }
            }
            else -> {emptyList()}
        }


        return@transaction result
    }

    @OptIn(FormatStringsInDatetimeFormats::class)
    fun fetchSeaWaterInfoFromDb(division: String): List<SeawaterInformationByObservationPoint>  = transaction {
        LOGGER.info("Serving from DB for ID: $division")
        val result = when(division) {
            "oneday" -> {
                val previous24Hour = kotlin.time.Clock.System.now()
                    .minus(24, DateTimeUnit.HOUR)
                    .toLocalDateTime(TimeZone.of("Asia/Seoul"))
                    .format(LocalDateTime.Format{byUnicodePattern("yyyy-MM-dd HH:mm:ss")})

                ObservationTable.join(
                    ObservatoryTable,
                    JoinType.INNER,
                    onColumn = ObservationTable.sta_cde,
                    otherColumn = ObservatoryTable.sta_cde
                ).select( ObservationTable.sta_cde,
                    ObservationTable.sta_nam_kor,
                    ObservationTable.obs_datetime,
                    ObservationTable.obs_lay,
                    ObservationTable.wtr_tmp,
                    ObservatoryTable.gru_nam,
                    ObservatoryTable.lon,
                    ObservatoryTable.lat

                ).where{
                    ObservationTable.obs_datetime greaterEq previous24Hour
                }
                    .orderBy(
                        ObservationTable.obs_datetime to SortOrder.ASC,
                        ObservatoryTable.gru_nam to SortOrder.ASC,
                        ObservatoryTable.sta_nam_kor to SortOrder.ASC,
                        ObservationTable.obs_lay to SortOrder.ASC
                    )
                    .map {
                        toSeawaterInformationByObservationPoint(it)
                    }
            }

            "grid" -> {
                val previous24Hour = kotlin.time.Clock.System.now()
                    .minus(24, DateTimeUnit.HOUR)
                    .toLocalDateTime(TimeZone.of("Asia/Seoul"))
                    .format(LocalDateTime.Format{byUnicodePattern("yyyy-MM-dd HH:mm:ss")})

                ObservationTable.join(
                    ObservatoryTable,
                    JoinType.INNER,
                    onColumn = ObservationTable.sta_cde,
                    otherColumn = ObservatoryTable.sta_cde
                ).select( ObservationTable.sta_cde,
                    ObservationTable.sta_nam_kor,
                    ObservationTable.obs_datetime,
                    ObservationTable.obs_lay,
                    ObservationTable.wtr_tmp,
                    ObservatoryTable.gru_nam,
                    ObservatoryTable.lon,
                    ObservatoryTable.lat

                ).where{
                    ObservationTable.obs_datetime greaterEq previous24Hour
                }.orderBy(
                    ObservationTable.obs_datetime to SortOrder.DESC,
                    ObservatoryTable.gru_nam to SortOrder.ASC,
                    ObservatoryTable.sta_nam_kor to SortOrder.ASC,
                    ObservationTable.obs_lay to SortOrder.ASC
                ).map {
                    toSeawaterInformationByObservationPoint(it)
                }
            }

            "current" -> {
                val lastTimeExpression = ObservationTable.obs_datetime.max()
                val lastTime = ObservationTable.select(lastTimeExpression).limit(1).map {
                    it[lastTimeExpression].toString()
                }.singleOrNull()

                if (lastTime == null) {
                    emptyList() // 현재 데이터가 없는 경우
                } else {
                    ObservationTable.join(
                        ObservatoryTable,
                        JoinType.INNER,
                        onColumn = ObservationTable.sta_cde,
                        otherColumn = ObservatoryTable.sta_cde
                    ).select( ObservationTable.sta_cde,
                        ObservationTable.sta_nam_kor,
                        ObservationTable.obs_datetime,
                        ObservationTable.obs_lay,
                        ObservationTable.wtr_tmp,
                        ObservatoryTable.gru_nam,
                        ObservatoryTable.lon,
                        ObservatoryTable.lat

                    ).where{
                        ObservationTable.obs_datetime eq lastTime
                    }.orderBy(
                        ObservatoryTable.sta_nam_kor to SortOrder.ASC,
                        ObservationTable.obs_lay to SortOrder.ASC
                    )
                        .map {
                            toSeawaterInformationByObservationPoint(it)
                        }
                }

            }

            else -> {emptyList()}
        }
        return@transaction result
    }


    @OptIn(FormatStringsInDatetimeFormats::class)
    fun fetchSeaWaterInfoOneDayBoxPlotFromDb(): List<SeaWaterBoxPlotStat>  = transaction {

        val previous24Hour =
            kotlin.time.Clock.System.now()
                .minus(24, DateTimeUnit.HOUR)
                .toLocalDateTime(TimeZone.of("Asia/Seoul"))
                .format(LocalDateTime.Format{byUnicodePattern("yyyy-MM-dd HH:mm:ss")})

        val rawRecords = ObservationTable.join(
            ObservatoryTable,
            JoinType.INNER,
            onColumn = ObservationTable.sta_cde,
            otherColumn = ObservatoryTable.sta_cde
        ).select( ObservationTable.sta_cde,
            ObservationTable.sta_nam_kor,
            ObservationTable.obs_datetime,
            ObservationTable.obs_lay,
            ObservationTable.wtr_tmp,
            ObservatoryTable.gru_nam,
        ).where{
            (ObservationTable.obs_datetime greaterEq previous24Hour) and
                    (ObservationTable.obs_lay eq "1")
        }.map {
            // (그룹명, 관측소명, 수온) Triple로 변환
            Triple(
                it[ObservatoryTable.gru_nam],
                it[ObservationTable.sta_nam_kor],
                it[ObservationTable.wtr_tmp].trim().toFloatOrNull() ?: 0f
            )
        }

        // 2. 관측소별로 그룹화하여 통계 계산
        val result = rawRecords.groupBy { it.first to it.second } // Pair(gru_nam, sta_nam_kor) 기준
            .map { (key, values) ->
                val temps = values.map { it.third }.sorted() // 오름차순 정렬
                val n = temps.size

                if (n == 0) return@map SeaWaterBoxPlotStat(
                    key.first,
                    key.second,
                    0f,
                    0f,
                    0f,
                    0f,
                    0f
                )

                // 사분위수 계산 (단순 인덱스 방식)
                val q1 = temps[n / 4]
                val median = temps[n / 2]
                val q3 = temps[n * 3 / 4]

                // 이상치(Outlier) 계산 로직
                val iqr = q3 - q1
                val lowerFence = q1 - (1.5f * iqr)
                val upperFence = q3 + (1.5f * iqr)

                // Fence 내부에 있는 값들 중 실제 최소/최대값 결정 (Whiskers 끝점)
                val actualMin = temps.firstOrNull { it >= lowerFence } ?: temps.first()
                val actualMax = temps.lastOrNull { it <= upperFence } ?: temps.last()

                // Fence를 벗어나는 값들을 이상치로 추출
                val outliers = temps.filter { it !in lowerFence..upperFence }

                SeaWaterBoxPlotStat(
                    gruNam = key.first,
                    staName = key.second,
                    min = actualMin,
                    q1 = q1,
                    median = median,
                    q3 = q3,
                    max = actualMax,
                    outliers = outliers
                )
            }

        return@transaction result

    }

    fun seaWaterInfoStatistics(): List<SeaWaterInfoByOneHourStat>{
        val key = "cache_stat"
        val now = System.currentTimeMillis()
        cacheStorage_SeaWaterInfoStatistics[key]?.let { it ->
            if( (now - it.second) < TimeUnit.SECONDS.toMillis(CACHE_EXPIRY_SECONDS) ){
                LOGGER.info("Serving from cache for ID: stat")
                return it.first
            }
        }

        val resultFromDb = fetchSeaWaterInfoStatisticsFromDb()
        if (resultFromDb.isNotEmpty()) {
            cacheStorage_SeaWaterInfoStatistics[key] = Pair(resultFromDb, now)
        }
        return resultFromDb


    }

    @OptIn(FormatStringsInDatetimeFormats::class)
    fun fetchSeaWaterInfoStatisticsFromDb(): List<SeaWaterInfoByOneHourStat>  = transaction {

        LOGGER.info("Serving from DB for ID: stat")
        val previous24Hour = kotlin.time.Clock.System.now()
            .minus(24, DateTimeUnit.HOUR)
            .toLocalDateTime(TimeZone.of("Asia/Seoul"))
            .format(LocalDateTime.Format{byUnicodePattern("yyyy-MM-dd HH:mm:ss")})

        val time = ObservationTable.obs_tim.substring(0,3)
        val datetime = ObservationTable.obs_datetime.min().substring(3, 11)
        val tmp_min = ObservationTable.wtr_tmp.castTo(FloatColumnType()).min()
        val tmp_max = ObservationTable.wtr_tmp.castTo(FloatColumnType()).max()
        val tmp_avg = ObservationTable.wtr_tmp.castTo(FloatColumnType()).avg()

        val result = ObservationTable
            .join(
                ObservatoryTable,
                JoinType.INNER,
                onColumn = ObservationTable.sta_cde,
                otherColumn = ObservatoryTable.sta_cde
            )
            .select(
                ObservatoryTable.gru_nam,
                ObservationTable.sta_cde,
                ObservationTable.sta_nam_kor,
                datetime,
                tmp_min,
                tmp_max,
                tmp_avg
            )
            .where { (ObservationTable.obs_datetime greaterEq previous24Hour) and (ObservationTable.obs_lay eq "1")}
            .groupBy ( ObservatoryTable.gru_nam, ObservationTable.sta_cde , ObservationTable.sta_nam_kor, time)
            .orderBy( ObservatoryTable.gru_nam to SortOrder.ASC, ObservationTable.sta_nam_kor to SortOrder.ASC , datetime to SortOrder.ASC  )
            .map {
                SeaWaterInfoByOneHourStat(
                    it[ObservatoryTable.gru_nam],
                    it[ObservationTable.sta_cde],
                    it[ObservationTable.sta_nam_kor],
                    it[datetime],
                    it[tmp_min].toString(),
                    it[tmp_max].toString(),
                    it[tmp_avg].toString()
                )
            }
        return@transaction result
    }

    fun fetchKhoaObservatoryFromDb():List<KhonObservatory> = transaction {
        LOGGER.info("Serving from DB for : fetchKhoaObservatoryFromDb")
        val result = ObservatoryKHOA.select(
                ObservatoryKHOA.obsCode,
                ObservatoryKHOA.obsvtrNm,
                ObservatoryKHOA.latitude,
                ObservatoryKHOA.longitude
        ).where { ObservatoryKHOA.obsCode like("HB%")}
        .map {
            KhonObservatory(
                it[ObservatoryKHOA.obsCode],
                it[ObservatoryKHOA.obsvtrNm],
                it[ObservatoryKHOA.longitude],
                it[ObservatoryKHOA.latitude],
            )
        }
        return@transaction result
    }

    @OptIn(ExperimentalTime::class, FormatStringsInDatetimeFormats::class)
    fun fetchKhoaTidalCurrentInfoFromDb():List<TidalCurrentInfo> = transaction  {
        LOGGER.info("Serving from DB for : fetchKhoaTidalCurrentInfoFromDb")

        val now = kotlin.time.Clock.System.now()

        val preTime = now.minus(5, DateTimeUnit.MINUTE)
            .toLocalDateTime(TimeZone.of("Asia/Seoul"))
            .format(LocalDateTime.Format { byUnicodePattern("yyyy-MM-dd HH:mm") })

        val result = TidalCurrentInfoKHOA.selectAll().where{
            TidalCurrentInfoKHOA.sch_time greaterEq  preTime
        }.orderBy(TidalCurrentInfoKHOA.sch_time, SortOrder.ASC).map {
            toTidalCurrentInfo(it)
        }

        return@transaction result
    }


    @OptIn(FormatStringsInDatetimeFormats::class)
    fun fetchKhoaObservationCurrentFromDb():List<KhoaObservation> = transaction {
        LOGGER.info("Serving from DB for : fetchKhoaObservationCurrentFromDb")


        val maxDt = ObservationKHOA
            .selectAll()
            .orderBy(ObservationKHOA.obsrvnDt to SortOrder.DESC) // 날짜 내림차순 정렬
            .limit(1)                                            // 제일 위 하나만
            .singleOrNull()
            ?.get(ObservationKHOA.obsrvnDt) ?: ""                   // 해당 날짜 추출

        val result = ObservationKHOA
            .join(
                ObservatoryKHOA,
                JoinType.INNER,
                onColumn = ObservationKHOA.obsCode,
                ObservatoryKHOA.obsCode
            ).select(
                ObservatoryKHOA.obsCode,
                ObservatoryKHOA.obsvtrNm,
                ObservatoryKHOA.latitude,
                ObservatoryKHOA.longitude,
                ObservationKHOA.obsrvnDt,
                ObservationKHOA.wndrct,
                ObservationKHOA.wspd,
                ObservationKHOA.maxMmntWspd,
                ObservationKHOA.artmp,
                ObservationKHOA.atmpr,
                ObservationKHOA.wvhgt,
                ObservationKHOA.wvpd,
                ObservationKHOA.crdir,
                ObservationKHOA.crsp,
                ObservationKHOA.wtem,
                ObservationKHOA.slnty
            ).where{
                 ObservationKHOA.obsrvnDt eq maxDt
            }.map{
                KhoaObservation(
                    it[ObservatoryKHOA.obsCode],
                    it[ObservatoryKHOA.obsvtrNm],
                    it[ObservatoryKHOA.longitude],
                    it[ObservatoryKHOA.latitude],
                    it[ObservationKHOA.obsrvnDt],
                    it[ObservationKHOA.wndrct],
                    it[ObservationKHOA.wspd],
                    it[ObservationKHOA.maxMmntWspd],
                    it[ObservationKHOA.artmp],
                    it[ObservationKHOA.atmpr],
                    it[ObservationKHOA.wvhgt],
                    it[ObservationKHOA.wvpd],
                    it[ObservationKHOA.crdir],
                    it[ObservationKHOA.crsp],
                    it[ObservationKHOA.wtem],
                    it[ObservationKHOA.slnty]
                )
            }

        return@transaction result
    }



    @OptIn(FormatStringsInDatetimeFormats::class)
    fun fetchKhoaObservationFromDb():List<KhoaObservation> = transaction {
        LOGGER.info("Serving from DB for : fetchKhoaObservationFromDb")

        //2026-03-06 13:59
        val previous24Hour = kotlin.time.Clock.System.now()
            .minus(24, DateTimeUnit.HOUR)
            .toLocalDateTime(TimeZone.of("Asia/Seoul"))
            .format(LocalDateTime.Format{byUnicodePattern("yyyy-MM-dd HH:mm")})


        val maxDt = ObservationKHOA
            .selectAll()
            .orderBy(ObservationKHOA.obsrvnDt to SortOrder.DESC) // 날짜 내림차순 정렬
            .limit(1)                                            // 제일 위 하나만
            .singleOrNull()
            ?.get(ObservationKHOA.obsrvnDt) ?: ""                   // 해당 날짜 추출

        val result = ObservationKHOA
            .join(
                ObservatoryKHOA,
                JoinType.INNER,
                onColumn = ObservationKHOA.obsCode,
                ObservatoryKHOA.obsCode
            ).select(
                ObservatoryKHOA.obsCode,
                ObservatoryKHOA.obsvtrNm,
                ObservatoryKHOA.latitude,
                ObservatoryKHOA.longitude,
                ObservationKHOA.obsrvnDt,
                ObservationKHOA.wndrct,
                ObservationKHOA.wspd,
                ObservationKHOA.maxMmntWspd,
                ObservationKHOA.artmp,
                ObservationKHOA.atmpr,
                ObservationKHOA.wvhgt,
                ObservationKHOA.wvpd,
                ObservationKHOA.crdir,
                ObservationKHOA.crsp,
                ObservationKHOA.wtem,
                ObservationKHOA.slnty
            ).where{
               // ObservationKHOA.obsrvnDt eq maxDt
                ObservationKHOA.obsrvnDt greaterEq previous24Hour
            }.map{
                KhoaObservation(
                    it[ObservatoryKHOA.obsCode],
                    it[ObservatoryKHOA.obsvtrNm],
                    it[ObservatoryKHOA.longitude],
                    it[ObservatoryKHOA.latitude],
                    it[ObservationKHOA.obsrvnDt],
                    it[ObservationKHOA.wndrct],
                    it[ObservationKHOA.wspd],
                    it[ObservationKHOA.maxMmntWspd],
                    it[ObservationKHOA.artmp],
                    it[ObservationKHOA.atmpr],
                    it[ObservationKHOA.wvhgt],
                    it[ObservationKHOA.wvpd],
                    it[ObservationKHOA.crdir],
                    it[ObservationKHOA.crsp],
                    it[ObservationKHOA.wtem],
                    it[ObservationKHOA.slnty]
                )
            }

        return@transaction result
    }



    fun observatoryInfo(): List<Observatory> = transaction {
        ObservatoryTable.selectAll()
            .map {
                toObservatory(it)
            }
    }

}