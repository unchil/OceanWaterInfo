package com.unchil.oceanwaterinfo

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.colsOf
import org.jetbrains.kotlinx.dataframe.api.concat
import org.jetbrains.kotlinx.dataframe.api.convert
import org.jetbrains.kotlinx.dataframe.api.describe
import org.jetbrains.kotlinx.dataframe.api.forEach
import org.jetbrains.kotlinx.dataframe.api.head
import org.jetbrains.kotlinx.dataframe.api.schema
import org.jetbrains.kotlinx.dataframe.api.with
import org.jetbrains.kotlinx.dataframe.io.read
import org.jetbrains.kotlinx.dataframe.io.readJson
import org.jetbrains.kotlinx.dataframe.io.toCsvStr
import org.json.XML
import java.io.InputStreamReader
import java.net.URLEncoder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.sql.Connection
import java.sql.DriverManager

import kotlin.use

class RecoveryCollector {


    fun getRealTimeObservation( ){


        val urlString = makeUrl(::getRealTimeObservation.name)
        val response = java.net.URI(urlString).toURL().openStream().use { inputStream ->
            InputStreamReader(inputStream, Charset.forName("EUC-KR")).readText()
        }
        LOGGER.debug("\n"+ response)

        val recvData = DataFrame.readJson(response.byteInputStream())

        LOGGER.debug("\n"+ recvData.schema().toString())

        val df = recvData.get("body").get("item").get(0) as AnyFrame

        LOGGER.debug("\n"+ df.schema().toString())
        LOGGER.debug("\n"+ df.describe().toString())
        LOGGER.info("\n"+ df.head(5).toString())

        val tableName = "Observation"

        try {
            DriverManager.getConnection(Config.url).use { conn ->

                val sql = """INSERT INTO ${tableName} ( 
                    sta_cde, sta_nam_kor, obs_dat, obs_tim, obs_datetime,
                    repair_gbn, obs_lay, wtr_tmp
                ) VALUES (?,?,?,?,?,?,?,? )""".trimIndent()

                LOGGER.debug("\n"+ sql)

                df.forEach { it ->

                    it["wtr_tmp"]?.let{tmp ->
                        try {
                            conn.prepareStatement(sql)?.use { preparedStatement ->
                                preparedStatement.setString(1, it["sta_cde"].toString())
                                preparedStatement.setString(2, it["sta_nam_kor"].toString())
                                preparedStatement.setString(3, it["obs_dat"].toString())
                                preparedStatement.setString(4, it["obs_tim"].toString())
                                preparedStatement.setString(5, "${it["obs_dat"].toString()} ${it["obs_tim"].toString()}")
                                preparedStatement.setString(6, it["repair_gbn"].toString())
                                preparedStatement.setString(7, it["obs_lay"].toString())
                                preparedStatement.setString(8, tmp.toString())
                                preparedStatement.executeUpdate()
                            }
                        } catch (e: Exception){
                            LOGGER.debug(e.localizedMessage)
                        }

                    }

                }

            }
        } catch (e: Exception){
            LOGGER.error(e.localizedMessage)
        }

    }

    fun getRealTimeObservatory(){

        val urlString = makeUrl(::getRealTimeObservatory.name)
        val response = java.net.URI(urlString).toURL().openStream().use { inputStream ->
            InputStreamReader(inputStream, Charset.forName("EUC-KR")).readText()
        }

        LOGGER.debug("\n"+ response)

        var df = DataFrame.readJson(response.byteInputStream())

        LOGGER.debug("\n"+ df.schema().toString())

        df = df.get("body").get("item").get(0) as AnyFrame

        LOGGER.debug("\n"+ df.schema().toString())
        LOGGER.debug("\n"+ df.describe().toString())
        LOGGER.info("\n"+ df.head(5).toString())

        val tableName = "Observatory"


        try {
            DriverManager.getConnection(Config.url).use { conn ->

                val sql = """INSERT INTO ${tableName} ( 
                    sta_cde, sta_nam_kor, bld_dat, end_dat, gru_nam,
                    lon, lat, sur_tmp_yn, mid_tmp_yn, bot_tmp_yn,
                     sur_dep, mid_dep, bot_dep, sta_des
                ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,? )""".trimIndent()

                LOGGER.debug("\n"+ sql)

                df.forEach { it ->
                    try {
                        conn.prepareStatement(sql)?.use { preparedStatement ->
                            preparedStatement.setString(1, it["sta_cde"].toString())
                            preparedStatement.setString(2, it["sta_nam_kor"].toString())
                            preparedStatement.setString(3, it["bld_dat"].toString())
                            it["end_dat"]?.let{
                                preparedStatement.setString(4,   it.toString())
                            }
                            preparedStatement.setString(5, it["gru_nam"].toString())
                            preparedStatement.setDouble(6, it["lon"].toString().toDouble())
                            preparedStatement.setDouble(7, it["lat"].toString().toDouble())
                            preparedStatement.setString(8, it["sur_tmp_yn"].toString())
                            preparedStatement.setString(9, it["mid_tmp_yn"].toString())
                            preparedStatement.setString(10, it["bot_tmp_yn"].toString())
                            preparedStatement.setString(11, it["sur_dep"].toString())
                            it["mid_dep"]?.let{
                                preparedStatement.setString(12,   it.toString())
                            }
                            it["bot_dep"]?.let{
                                preparedStatement.setString(13,   it.toString())
                            }
                            it["sta_des"]?.let{
                                preparedStatement.setString(14,   it.toString())
                            }
                            preparedStatement.executeUpdate()
                        }
                    } catch (e: Exception){
                        LOGGER.debug(e.localizedMessage)
                    }
                }

            }
        } catch (e: Exception){
            LOGGER.error(e.localizedMessage)
        }

    }

    fun getRealTimeOceanWaterQuality_Rocovery(wtch_dt_start:String, wtch_dt_end:String){
        LOGGER.debug("wtch_dt_start : ${wtch_dt_start}, wtch_dt_end : ${wtch_dt_end}")
        val maxPage = 500
        val numOfRows = 1000
        val url = "${Config.configData.MOF_API?.endPoint}/${Config.configData.MOF_API?.subPath}" +
                "?wtch_dt_start=${
                    URLEncoder.encode(
                        wtch_dt_start,
                        StandardCharsets.UTF_8.toString()
                    )
                }" +
                "&wtch_dt_end=${
                    URLEncoder.encode(
                        wtch_dt_end,
                        StandardCharsets.UTF_8.toString()
                    )
                }" +
                "&numOfRows=${numOfRows}" +
                "&ServiceKey=${Config.configData.MOF_API?.apikey}"

        val dataList = loadData(url , maxPage)
        val df = dataList.concat()

        LOGGER.info("\n"+ df.head(5).toString())
        LOGGER.info("\n"+ df.describe().toString())

        val tableName = "OWQInformation"

        try {

            DriverManager.getConnection(Config.url).use {conn ->
                createAndPopulateTable(conn, tableName)

                val sql = """INSERT INTO ${tableName} ( 
                    rtmWqWtchDtlDt, rtmWqWtchStaCd, rtmWtchWtem, rtmWqCndctv,
                    ph, rtmWqDoxn, rtmWqTu, rtmWqBgalgsQy, rtmWqChpla, rtmWqSlnty 
                ) VALUES (?,?,?,?,?,?,?,?,?,? )""".trimIndent()

                LOGGER.debug("\n"+ sql)



                val result = df
                    .convert{ colsAtAnyDepth().colsOf<Double>() }.with { String.format("%.3f", it) }
                    .convert { colsAtAnyDepth().colsOf<Float>() }.with{ String.format("%.3f", it)}
                    .convert("rtmWqWtchDtlDt").with { (it as String).substring(0, 19) }

                result.forEach { it ->

                    try {
                        conn.prepareStatement(sql)?.use { preparedStatement ->
                            preparedStatement.setString(1, it["rtmWqWtchDtlDt"].toString())
                            preparedStatement.setString(2, it["rtmWqWtchStaCd"].toString())
                            preparedStatement.setString(3, it["rtmWtchWtem"].toString())
                            preparedStatement.setString(4, it["rtmWqCndctv"].toString())
                            preparedStatement.setString(5, it["ph"].toString())
                            preparedStatement.setString(6, it["rtmWqDoxn"].toString())
                            preparedStatement.setString(7, it["rtmWqTu"].toString())
                            preparedStatement.setString(8, it["rtmWqBgalgsQy"].toString())
                            preparedStatement.setString(9, it["rtmWqChpla"].toString())
                            preparedStatement.setString(10, it["rtmWqSlnty"].toString())

                            preparedStatement.executeUpdate()
                        }
                    } catch (e: Exception){
                        LOGGER.debug(e.localizedMessage)
                    }
                }

            }
        } catch (e: Exception){
            LOGGER.error(e.localizedMessage)
        }
    }

    private fun loadData(path:String, maxPage:Int): List<DataFrame<*>> {

        val rows = mutableListOf<DataFrame<*>>()
        var requestPage = 1
        do{
            val pagePath = "$path&pageNo=$requestPage"
            val jsonData = XML.toJSONObject(DataFrame.read(pagePath).toCsvStr())
            val df = DataFrame.readJson(jsonData.toString().byteInputStream())
            try {
                val instanceDf = df.get("response").get("body").get("items").get("item")[0] as DataFrame<*>
                requestPage += 1
                rows.add(instanceDf)
            } catch(e: Exception) {
                print(e.localizedMessage)
                break
            }
        } while (requestPage < maxPage )
        return rows
    }

    private fun createAndPopulateTable(con: Connection, tableName: String) {
        val stmt = con.createStatement()
        val sql = """CREATE TABLE IF NOT EXISTS ${tableName} (
                    rtmWqWtchDtlDt TEXT,
                    rtmWqWtchStaCd TEXT,
                    rtmWtchWtem    TEXT,
                    rtmWqCndctv    TEXT,
                    ph             TEXT,
                    rtmWqDoxn      TEXT,
                    rtmWqTu        TEXT,
                    rtmWqBgalgsQy  TEXT,
                    rtmWqChpla     TEXT,
                    rtmWqSlnty     TEXT,
                    constraint primaryKey
                        primary key (rtmWqWtchDtlDt, rtmWqWtchStaCd)
                );""".trimIndent()

        stmt.executeUpdate(sql)

    }

    private fun makeUrl(funcName:String):String {


        val urlString = when (funcName) {
            ::getRealTimeObservation.name -> {
                "${Config.configData.NIFS_API?.endPoint}/${Config.configData.NIFS_API?.subPath}" +
                        "?id=${Config.configData.NIFS_API?.id?.list}&key=${Config.configData.NIFS_API?.apikey}"
            }
            ::getRealTimeObservatory.name -> {
                "${Config.configData.NIFS_API?.endPoint}/${Config.configData.NIFS_API?.subPath}" +
                        "?id=${Config.configData.NIFS_API?.id?.code}&key=${Config.configData.NIFS_API?.apikey}"
            }
            ::getRealTimeOceanWaterQuality_Rocovery.name -> {
                val now = Clock.System.now()
                @OptIn(FormatStringsInDatetimeFormats::class)
                val currentTime = now
                    .toLocalDateTime(TimeZone.of("Asia/Seoul"))
                    .format(LocalDateTime.Format{byUnicodePattern("yyyy-MM-dd HH:mm:ss")})

                @OptIn(FormatStringsInDatetimeFormats::class)
                val previous24Hour = now
                    .minus(2, DateTimeUnit.HOUR)
                    .toLocalDateTime(TimeZone.of("Asia/Seoul"))
                    .format(LocalDateTime.Format{byUnicodePattern("yyyy-MM-dd HH:mm:ss")})


                LOGGER.debug("Current time : ${currentTime}, Previous time : ${previous24Hour}")

                val numOfRows = 1000
                "${Config.configData.MOF_API?.endPoint}/${Config.configData.MOF_API?.subPath}" +
                        "?wtch_dt_start=${URLEncoder.encode(previous24Hour, StandardCharsets.UTF_8.toString())}" +
                        "&wtch_dt_end=${URLEncoder.encode(currentTime, StandardCharsets.UTF_8.toString())}" +
                        "&numOfRows=${numOfRows}" +
                        "&ServiceKey=${Config.configData.MOF_API?.apikey}"


            }
            else -> {""}
        }

        return urlString
    }


}