package com.unchil.oceanwaterinfo

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopping
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

fun Application.dataSource():HikariDataSource{
    val config = HikariConfig().apply {
        jdbcUrl = environment.config.property("storage.database.sqlite.jdbcURL").getString()
        driverClassName = environment.config.property("storage.database.sqlite.driverClassName").getString()
        maximumPoolSize = environment.config.property("storage.dbcp.maxPoolSize").getString().toInt()
        isAutoCommit = environment.config.property("storage.dbcp.isAutoCommit").getString().toBoolean()
        validate()
    }
    val dataSource = HikariDataSource(config)

    // 핵심: Ktor Monitor를 통한 애플리케이션 종료 이벤트 구독
    // 서버가 완전히 정지된 시점(ApplicationStopped) 또는 정지 중인 시점(ApplicationStopping)에 커넥션 풀을 닫습니다.
    // 만약 서비스가 종료되는 과정에서 안전하게(Graceful) 커넥션을 끊고 싶다면 아래처럼 ApplicationStopping을 사용하셔도 좋습니다.
    monitor.subscribe (ApplicationStopping) {
        dataSource.close()
    }

    return dataSource
}

fun Application.configureDatabase() {

    val databaseName = environment.config.property("storage.dbName").getString()

    val database = with(databaseName) {
        when{
            startsWith("sqlite") -> {
                Database.connect(dataSource())
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

    fun initSqliteDbTable(db:Database){
        transaction (db){
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
                initSqliteDbTable(database)
            }
            else -> {
                initMemoryDb(database)
            }
        }

    }
}

