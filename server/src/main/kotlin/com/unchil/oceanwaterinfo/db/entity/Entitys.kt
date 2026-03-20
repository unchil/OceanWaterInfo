package com.unchil.oceanwaterinfo


import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.Table

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
