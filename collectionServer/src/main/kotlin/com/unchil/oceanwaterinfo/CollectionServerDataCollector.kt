package com.unchil.oceanwaterinfo

class CollectionServerDataCollector {

    val collectionServerRepository = CollectionServerRepository()

    fun batchJob(startDate:String, endDate:String){
        LOGGER.info("Data Collector Started. batchJob:startDate[${startDate}], endDate[${endDate}]...")
        try {
            CollectionServerRecoveryCollector().getRealTimeOceanWaterQuality_Rocovery(startDate, endDate)
            LOGGER.info("Batch job finished.")
        } catch (e: Exception) {
            LOGGER.error(e.stackTrace.toString())
        }
    }

    suspend fun scheduleJob5Minutes(){

        LOGGER.info("Data Collector Started. scheduleJob5Minutes...")

        try {

            collectionServerRepository.getKhoaTidalCurrent()
            collectionServerRepository.getKhoaObservation()
            collectionServerRepository.getKHNP_RadioRate()
            collectionServerRepository.getKHNP_ThermalWasteWater()
            collectionServerRepository.getKHNP_WasteWater()
          //  collectionServerRepository.getRealTimeOceanWaterQuality()




            LOGGER.info("Schedule job for 5 Minutes finished.")
        } catch (e: Exception) {
            LOGGER.error(e.localizedMessage)
        }
    }

    suspend fun scheduleJob10Minutes(){

        LOGGER.info("Data Collector Started. scheduleJob10Minutes...")

        try {
            // 30 Minutes Gap Data
            collectionServerRepository.getRealTimeObservation()
            collectionServerRepository.getRealTimeObservatory()

            LOGGER.info("Schedule job for 10 Minutes finished.")
        } catch (e: Exception) {
            LOGGER.error(e.stackTrace.toString())
        }

    }

    suspend fun scheduleJob30Minutes(){
        LOGGER.info("Data Collector Started. scheduleJob30Minutes...")

            try {
                // 60 Minutes Gap Data
                collectionServerRepository.getSDoTEnvInfo()
                collectionServerRepository.getSDoTEnvInfoGyonggi()

                LOGGER.info("Schedule job for 30 Minutes finished.")
            } catch (e: Exception) {
                LOGGER.error(e.stackTrace.toString())
            }

    }

    suspend fun scheduleJob720Minutes(){
        LOGGER.info("Data Collector Started. scheduleJob720Minutes...")
        try {
            collectionServerRepository.getCoastalFloodingInfo()
        } catch (e: Exception) {
            LOGGER.error(e.localizedMessage)
        }
    }

    suspend fun scheduleJob1440Minutes(){
        LOGGER.info("Data Collector Started. scheduleJob1440Minutes...")

        try {
            collectionServerRepository.getKHNP_RadioActiveWaste()
            collectionServerRepository.getKHNP_PlantStates()
            LOGGER.info("Schedule job for 1440 Minutes finished.")
        } catch (e: Exception) {
            LOGGER.error(e.stackTrace.toString())
        }

    }

}