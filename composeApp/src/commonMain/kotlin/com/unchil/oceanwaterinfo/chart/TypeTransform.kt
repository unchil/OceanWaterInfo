package com.unchil.oceanwaterinfo



import io.github.koalaplot.core.xygraph.Point
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.collections.mapIndexed
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.round


fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return kotlin.math.round(this * multiplier) / multiplier
}

// 또는 확장 프로퍼티로 만들어 사용하면 편리합니다.
// 공식: radian = degree * (PI / 180)
val Double.toRadians get() = this * (PI / 180.0)
val Double.toDegrees get() = this * (180.0 / PI)


fun List<TidalCurrentInfo>.toTidalCurrentDataMap():Map<Pair<Double, Double>, List<TidalCurrentData>>{

    return this.groupBy(
        // Key: 위경도 쌍 (pre_lon, pre_lat)
        keySelector = {  it.pre_lat to it.pre_lon},

        valueTransform = { item ->

            TidalCurrentData(
                schTime = item.sch_time,
                currentDir = item.current_dir,
                currentSpeed = item.current_speed,
                prev_lat = item.pre_lat,
                prev_lon = item.pre_lon
            )
        }
    )
}

/**
 * TidalCurrentData Map을 Deck.gl HexagonLayer용 리스트로 변환
 * 각 좌표별로 가장 이른 시간(schTime 최소값)의 데이터만 포함
 */
fun transformToHexagonData(
    tidalCurrentData: Map<Pair<Double, Double>, List<TidalCurrentData>>
): List<Triple<Double, Double, Double>> {
    return tidalCurrentData.mapNotNull { (coords, dataList) ->
        // 1. 리스트 내에서 schTime이 가장 빠른 항목을 찾음
        val firstEntry = dataList.minByOrNull { it.schTime }

        // 2. 항목이 존재할 경우 Map 형태로 변환 (JS/JSON 전달용)
        firstEntry?.let {
            Triple(
                 coords.first,    // Pair의 첫 번째 값 (lat)
                 coords.second,   // Pair의 두 번째 값 (lon)
                 it.currentSpeed
            )
        }
    }
}

fun updatePrevCoordinates(
    result: Map<Pair<Double, Double>, List<TidalCurrentData>>,
    timeIntervalSeconds: Double = 300.0 // 5분 간격 기준
) {
    val earthRadius = 6371000.0 // m

    result.forEach { (coords, dataList) ->

        dataList.forEachIndexed { index, data ->

            if(index > 0 ){
                // 유속 단위를 m/s로 변환 (cm/s인 경우 0.01, knots인 경우 0.51444)
                val speedMps = dataList[index-1].currentSpeed * 0.01 // KHOA cm/s 기준
           //     val distance = speedMps * timeIntervalSeconds * index
                val distance = speedMps * timeIntervalSeconds
                val lat1 = dataList[index-1].prev_lat.toRadians
                val lon1 = dataList[index-1].prev_lon.toRadians
                val brng = dataList[index-1].currentDir.toRadians


                val lat2 = asin(
                    sin(lat1) * cos(distance / earthRadius) +
                            cos(lat1) * sin(distance / earthRadius) * cos(brng)
                )

                val lon2 = lon1 + atan2(
                    sin(brng) * sin(distance / earthRadius) * cos(lat1),
                    cos(distance / earthRadius) - sin(lat1) * sin(lat2)
                )

                //  결과 세팅 (소수점 6자리까지 반올림 - 위경도는 정밀도가 중요하므로 6자리 권장)
                data.prev_lat = (lat2.toDegrees * 1000000.0).round(6) / 1000000.0
                data.prev_lon = (lon2.toDegrees * 1000000.0).round(6) / 1000000.0


            }

        }
    }
}


@OptIn(FormatStringsInDatetimeFormats::class)
fun formatLongToDateTime(millis: Any): String {

    // 1. Long을 Instant로 변환
    val instant = kotlin.time.Instant.fromEpochMilliseconds(   (millis as Double).toLong())

    // 2. 시스템 기본 시간대(TimeZone)를 적용하여 LocalDateTime으로 변환
    val localDateTime = instant.toLocalDateTime(TimeZone.UTC)

    // 3. 사용자 정의 포맷 정의
    val myFormat = LocalDateTime.Format {
        byUnicodePattern("yy/MM/dd HH:mm")
    }

    // 4. 포맷팅 실행
    return myFormat.format(localDateTime)
}

fun List<Point<Double, Double>>.getRange(
    boundValue:Double = 0.5
):Pair<ClosedFloatingPointRange<Double> , ClosedFloatingPointRange<Double>>{
    if(this.isEmpty()) return Pair(0.0..0.1, 0.0..0.1)

    val xMax = this.maxOf { it.x }
    val xMin = this.minOf { it.x }
    val yMax = this.maxOf { it.y }
    val yMin = this.minOf { it.y }

    return Pair(xMin-boundValue..xMax+boundValue, yMin-boundValue..yMax+boundValue )
}



fun List<SeawaterInformationByObservationPoint>.toBarChartTripleList(): ChartDataListStringFloat{


    val columns = this.first().makeGridColumns()
    val rows = this.map { it.toGridData() }

    // 1. 필요한 컬럼의 인덱스를 찾습니다.
    val obsIndex = columns.indexOf("Observatory")
    val tempIndex = columns.indexOf("WaterTemperature")
    // val timeIndex = columns.indexOf("Collection Time")

    // 2. 인덱스가 유효한지 확인 후 데이터 추출
    if (obsIndex != -1 && tempIndex != -1 ) {
        val entries =  rows.map { it[obsIndex].toString() }
        val values =  rows.map { it[tempIndex].toString().trim().toFloatOrNull() ?: 0f }

        return entries.mapIndexed { index, observatory ->
            Triple(observatory, Point(observatory, values[index]), emptyMap())
        }
    } else {
        return emptyList()
    }

}


fun<T>List<T>.toBarChartTripleList(
    nameSelector: (T) -> String,
    primaryValueSelector: (T) -> Float,
    secondaryValueSelector:(T) -> Triple<String,String, String>
):ChartDataListStringFloat{
    return this.map {
        val name  = nameSelector(it)
        Triple(
            name,
            Point( name , primaryValueSelector(it) ),
            mapOf( "info" to secondaryValueSelector(it))
        )
    }
}



/**
 * 다양한 데이터 리스트를 차트용 Triple 리스트로 변환하는 범용 함수
 */
@OptIn(FormatStringsInDatetimeFormats::class)
fun <T> List<T>.toChartTripleList(
    nameSelector: (T) -> String,          // Entry.Key (발전소/관측소) 이름 추출
    timeSelector: (T) -> String,          // 시간 문자열 추출
    timePattern: String,                  // 시간 포맷 패턴
    primaryValueSelector: (T) -> Float,   // 메인 Y축 값 (예: 수온, PH)
    secondaryValueSelector: ((T) -> Float)? = null, // 보조 Y축 값 (선택 사항, 예: 유량)
    secondaryKey: String = ""             // 보조 데이터의 Map Key 이름
): ChartDataList {

    val inputFormat = LocalDateTime.Format { byUnicodePattern(timePattern) }

    // 1. 공통 데이터 전처리
    val processedData = this.map { item ->
        val timeX = LocalDateTime.parse(timeSelector(item), inputFormat)
            .toInstant(TimeZone.UTC).toEpochMilliseconds().toDouble()

        val val1 = (round(primaryValueSelector(item) * 10) / 10f)
        val val2 = secondaryValueSelector?.invoke(item)?.let { (round(it * 10) / 10f) }

        nameSelector(item) to (timeX to (val1 to val2))
    }.sortedBy { it.second.first } // 시간순 정렬 (결측치 보정을 위해 필수)

    // 2. 전체 고유 시간축 추출 및 정렬
    val allXValues = processedData.map { it.second.first }.distinct().sorted()

    // 3. 그룹화 및 결측치 보정 (Forward Fill 또는 0f)
    return processedData.groupBy({ it.first }, { it.second })
        .map { (name, timeValues) ->
            val timeMap = timeValues.toMap()

            // Forward Fill용 변수 초기화
            var lastValidVal1 = 0f
            var lastValidVal2 = 0f

            // 모든 시간 지점을 순회하며 보정
            val primaryPoints = allXValues.map { x ->
                val current = timeMap[x]
                if (current != null) {
                    lastValidVal1 = current.first
                    Point(x, current.first)
                } else {
                    Point(x, lastValidVal1) // 데이터 없으면 이전 값 사용
                }
            }

            val extraData = if (secondaryValueSelector != null && secondaryKey.isNotBlank()) {
                // 두 번째 값에 대해서도 동일하게 Forward Fill 수행
                val secondaryPoints = allXValues.map { x ->
                    val current = timeMap[x]
                    if (current != null && current.second != null) {
                        lastValidVal2 = current.second!!
                        Point(x, current.second!!)
                    } else {
                        Point(x, lastValidVal2)
                    }
                }
                mapOf(secondaryKey to secondaryPoints)
            } else {
                emptyMap<String, Any>()
            }

            Triple(name, primaryPoints, extraData)
        }
}




fun Pair<List<String>, List<List<Any>>>.toMap():MutableMap<String, List<Any>>{
    val result = mutableMapOf<String, List<Any>>()
    if(first.size == second.first().size) {
        first.forEachIndexed { index, string ->
            result.putAll(mapOf(string to second.map { it -> it[index] }.toList()) )
        }
    }
    return result
}


// TypeTransform.kt 또는 적절한 위치에 추가
fun List<SeawaterInformationByObservationPoint>.toGridDataMap(): Map<String, List<Any>> {
    if (this.isEmpty()) return mutableMapOf()

    // 첫 번째 아이템에서 컬럼 이름을 추출
    val columns = this.first().makeGridColumns()
    // 모든 데이터를 리스트의 리스트 형태로 변환
    val rows = this.map { it.toGridData() }

    // Pair(컬럼 리스트, 로우 리스트).toMap() 호출
    return (columns to rows).toMap()
}





fun List<SeawaterInformationByObservationPoint>.toBoxPlotMap(): Map<String, SeaWaterBoxPlotStat> {
    return  this.map {
        Pair(
            it.sta_nam_kor,
            it.wtr_tmp.trim().toFloatOrNull() ?: 0f
        )
    }.groupBy { it.first  } // Pair(gru_nam, sta_nam_kor) 기준
        .mapValues { (key, values) ->
            val temps = values.map { it.second }.sorted() // 오름차순 정렬
            val n = temps.size
            if (n == 0) return emptyMap()
            // 사분위수 계산 (단순 인덱스 방식)
            val q1 = temps[n / 4] // 25% 지점인 1사분위수(Q1)
            val median = temps[n / 2] // 50% 지점인 중앙값(Q2)
            val q3 = temps[n * 3 / 4] // 75% 지점인 3사분위수(Q3)

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
                gruNam = "",
                staName = key,
                min = actualMin,
                q1 = q1,
                median = median,
                q3 = q3,
                max = actualMax,
                outliers = outliers
            )
        }

}




fun SeaWaterInformation.makeGridColumns():List<String>{
    val columns = mutableListOf<String>()

    columns.add("Collection Time")
    columns.add("Observation Point")
    columns.add("Water Temperature")
    columns.add("Hydrogen Ion Concentration")
    columns.add("Dissolved Oxygen")
    columns.add("Turbidity")
    columns.add("Chlorophyll")
    columns.add("Salinity")

    return columns
}

fun SeaWaterInformation.toGridData():List<Any?>{
    val data = mutableListOf<Any?>()

    data.add(this.rtmWqWtchDtlDt)
    data.add(this.rtmWqWtchStaName)
    data.add(this.rtmWtchWtem.toFloat())
    data.add(this.ph.toFloat())
    data.add(this.rtmWqDoxn.toFloat())
    data.add(  if(this.rtmWqTu.isBlank() ) -1 else  this.rtmWqTu.toInt() )
    data.add(  if(this.rtmWqChpla.isBlank() ) -999f else  this.rtmWqChpla.toFloat() )
    data.add(this.rtmWqSlnty.toFloat())

    return data
}

fun SeawaterInformationByObservationPoint.makeGridColumns():List<String>{
    val columns = mutableListOf<String>()
    columns.add("Collection Time")
    columns.add("SeaArea")
    columns.add("ObservatoryCode")
    columns.add("Observatory")
    columns.add("ObservationLayer")
    columns.add("WaterTemperature")
    columns.add("Longitude")
    columns.add("Latitude")
    return columns
}

fun SeawaterInformationByObservationPoint.toGridData():List<Any>{

    val data = mutableListOf<Any>()
    data.add(this.obs_datetime)
    data.add(this.gru_nam)
    data.add(this.sta_cde)
    data.add(this.sta_nam_kor)
    data.add(when(this.obs_lay){
        "1" -> "Surface"
        "2" -> "Middle"
        "3" -> "Deep"
        else -> ""
    })
    data.add( if(this.wtr_tmp.isBlank() ) 0f else  this.wtr_tmp.toFloat())
    data.add(this.lon)
    data.add(this.lat)
    return data
}

