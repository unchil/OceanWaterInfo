package com.unchil.oceanwaterinfo

import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.coroutines.runBlocking

val LOGGER = KtorSimpleLogger( "CollectionServer")


@Suppress("DefaultLocale")
fun main(args: Array<String>) = runBlocking {


    val allowedIntervals = ConfigManager.currentConfig.COLLECTION_TYPE?.allowedIntervals

    val interval = if ( args.isNotEmpty() ) {
        args[0].toIntOrNull() ?: 5
    } else {
        LOGGER.info("There are no arguments to drive the schedule job.")
        return@runBlocking
    }

    if (allowedIntervals?.contains(interval) == true ) {

        // 설정 감시 시작
    //    ConfigManager.startWatching(this)

        val collector = CollectionServerDataCollector()

        LOGGER.info("Starting Data Collector with interval: $interval minutes")

        when(interval){
            0 -> {
                val startDate = args[1]
                val endDate = args[2]
                collector.batchJob(startDate, endDate)
            }
            5 -> {
                collector.scheduleJob5Minutes()
            }
            10 -> {
                collector.scheduleJob10Minutes()
            }
            30 -> {
                collector.scheduleJob30Minutes()
            }
            720 -> {
                collector.scheduleJob720Minutes()
            }
            1440 -> {
                collector.scheduleJob1440Minutes()
            }
            else -> {
                LOGGER.info("[$interval minutes] There are no jobs set for the corresponding schedule interval.")
            }
        }
    }else{
        LOGGER.info("[$interval minutes] This is not an allowed schedule interval.")
    }

    // 종료 시
  //  ConfigManager.stopWatching()
    LOGGER.info("Data Collector Stopped.")
}

