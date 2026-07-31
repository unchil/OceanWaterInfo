package com.unchil.oceanwaterinfo


import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.Table


object CoastalFloodingGeoTbl: Table("CoastalFloodingGeoTbl"){
    val grade = varchar("grade", 2)
    val flodVlCn = varchar("flodVlCn", 20)
    val ctpvNm = varchar("ctpvNm", 20)
    val geom = text("geom")

    init {
        index("CoastalFloodingGeoTbl__index_grade_ctpvNm", false, columns = arrayOf(grade, ctpvNm) )
        index("CoastalFloodingGeoTbl__index_grade", false, columns = arrayOf(grade) )
        index("CoastalFloodingGeoTbl__index_ctpvNm", false, columns = arrayOf( ctpvNm) )
    }
}

object CoastalFloodingGeoInfo: Table("CoastalFloodingGeoInfo"){
    val ctpvNm = varchar("ctpvNm", 20)
    val sggNm = varchar("sggNm", 20)
    val flodVlCn = varchar("flodVlCn", 20)
    val geom = text("geom")
}

object QWQObservatoryTable: Table("OWQObservatory"){
    val sta_code = varchar("sta_code", 7)
    val sta_name =  varchar("sta_name", 20)
    val ocean_division =  varchar("ocean_division", 20)
    val lon =  double("lon")
    val lat =  double("lat")
    override val primaryKey = PrimaryKey(sta_code, name = "OWQObservatory_pk")
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

fun toSeaWaterInformation(it: ResultRow) = SeaWaterInformation(
    it[OWQInformationTable.rtmWqWtchDtlDt],
    it[OWQInformationTable.rtmWqWtchStaCd],
    it[QWQObservatoryTable.sta_name],
    it[OWQInformationTable.rtmWtchWtem],
    it[OWQInformationTable.rtmWqCndctv],
    it[OWQInformationTable.ph],
    it[OWQInformationTable.rtmWqDoxn],
    it[OWQInformationTable.rtmWqTu],
    it[OWQInformationTable.rtmWqChpla],
    it[OWQInformationTable.rtmWqSlnty],
    it[QWQObservatoryTable.lon],
    it[QWQObservatoryTable.lat]
)


fun toSeawaterInformationByObservationPoint(it: ResultRow) = SeawaterInformationByObservationPoint(

    it[ObservationTable.sta_cde],
    it[ObservationTable.sta_nam_kor],
    it[ObservationTable.obs_datetime],
    it[ObservationTable.obs_lay],
    it[ObservationTable.wtr_tmp],
    it[ObservatoryTable.gru_nam],
    it[ObservatoryTable.lon],
    it[ObservatoryTable.lat]

)


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

fun toObservation(it: ResultRow) = Observation(
    it[ObservationTable.sta_cde],
    it[ObservationTable.sta_nam_kor],
    it[ObservationTable.obs_datetime],
    it[ObservationTable.repair_gbn],
    it[ObservationTable.obs_lay],
    it[ObservationTable.wtr_tmp]
)


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

fun toObservatory(it: ResultRow) = Observatory(
    it[ObservatoryTable.sta_cde],
    it[ObservatoryTable.sta_nam_kor],
    it[ObservatoryTable.bld_dat],
    it[ObservatoryTable.end_dat],
    it[ObservatoryTable.gru_nam],
    it[ObservatoryTable.lon],
    it[ObservatoryTable.lat],
    it[ObservatoryTable.sur_tmp_yn],
    it[ObservatoryTable.mid_tmp_yn],
    it[ObservatoryTable.bot_tmp_yn],
    it[ObservatoryTable.sur_dep],
    it[ObservatoryTable.mid_dep],
    it[ObservatoryTable.bot_dep],
    it[ObservatoryTable.sta_des]
)


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

object TidalCurrentInfoKHOA: Table("TidalCurrentInfoKHOA"){
    val sch_time = varchar("sch_time", length=20)
    val pre_lon = double("pre_lon")
    val pre_lat =  double("pre_lat")
    val current_dir = double("current_dir")
    val current_speed = double("current_speed")

    override val primaryKey = PrimaryKey(sch_time, pre_lon, pre_lat, name = "primaryKey")
}

fun toTidalCurrentInfo(it: ResultRow) = TidalCurrentInfo(
    it[TidalCurrentInfoKHOA.sch_time],
    it[TidalCurrentInfoKHOA.pre_lon],
    it[TidalCurrentInfoKHOA.pre_lat],
    it[TidalCurrentInfoKHOA.current_dir],
    it[TidalCurrentInfoKHOA.current_speed],
)

object SDoT_Location: Table("SDoT_Location"){
    val serial = varchar("serial", length=30)
    val addr = varchar("addr", length=30)
    val lat = varchar("lat", length=30)
    val lng = varchar("lng", length=30)

    override val primaryKey = PrimaryKey(serial, name = "primaryKey")
}

fun toSDoTEnvInformation(it: ResultRow) = SDoTEnvInformation(
    it[SDoT_EnvInfo.sensing_time],
    it[SDoT_EnvInfo.serial],
    it[SDoT_EnvInfo.region],
    it[SDoT_EnvInfo.autonomous_district],
    it[SDoT_EnvInfo.administrative_district],
    it[SDoT_Location.addr],
    it[SDoT_Location.lat],
    it[SDoT_Location.lng],
    it[SDoT_EnvInfo.max_temp], it[SDoT_EnvInfo.min_temp], it[SDoT_EnvInfo.avg_temp],
    it[SDoT_EnvInfo.max_humi], it[SDoT_EnvInfo.min_humi], it[SDoT_EnvInfo.avg_humi],
    it[SDoT_EnvInfo.max_ultra_rays], it[SDoT_EnvInfo.min_ultra_rays], it[SDoT_EnvInfo.avg_ultra_rays],
    it[SDoT_EnvInfo.max_noise], it[SDoT_EnvInfo.min_noise], it[SDoT_EnvInfo.avg_noise],
    it[SDoT_EnvInfo.max_vibr_x], it[SDoT_EnvInfo.min_vibr_x], it[SDoT_EnvInfo.avg_vibr_x],
    it[SDoT_EnvInfo.max_vibr_y], it[SDoT_EnvInfo.min_vibr_y], it[SDoT_EnvInfo.avg_vibr_y],
    it[SDoT_EnvInfo.max_vibr_z], it[SDoT_EnvInfo.min_vibr_z], it[SDoT_EnvInfo.avg_vibr_z],
    it[SDoT_EnvInfo.max_no2], it[SDoT_EnvInfo.min_no2], it[SDoT_EnvInfo.avg_no2],
    it[SDoT_EnvInfo.max_co], it[SDoT_EnvInfo.min_co], it[SDoT_EnvInfo.avg_co],
    it[SDoT_EnvInfo.max_so2], it[SDoT_EnvInfo.min_so2], it[SDoT_EnvInfo.avg_so2],
    it[SDoT_EnvInfo.max_nh3], it[SDoT_EnvInfo.min_nh3], it[SDoT_EnvInfo.avg_nh3],
    it[SDoT_EnvInfo.max_h2s], it[SDoT_EnvInfo.min_h2s], it[SDoT_EnvInfo.avg_h2s],
    it[SDoT_EnvInfo.max_o3], it[SDoT_EnvInfo.min_o3], it[SDoT_EnvInfo.avg_o3]
)


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


object SDoT_Location_Gyonggi: Table("SDoT_Location_Gyonggi"){
    val obs = varchar("obs", length=30)
    val addr = varchar("addr", length=30)
    val op = varchar("op", length=30)
    val regdate = varchar("regdate", length=30)
    val lng = varchar("lng", length=30)
    val lat = varchar("lat", length=30)


    override val primaryKey = PrimaryKey(obs, name = "primaryKey")
}


fun toSDoTEnvInformationGyonggi(it: ResultRow) = SDoTEnvInformationGyonggi(
    it[SDoT_EnvInfo_Gyonggi.sensing_time],
    it[SDoT_EnvInfo_Gyonggi.obs],
    it[SDoT_EnvInfo_Gyonggi.region],
    it[SDoT_Location_Gyonggi.addr],
    it[SDoT_Location_Gyonggi.lat],
    it[SDoT_Location_Gyonggi.lng],
    it[SDoT_EnvInfo_Gyonggi.so2], it[SDoT_EnvInfo_Gyonggi.co], it[SDoT_EnvInfo_Gyonggi.no2],
    it[SDoT_EnvInfo_Gyonggi.o3], it[SDoT_EnvInfo_Gyonggi.pm10], it[SDoT_EnvInfo_Gyonggi.pm25],

)


fun toSDoTEnvInfoUnionFromSDoT_EnvInfo(it: ResultRow) = SDoTEnvInfoUnion(
    it[SDoT_EnvInfo.sensing_time],
    it[SDoT_EnvInfo.serial],
    it[SDoT_EnvInfo.region],
    it[SDoT_Location.addr],
    it[SDoT_Location.lat],
    it[SDoT_Location.lng],
    it[SDoT_EnvInfo.max_so2], it[SDoT_EnvInfo.max_co], it[SDoT_EnvInfo.max_no2],
    it[SDoT_EnvInfo.max_o3], it[SDoT_EnvInfo.max_nh3], it[SDoT_EnvInfo.max_h2s],
    "", ""
)

fun toSDoTEnvInfoUnionFromSDoT_EnvInfoGyonggi(it: ResultRow) = SDoTEnvInfoUnion(
    it[SDoT_EnvInfo_Gyonggi.sensing_time],
    it[SDoT_EnvInfo_Gyonggi.obs],
    it[SDoT_EnvInfo_Gyonggi.region],
    it[SDoT_Location_Gyonggi.addr],
    it[SDoT_Location_Gyonggi.lat],
    it[SDoT_Location_Gyonggi.lng],
    it[SDoT_EnvInfo_Gyonggi.so2], it[SDoT_EnvInfo_Gyonggi.co], it[SDoT_EnvInfo_Gyonggi.no2],
    it[SDoT_EnvInfo_Gyonggi.o3], "", "",
    it[SDoT_EnvInfo_Gyonggi.pm10], it[SDoT_EnvInfo_Gyonggi.pm25]

    )



object KHNP_WasteWater:Table("KHNP_WasteWater"){
    val time = varchar("time", length=30)
    val genName = varchar("genName", length=30)
    val tm001 = varchar("tm001", length=30)
    val tm001_time = varchar("tm001_time", length=30)
    val tm002 = varchar("tm002", length=30)
    val tm002_time = varchar("tm002_time", length=30)

    override val primaryKey = PrimaryKey( time,genName,  name = "primaryKey")
}


object KHNP_ThermalWasteWater:Table("KHNP_ThermalWasteWater"){
    val time = varchar("time", length=30)
    val genName = varchar("genName", length=30)
    val rm001 = varchar("rm001", length=30)
    val rm001_time = varchar("rm001_time", length=30)
    val rm002 = varchar("rm002", length=30)
    val rm002_time = varchar("rm002_time", length=30)
    val rm005 = varchar("rm005", length=30)
    val rm005_time = varchar("rm005_time", length=30)
    val rm006 = varchar("rm006", length=30)
    val rm006_time = varchar("rm006_time", length=30)

    override val primaryKey = PrimaryKey( time,genName,  name = "primaryKey")
}

object KHNP_RadioRate:Table("KHNP_RadioRate"){
    val collectionTime = varchar("collectionTime", length=30)
    val time = varchar("time", length=30)
    val genName = varchar("genName", length=30)
    val name = varchar("name", length=30)
    val expl = varchar("expl", length=30)
    val value = varchar("value", length=30)

    override val primaryKey = PrimaryKey(time, genName, name,  name = "primaryKey")
}


object KHNP_RadioActiveWaste:Table("KHNP_RadioActiveWaste"){
    val collectionTime = varchar("collectionTime", length=30)
    val spmon = varchar("spmon", length=30)
    val genName = varchar("genName", length=30)
    val plant = varchar("plant", length=30)
    val total = varchar("total", length=30)

    override val primaryKey = PrimaryKey(spmon, plant,   name = "primaryKey")
}

object KHNP_PlantInfo:Table("KHNP_PlantInfo"){
    val siteCd = varchar("siteCd", length=30)
    val siteNm = varchar("siteNm", length=30)
    val siteMm = varchar("siteMm", length=100)
    override val primaryKey = PrimaryKey(siteCd,   name = "primaryKey")

}

object KHNP_PlantOperationInfo:Table("KHNP_PlantOperationInfo"){
    val collectionTime = varchar("collectionTime", length=30)
    val siteCd = varchar("siteCd", length=30)
    val genName = varchar("genName", length=30)
    val unitCd = varchar("unitCd", length=30)
    val unitDttm = varchar("unitDttm", length=30)
    val unitNm = varchar("unitNm", length=30)
    val unitSt = varchar("unitSt", length=30)
    override val primaryKey = PrimaryKey(siteCd, unitCd , unitNm,   name = "primaryKey")

}