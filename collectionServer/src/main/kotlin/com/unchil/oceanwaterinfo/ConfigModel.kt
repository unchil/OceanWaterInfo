package com.unchil.oceanwaterinfo

import kotlinx.serialization.Serializable


// JSON의 최상위 구조에 해당하는 메인 데이터 클래스
@Serializable
data class ConfigData(
    val NIFS_API: NifsApiConfig? = null,
    val MOF_API: MofApiConfig? = null,
    val KHOA_API: KhoaApiConfig? = null,
    val KHOA_TIDALCURRENT_API: KhoaTidalCurrentApiConfig? = null,
    val SDOT_API: SDoTApiConfig? = null,
    val KHNP: KHNP? = null,
    val WATER_LOGGED: Water_LoggedConfig? = null,
    val SDOT_Gyonggi: SDoTGyonggiConfig? = null,
    val SQLITE_DB: DatabaseConfig? = null,
    val COLLECTION_TYPE: CollectionConfig? = null,
    val WAVE_INFO_API: WaveInfoApiConfig? = null
)

@Serializable
data class WaveInfoApiConfig(
    val endPoint: String,
    val apikey: String,
    val subPath: String,
    val type: String,
    val min: Int,
)
@Serializable
data class KhoaTidalCurrentApiConfig(
    val endPoint: String,
    val apikey: String,
    val subPath: String,
    val type: String,
    val boundBox: String,
    val interval: Int,
    val predictedTotalMinute: Int,
    val limitedParallelism: Int,
    val loopdelay: Int

)

@Serializable
data class  NifsApiConfig(
    val endPoint: String,
    val apikey: String,
    val subPath: String,
    val id: NifsApiID
)

@Serializable
data class NifsApiID (
    val list: String,
    val code: String
)

@Serializable
data class  MofApiConfig(
    val endPoint: String,
    val apikey: String,
    val subPath: String,
    val limitedParallelism: Int,
    val numOfRows: Int

)

@Serializable
data class  KhoaApiConfig(
    val endPoint: String,
    val apikey: String,
    val subPath: String,
    val type: String,
    val min: String,
    val numOfRows: String,
    val limitedParallelismREST: Int,
    val limitedParallelismDB: Int,

    )

@Serializable
data class KHNP_SUBURL(
    val NuclearPlantStates: String,
    val WasteWater: String,
    val RadioRate: String,
    val ThermalWasteWater: String,
    val RadioActiveWaste: String
)

@Serializable
data class KHNP(
    val endPoint: String,
    val subPath: KHNP_SUBURL,
    val serviceKey: String,
    val limitedParallelism: Int
)

@Serializable
data class Water_LoggedConfig(
    val endPoint: String,
    val subPath: String,
    val apikey: String,
    val type: String,
    val node: String,
    val nodeOption: String,
    val mapshaper: String,
    val limitedParallelism: Int,
    val mapshaperLimitedParallelism: Int,
    val numOfRows:Int

)

@Serializable
data class SDoTGyonggiConfig(
    val endPoint: String,
    val apikey: String,
    val subPath: String,
    val type: String
)

@Serializable
data class  SDoTApiConfig(
    val endPoint: String,
    val apikey: String,
    val subPath: String,
    val type: String
)


@Serializable
data class DBCP(
    val maxPoolSize: Int,
    val maxLifetime: Long,
    val connectionTimeout: Long,
    val validationTimeout: Long,
    val idleTimeout: Long,
    val initializationFailTimeout: Long,
    val isAutoCommit: Boolean,
    val keepaliveTime: Long
)

@Serializable
data class DatabaseConfig(
    val jdbcURL: String,
    val driverClassName: String,
    val dbcp:DBCP

)

@Serializable
data class CollectionConfig(
    val type: String,
    val event: String,
    val interval: String,
    val wtch_dt_start: String? = null, // JSON에 없을 수도 있는 값은 nullable로 처리
    val wtch_dt_end: String? = null,
    val allowedIntervals:List<Int> = emptyList()
)

