package com.unchil.oceanwaterinfo

import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.format.FormatStringsInDatetimeFormats

val LOGGER = KtorSimpleLogger( "CollectionServer")


@Suppress("DefaultLocale")
@OptIn(FormatStringsInDatetimeFormats::class)
fun main() = runBlocking {
    val collector = DataCollector()
    collector.startCollecting()
    LOGGER.info("Data Collector Stopped.")
}
