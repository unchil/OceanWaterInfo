package com.unchil.oceanwaterinfo


import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.minus
import kotlinx.datetime.toDeprecatedInstant
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toStdlibInstant
import org.jetbrains.exposed.v1.core.FloatColumnType
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.avg
import org.jetbrains.exposed.v1.core.castTo
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.max
import org.jetbrains.exposed.v1.core.min
import org.jetbrains.exposed.v1.core.substring
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit



// 캐시를 저장할 ConcurrentHashMap. 스레드 안전성을 보장합니다.
private val cacheStorage_SeawaterInfo = ConcurrentHashMap<String, Pair<List<SeawaterInformationByObservationPoint>, Long>>()

private val cacheStorage_SeawaterInfo_Mof = ConcurrentHashMap<String, Pair<List<SeaWaterInformation>, Long>>()
private val cacheStorage_SeaWaterInfoStatistics = ConcurrentHashMap<String, Pair<List<SeaWaterInfoByOneHourStat>, Long>>()

private val cacheStorage_SeaWaterInfoBoxPlot = ConcurrentHashMap<String, Pair<List<SeaWaterBoxPlotStat>, Long>>()
private const val CACHE_EXPIRY_SECONDS =  1 * 60L  // 10분



class Repository:RepositoryInterface {


    suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
        newSuspendedTransaction(Dispatchers.IO, statement = block)

    // 캐시 로직과 DB 조회 호출을 담당하는 메인 함수
    suspend fun seaWaterInfo(division: String): List<SeawaterInformationByObservationPoint> {
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

    suspend fun swi(division:String):List<SeaWaterInformation?>{
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


    suspend fun seaWaterInfoOneDayBoxPlot(division:String):List<SeaWaterBoxPlotStat?>{
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


    @OptIn(FormatStringsInDatetimeFormats::class)
    suspend fun fetchSeaWaterInfoFromDb_Mof(division: String): List<SeaWaterInformation>  = suspendTransaction {
        LOGGER.info("Serving from DB for ID: $division")

        val result = when(division) {
            "mof_oneday" -> {
                val previous24Hour = Clock.System.now()
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


        return@suspendTransaction result
    }

    @OptIn(FormatStringsInDatetimeFormats::class)
    override suspend fun fetchSeaWaterInfoFromDb(division: String): List<SeawaterInformationByObservationPoint>  = suspendTransaction {
        LOGGER.info("Serving from DB for ID: $division")
        val result = when(division) {
            "oneday" -> {
                val previous24Hour = Clock.System.now()
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
                val previous24Hour = Clock.System.now()
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
        return@suspendTransaction result
    }


    @OptIn(FormatStringsInDatetimeFormats::class)
    suspend fun fetchSeaWaterInfoOneDayBoxPlotFromDb(): List<SeaWaterBoxPlotStat>  = suspendTransaction {

        val previous24Hour =
            kotlin.time.Clock.System.now()
                .minus(24, DateTimeUnit.HOUR)
                .toLocalDateTime(TimeZone.UTC)
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

        return@suspendTransaction result

    }

    suspend fun seaWaterInfoStatistics(): List<SeaWaterInfoByOneHourStat>{
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
    override suspend fun fetchSeaWaterInfoStatisticsFromDb(): List<SeaWaterInfoByOneHourStat>  = suspendTransaction {

        LOGGER.info("Serving from DB for ID: stat")
        val previous24Hour = Clock.System.now()
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
                    it[datetime].toString(),
                    it[tmp_min].toString(),
                    it[tmp_max].toString(),
                    it[tmp_avg].toString()
                )
            }
        return@suspendTransaction result
    }


    override suspend fun observatoryInfo(): List<Observatory> = suspendTransaction {
        ObservatoryTable.selectAll()
            .map {
                toObservatory(it)
            }
    }

}