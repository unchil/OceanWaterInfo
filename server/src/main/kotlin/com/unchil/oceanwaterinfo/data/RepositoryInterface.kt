package com.unchil.oceanwaterinfo

interface RepositoryInterface {
    suspend fun fetchSeaWaterInfoFromDb(division:String, ):List<SeawaterInformationByObservationPoint>
    suspend fun fetchSeaWaterInfoStatisticsFromDb():List<SeaWaterInfoByOneHourStat>
    suspend fun observatoryInfo():List<Observatory>
}