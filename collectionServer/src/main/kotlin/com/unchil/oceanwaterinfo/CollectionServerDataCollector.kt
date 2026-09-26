package com.unchil.oceanwaterinfo

class CollectionServerDataCollector {

    val collectionServerRepository = CollectionServerRepository()

    suspend fun batchJob(startDate:String, endDate:String){
        val funcName = ::batchJob.name
        LOGGER.info("${LoggerHeader.Collector_Start.name}: ${funcName}: [startDate[${startDate}], endDate[${endDate}]")
        try {
            collectionServerRepository.getRealTimeOceanWaterQuality_Rocovery(startDate, endDate)

            LOGGER.info("${LoggerHeader.Collector_End.name}: ${funcName}")
        } catch (e: Exception) {
            LOGGER.error("${LoggerHeader.Collector_Error.name}: ${funcName}:[${e.localizedMessage}]")

        }
    }

    suspend fun scheduleJob5Minutes(){
        val funcName = ::scheduleJob5Minutes.name
        LOGGER.info("${LoggerHeader.Collector_Start.name}: ${funcName}")
        try {
            collectionServerRepository.getKhoaTidalCurrent()
            collectionServerRepository.getKhoaObservation()
            collectionServerRepository.getKHNP_RadioRate()
            collectionServerRepository.getKHNP_ThermalWasteWater()
            collectionServerRepository.getKHNP_WasteWater()
            collectionServerRepository.getRealTimeOceanWaterQuality()
            LOGGER.info("${LoggerHeader.Collector_End.name}: ${funcName}")
        } catch (e: Exception) {
            LOGGER.error("${LoggerHeader.Collector_Error.name}: ${funcName}:[${e.localizedMessage}]")
        }
    }

    suspend fun scheduleJob10Minutes(){

        val funcName = ::scheduleJob10Minutes.name
        LOGGER.info("${LoggerHeader.Collector_Start.name}: ${funcName}")

        try {
            // 30 Minutes Gap Data
            collectionServerRepository.getRealTimeObservation()
            collectionServerRepository.getRealTimeObservatory()

            LOGGER.info("${LoggerHeader.Collector_End.name}: ${funcName}")
        } catch (e: Exception) {
            LOGGER.error("${LoggerHeader.Collector_Error.name}: ${funcName}:[${e.localizedMessage}]")

        }

    }

    suspend fun scheduleJob30Minutes(){
        val funcName = ::scheduleJob30Minutes.name
        LOGGER.info("${LoggerHeader.Collector_Start.name}: ${funcName}")
            try {
                // 60 Minutes Gap Data
                collectionServerRepository.getSDoTEnvInfo()
                collectionServerRepository.getSDoTEnvInfoGyonggi()

                LOGGER.info("${LoggerHeader.Collector_End.name}: ${funcName}")
            } catch (e: Exception) {
                LOGGER.error("${LoggerHeader.Collector_Error.name}: ${funcName}:[${e.localizedMessage}]")

            }

    }

    suspend fun scheduleJob720Minutes(){
        val funcName = ::scheduleJob720Minutes.name
        LOGGER.info("${LoggerHeader.Collector_Start.name}: ${funcName}")
        try {
            collectionServerRepository.getCoastalFloodingInfo()

            LOGGER.info("${LoggerHeader.Collector_End.name}: ${funcName}")
        } catch (e: Exception) {
            LOGGER.error("${LoggerHeader.Collector_Error.name}: ${funcName}:[${e.localizedMessage}]")

        }
    }

    suspend fun scheduleJob1440Minutes(){
        val funcName = ::scheduleJob1440Minutes.name
        LOGGER.info("${LoggerHeader.Collector_Start.name}: ${funcName}")


        try {
            collectionServerRepository.getKHNP_RadioActiveWaste()
            collectionServerRepository.getKHNP_PlantStates()

            LOGGER.info("${LoggerHeader.Collector_End.name}: ${funcName}")
        } catch (e: Exception) {
            LOGGER.error("${LoggerHeader.Collector_Error.name}: ${funcName}:[${e.localizedMessage}]")

        }

    }

}