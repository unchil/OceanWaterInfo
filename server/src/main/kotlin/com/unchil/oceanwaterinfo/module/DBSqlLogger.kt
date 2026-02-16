package com.unchil.oceanwaterinfo

import io.ktor.util.logging.KtorSimpleLogger
import org.jetbrains.exposed.v1.core.SqlLogger
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.statements.StatementContext
import org.jetbrains.exposed.v1.core.statements.expandArgs

val LOGGER = KtorSimpleLogger( "OceanWaterInformation")


object DBSqlLogger: SqlLogger {
    val LOGGER = KtorSimpleLogger( "")
    override fun log(context: StatementContext, transaction: Transaction) {
        if (LOGGER.isErrorEnabled) {
            LOGGER.error("SQL: ${context.expandArgs(transaction)}")
        }
        if (LOGGER.isWarnEnabled) {
            LOGGER.warn("SQL: ${context.expandArgs(transaction)}")
        }
        if (LOGGER.isInfoEnabled) {
            LOGGER.info("SQL: ${context.expandArgs(transaction)}")
        }
        if (LOGGER.isDebugEnabled) {
            LOGGER.debug("SQL: ${context.expandArgs(transaction)}")
        }
        if (LOGGER.isTraceEnabled) {
            LOGGER.trace("SQL: ${context.expandArgs(transaction)}")
        }
    }
}
