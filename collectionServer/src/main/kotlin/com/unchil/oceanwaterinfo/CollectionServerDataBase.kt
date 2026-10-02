package com.unchil.oceanwaterinfo

import com.unchil.oceanwaterinfo.ConfigManager.currentConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource


object CollectionServerDataBase {
    private fun getDBConfig(): HikariConfig {
        val config = HikariConfig().apply {
            jdbcUrl = currentConfig.SQLITE_DB?.jdbcURL ?: ""
            driverClassName = currentConfig.SQLITE_DB?.driverClassName ?: ""
            maximumPoolSize = currentConfig.SQLITE_DB?.dbcp?.maxPoolSize ?: 10
            isAutoCommit = currentConfig.SQLITE_DB?.dbcp?.isAutoCommit ?: false
            validate()
        }
        return config
    }

    val dataSource = HikariDataSource(getDBConfig())

}

