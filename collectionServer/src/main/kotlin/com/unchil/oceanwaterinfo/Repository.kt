package com.unchil.oceanwaterinfo

import com.unchil.oceanwaterinfo.Config.Companion.configData
import io.ktor.client.statement.bodyAsText
import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.replace
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.upsert
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.count
import org.jetbrains.kotlinx.dataframe.api.forEach
import org.jetbrains.kotlinx.dataframe.io.readJson
import org.json.XML
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.Clock

class Repository {
    internal val LOGGER = KtorSimpleLogger( Repository::class.java.name )

    init {
        transaction(Config.conn) {
            addLogger(StdOutSqlLogger)
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
                RestApi.callKhoaAPI_json(url).let {

                    val response = Json.decodeFromString<KhonTidalCurrentInfoResponse>(it)

                    LOGGER.info("${::getKhoaTidalCurrent.name} [receive count[${response.result.meta.sch_time}]]")

                    transaction(Config.conn) {
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


             val result = transaction(Config.conn) {
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
                            RestApi.callKhoaAPI_json(pageUrl).let {
                             val recvData = Json.decodeFromString<KhoaObservationResponse>(it)

                             if(recvData.header.resultCode.equals("00")) {
                             //    requestPage += 1

                                 LOGGER.info("${::getKhoaObservation.name} [receive count[${recvData.body.totalCount}]]")

                                 transaction(Config.conn) {
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
            RestApi.callMofAPI_xml().let { response ->
                if(response.status.value == 200){
                    XML.toJSONObject(response.bodyAsText()).let { jsonData ->
                        val df = DataFrame.readJson(jsonData.toString().byteInputStream())
                        val result = df.get("response").get("body").get("items").get("item")[0] as DataFrame<*>

                        LOGGER.info( "${::getRealTimeOceanWaterQuality.name} [receive count[${result.count()}]]")

                        transaction (Config.conn){
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
            RestApi.callNifsAPI_json("list").let {
                val recvData = Json.decodeFromString<ObservationResponse>(it)
                if(recvData.header.resultCode.equals("00")){
                    LOGGER.info( "${::getRealTimeObservation.name} [receive count[${recvData.body.item.size}]]")
                    transaction (Config.conn){

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
            RestApi.callNifsAPI_json("code").let {
                val recvData = Json.decodeFromString<ObservatoryResponse>(it)
                if(recvData.header.resultCode.equals("00")) {

                    LOGGER.info( "${::getRealTimeObservatory.name} [receive count[${recvData.body.item.size}]]")

                    transaction (Config.conn){
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