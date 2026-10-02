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
        val config = HikariConfig().apply {
            jdbcUrl = config.property("storage.database.sqlite.jdbcURL").getString()
            driverClassName = config.property("storage.database.sqlite.driverClassName").getString()
            maximumPoolSize = config.property("storage.dbcp.maxPoolSize").getString().toInt()
            isAutoCommit = config.property("storage.dbcp.isAutoCommit").getString().toBoolean()
            validate()
        }
        return config
    }

    // by lazy를 사용하여 최초 접근 시점에 초기화되도록 수정
    val dataSource: HikariDataSource by lazy { HikariDataSource(getDBConfig()) }
}


fun Application.configureDatabase() {


    val databaseName = environment.config.property("storage.dbName").getString()

    val database = with(databaseName) {
        when{
            startsWith("sqlite") -> {
                Database.connect(dataSource)
            }
            else -> {
                val driver = environment.config.property("storage.database.h2.driverClassName").getString()
                val url = environment.config.property("storage.database.h2.jdbcURL").getString()
                val user = environment.config.property("storage.database.h2.user").getString()
                val password = environment.config.property("storage.database.h2.password").getString()
                Database.connect(
                    url = url,
                    driver = driver,
                    user = user,
                    password = password
                )
            }
        }
    }

    fun initMemoryDb(db: Database){
        transaction(db) {
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

    with(databaseName) {
        when {
            startsWith("h2") -> {
                initMemoryDb(database)
            }
            startsWith("sqlite") -> {
                initSqliteDbTable()
            }
            else -> {
                initMemoryDb(database)
            }
        }

    }
}

