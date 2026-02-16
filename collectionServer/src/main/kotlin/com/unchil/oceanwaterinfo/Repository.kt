package com.unchil.oceanwaterinfo

import io.ktor.client.statement.bodyAsText
import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.count
import org.jetbrains.kotlinx.dataframe.api.forEach
import org.jetbrains.kotlinx.dataframe.io.readJson
import org.json.XML

class Repository {


    internal val LOGGER = KtorSimpleLogger( Repository::class.java.name )

    init {
        transaction(Config.conn) {
            addLogger(StdOutSqlLogger)
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
                                    OWQInformationTable.insert { it ->

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
                                    ObservationTable.insert { it ->
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
                                ObservatoryTable.insert { it ->
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