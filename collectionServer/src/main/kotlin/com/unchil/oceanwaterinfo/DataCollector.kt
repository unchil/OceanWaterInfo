package com.unchil.oceanwaterinfo

class DataCollector {

    val repository = Repository()

    fun batchJob(startDate:String, endDate:String){
        LOGGER.info("Data Collector Started. batchJob:startDate[${startDate}], endDate[${endDate}]...")
        try {
            RecoveryCollector().getRealTimeOceanWaterQuality_Rocovery(startDate, endDate)
            LOGGER.info("Batch job finished.")
        } catch (e: Exception) {
            LOGGER.error(e.stackTrace.toString())
        }
    }

    suspend fun scheduleJob5Minutes(){

        LOGGER.info("Data Collector Started. scheduleJob5Minutes...")

        try {
            repository.getRealTimeOceanWaterQuality()
            repository.getKhoaObservation()
            repository.getKhoaTidalCurrent()
            repository.getKHNP_RadioRate()
            repository.getKHNP_ThermalWasteWater()
            repository.getKHNP_WasteWater()

            LOGGER.info("Schedule job for 5 Minutes finished.")
        } catch (e: Exception) {
            LOGGER.error(e.stackTrace.toString())
        }
    }

    suspend fun scheduleJob10Minutes(){

        LOGGER.info("Data Collector Started. scheduleJob10Minutes...")

        try {
            // 30 Minutes Gap Data
            repository.getRealTimeObservation()
            repository.getRealTimeObservatory()

            LOGGER.info("Schedule job for 10 Minutes finished.")
        } catch (e: Exception) {
            LOGGER.error(e.stackTrace.toString())
        }

    }

    suspend fun scheduleJob30Minutes(){
        LOGGER.info("Data Collector Started. scheduleJob30Minutes...")

            try {
                // 60 Minutes Gap Data
                repository.getSDoTEnvInfo()
                repository.getSDoTEnvInfoGyonggi()

                LOGGER.info("Schedule job for 30 Minutes finished.")
            } catch (e: Exception) {
                LOGGER.error(e.stackTrace.toString())
            }

    }

    fun scheduleJob720Minutes(){
        LOGGER.info("Data Collector Started. scheduleJob720Minutes...")
        try {
            repository.getCoastalFloodingInfo()
        } catch (e: Exception) {
            LOGGER.error(e.stackTrace.toString())
        }
    }

    suspend fun scheduleJob1440Minutes(){
        LOGGER.info("Data Collector Started. scheduleJob1440Minutes...")

        try {
            repository.getKHNP_RadioActiveWaste()
            repository.getKHNP_PlantStates()
            LOGGER.info("Schedule job for 1440 Minutes finished.")
        } catch (e: Exception) {
            LOGGER.error(e.stackTrace.toString())
        }

    }

}