package com.unchil.oceanwaterinfo

import com.unchil.oceanwaterinfo.SDoT_EnvInfo.sensing_time
import com.unchil.oceanwaterinfo.SDoT_EnvInfo.serial
import com.unchil.oceanwaterinfo.TidalCurrentInfoKHOA.pre_lat
import com.unchil.oceanwaterinfo.TidalCurrentInfoKHOA.pre_lon
import com.unchil.oceanwaterinfo.TidalCurrentInfoKHOA.sch_time
import org.jetbrains.exposed.v1.core.Table

object ObservationTable: Table("Observation"){
    val sta_cde = varchar("sta_cde", 5)
    val sta_nam_kor = varchar("sta_nam_kor", 50)
    val obs_dat = varchar("obs_dat", 10)
    val obs_tim = varchar("obs_tim", 8)
    val obs_datetime = varchar("obs_datetime", 19)
    val repair_gbn =  varchar("repair_gbn", 1)
    val obs_lay = varchar("obs_lay", 1)
    val wtr_tmp = varchar("wtr_tmp", 10)

    init {
        index("idx_datetime", false, columns = arrayOf(obs_datetime) )
    }

    override val primaryKey = PrimaryKey(sta_cde, obs_dat, obs_tim, obs_lay, name = "primaryKey")
}


object ObservatoryTable: Table("Observatory"){
    val sta_cde = varchar("sta_cde", 5)
    val sta_nam_kor = varchar("sta_nam_kor", 30)
    val bld_dat = varchar("bld_dat", 10)
    val end_dat = varchar("end_dat", 10).nullable()
    val gru_nam =  varchar("gru_nam", 30)
    val lon = double("lon")
    val lat = double("lat")
    val sur_tmp_yn = varchar("sur_tmp_yn", 1)
    val mid_tmp_yn = varchar("mid_tmp_yn", 1)
    val bot_tmp_yn = varchar("bot_tmp_yn", 1)
    val sur_dep = varchar("sur_dep", 10).nullable()
    val mid_dep = varchar("mid_dep", 10).nullable()
    val bot_dep = varchar("bot_dep", 10).nullable()
    val sta_des = varchar("sta_des", 250).nullable()

    override val primaryKey = PrimaryKey(sta_cde, name = "primaryKey")
}

object OWQInformationTable: Table("OWQInformation"){
    val rtmWqWtchDtlDt = varchar("rtmWqWtchDtlDt", 21)
    val rtmWqWtchStaCd = varchar("rtmWqWtchStaCd", 7)
    val rtmWtchWtem = varchar("rtmWtchWtem", 20)
    val rtmWqCndctv = varchar("rtmWqCndctv", 20)
    val ph = varchar("ph", 20)
    val rtmWqDoxn = varchar("rtmWqDoxn", 20)
    val rtmWqTu = varchar("rtmWqTu", 20)
    val rtmWqBgalgsQy = varchar("rtmWqBgalgsQy", 20).nullable()
    val rtmWqChpla = varchar("rtmWqChpla", 20)
    val rtmWqSlnty = varchar("rtmWqSlnty", 20)


    override val primaryKey = PrimaryKey(rtmWqWtchDtlDt, rtmWqWtchStaCd, name = "primaryKey")
}


object ObservationKHOA: Table("ObservationKHOA"){

    val obsCode = varchar("obsCode", length=10)

    val obsrvnDt = varchar("obsrvnDt", length=20)
    val wndrct = varchar("wndrct", length=10).nullable()
    val wspd = varchar("wspd", length=10).nullable()
    val maxMmntWspd = varchar("maxMmntWspd", length=10).nullable()
    val artmp = varchar("artmp", length=10).nullable()
    val atmpr = varchar("atmpr", length=10).nullable()
    val wvhgt = varchar("wvhgt", length=10).nullable()
    val wvpd = varchar("wvpd", length=10).nullable()
    val crdir = varchar("crdir", length=10).nullable()
    val crsp = varchar("crsp", length=10).nullable()
    val wtem = varchar("wtem", length=10).nullable()
    val slnty = varchar("slnty", length=10).nullable()

    override val primaryKey = PrimaryKey(obsCode, obsrvnDt, name = "primaryKey")

}

object ObservatoryKHOA: Table("ObservatoryKHOA"){
    val obsCode = varchar("obsCode", length=10)
    val obsvtrNm = varchar("obsvtrNm", length=200)
    val longitude = double("longitude")
    val latitude =  double("latitude")

    override val primaryKey = PrimaryKey(obsCode, name = "primaryKey")
}

/*
물리학과 기상학, 해양학에서 유체의 흐름을 나타낼 때 사용하는 u와 v는 특정 단어의 직접적인 약자라기보다는,
데카르트 좌표계(Cartesian coordinate system)의 관습적인 표현에서 유래되었습니다.
결론부터 말씀드리면 다음과 같습니다.

    1. 관습적인 알파벳 순서 (u, v, w)수학이나 물리학에서는 좌표축 x, y, z에 대응하는 속도 성분을 알파벳 순서에 따라 u, v, w로 할당합니다.
        u: x축(동서 방향)의 속도 성분
        v: y축(남북 방향)의 속도 성분
        w: z축(수직 방향)의 속도 성분
*/

object TidalCurrentInfoKHOA: Table("TidalCurrentInfoKHOA"){
    val sch_time = varchar("sch_time", length=20)
    val pre_lon = double("pre_lon")
    val pre_lat =  double("pre_lat")
    val current_dir = double("current_dir")
    val current_speed = double("current_speed")

    override val primaryKey = PrimaryKey(sch_time, pre_lon, pre_lat, name = "primaryKey")
}

object SDoT_Location: Table("SDoT_Location"){
    val serial = varchar("serial", length=30)
    val addr = varchar("addr", length=30)
    val lat = varchar("lat", length=30)
    val lng = varchar("lng", length=30)

    override val primaryKey = PrimaryKey(serial, name = "primaryKey")
}

object SDoT_EnvInfo_Gyonggi: Table("SDoT_EnvInfo_Gyonggi"){
    val obs = varchar("obs", length=30)
    val region = varchar("region", length=30)
    val sensing_time = varchar("sensing_time", length=30)
    val so2 = varchar("so2", length=30)
    val co = varchar("co", length=30)
    val no2 = varchar("no2", length=30)
    val o3 = varchar("o3", length=30)
    val pm10 = varchar("pm10", length=30)
    val pm25 = varchar("pm25", length=30)

    override val primaryKey = PrimaryKey(obs, sensing_time, name = "primaryKey")
}

object SDoT_EnvInfo: Table("SDoT_EnvInfo"){
    val modelname = varchar("modelname", length=30)
    val serial = varchar("serial", length=30)
    val sensing_time = varchar("sensing_time", length=30)
    val region = varchar("region", length=30)
    val autonomous_district = varchar("autonomous_district", length=30)
    val administrative_district = varchar("administrative_district", length=30)
    val max_temp = varchar("max_temp", length=30)
    val avg_temp = varchar("avg_temp", length=30)
    val min_temp = varchar("min_temp", length=30)
    val max_humi = varchar("max_humi", length=30)
    val avg_humi = varchar("avg_humi", length=30)
    val min_humi = varchar("min_humi", length=30)
    val max_wind_speed = varchar("max_wind_speed", length=30)
    val avg_wind_speed = varchar("avg_wind_speed", length=30)
    val min_wind_speed = varchar("min_wind_speed", length=30)
    val max_wind_dire = varchar("max_wind_dire", length=30)
    val avg_wind_dire = varchar("avg_wind_dire", length=30)
    val min_wind_dire = varchar("min_wind_dire", length=30)
    val max_inte_illu = varchar("max_inte_illu", length=30)
    val avg_inte_illu = varchar("avg_inte_illu", length=30)
    val min_inte_illu = varchar("min_inte_illu", length=30)
    val max_ultra_rays = varchar("max_ultra_rays", length=30)
    val avg_ultra_rays = varchar("avg_ultra_rays", length=30)
    val min_ultra_rays = varchar("min_ultra_rays", length=30)
    val max_noise = varchar("max_noise", length=30)
    val avg_noise = varchar("avg_noise", length=30)
    val min_noise = varchar("min_noise", length=30)
    val max_vibr_x = varchar("max_vibr_x", length=30)
    val avg_vibr_x = varchar("avg_vibr_x", length=30)
    val min_vibr_x = varchar("min_vibr_x", length=30)
    val max_vibr_y = varchar("max_vibr_y", length=30)
    val avg_vibr_y = varchar("avg_vibr_y", length=30)
    val min_vibr_y = varchar("min_vibr_y", length=30)
    val max_vibr_z = varchar("max_vibr_z", length=30)
    val avg_vibr_z = varchar("avg_vibr_z", length=30)
    val min_vibr_z = varchar("min_vibr_z", length=30)
    val max_effe_temp = varchar("max_effe_temp", length=30)
    val avg_effe_temp = varchar("avg_effe_temp", length=30)
    val min_effe_temp = varchar("min_effe_temp", length=30)
    val max_no2 = varchar("max_no2", length=30)
    val avg_no2 = varchar("avg_no2", length=30)
    val min_no2 = varchar("min_no2", length=30)
    val max_co = varchar("max_co", length=30)
    val avg_co = varchar("avg_co", length=30)
    val min_co = varchar("min_co", length=30)
    val max_so2 = varchar("max_so2", length=30)
    val avg_so2 = varchar("avg_so2", length=30)
    val min_so2 = varchar("min_so2", length=30)
    val max_nh3 = varchar("max_nh3", length=30)
    val avg_nh3 = varchar("avg_nh3", length=30)
    val min_nh3 = varchar("min_nh3", length=30)
    val max_h2s = varchar("max_h2s", length=30)
    val avg_h2s = varchar("avg_h2s", length=30)
    val min_h2s = varchar("min_h2s", length=30)
    val max_o3 = varchar("max_o3", length=30)
    val avg_o3 = varchar("avg_o3", length=30)
    val min_o3 = varchar("min_o3", length=30)
    val date = varchar("date", length=30)
    val data_no = varchar("data_no", length=30)

    override val primaryKey = PrimaryKey(serial, sensing_time, name = "primaryKey")
}