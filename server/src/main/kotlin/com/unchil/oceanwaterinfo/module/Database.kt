package com.unchil.oceanwaterinfo

import com.unchil.oceanwaterinfo.EnvInfoServerDatabase.dataSource
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.config.ApplicationConfig
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction



object EnvInfoServerDatabase {

    private lateinit var config: ApplicationConfig
    // Application 시작 시 초기화할 메서드
    fun init(environmentConfig: ApplicationConfig) {
        config = environmentConfig
    }

    private fun getDBConfig(): HikariConfig {

        val databaseName = config.property("storage.dbName").getString()

        val dbConfig = with(databaseName) {
            when {
                startsWith("sqlite") -> {
                    HikariConfig().apply {
                        driverClassName = config.property("storage.database.sqlite.driverClassName").getString()
                        jdbcUrl = config.property("storage.database.sqlite.jdbcURL").getString()

                        maximumPoolSize = config.property("storage.dbcp.maxPoolSize").getString().toInt()
                        isAutoCommit = config.property("storage.dbcp.isAutoCommit").getString().toBoolean()
                        validate()
                    }
                }
                else -> {
                    HikariConfig().apply {
                        driverClassName = config.property("storage.database.h2.driverClassName").getString()
                        jdbcUrl = config.property("storage.database.h2.jdbcURL").getString()
                        username = config.property("storage.database.h2.user").getString()
                        password = config.property("storage.database.h2.password").getString()

                        maximumPoolSize = config.property("storage.dbcp.maxPoolSize").getString().toInt()
                        isAutoCommit = config.property("storage.dbcp.isAutoCommit").getString().toBoolean()
                        validate()
                    }

                }

            }
        }

        return dbConfig
    }

    // by lazy를 사용하여 최초 접근 시점에 초기화되도록 수정
    val dataSource: HikariDataSource by lazy { HikariDataSource(getDBConfig()) }
}


fun Application.configureDatabase() {

    fun initMemoryDb(){
        monitor.subscribe (ApplicationStopping) {
            dataSource.close()
        }
        transaction(Database.connect(dataSource)) {
            addLogger(DBSqlLogger)
        }
    }

    fun initSqliteDbTable(){
        monitor.subscribe (ApplicationStopping) {
            dataSource.close()
        }

        transaction (Database.connect(dataSource)){
            exec("PRAGMA journal_mode=WAL;") // 읽기/쓰기 동시성 확보
            addLogger(DBSqlLogger)
        }
    }

    val databaseName = environment.config.property("storage.dbName").getString()

    with(databaseName) {
        when {
            startsWith("sqlite") -> {
                initSqliteDbTable()
            }
            startsWith("h2") -> {
                initMemoryDb()
            }
            else -> {
                initMemoryDb()
            }
        }

    }


}

