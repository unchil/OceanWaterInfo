package com.unchil.oceanwaterinfo

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

fun Application.configureDatabase() {

    val databaseName = environment.config.property("storage.dbName").getString()

    val database = with(databaseName) {
        when{
            startsWith("sqlite") -> {
                val config = HikariConfig().apply {
                    jdbcUrl = environment.config.property("storage.database.sqlite.jdbcURL").getString()
                    driverClassName = environment.config.property("storage.database.sqlite.driverClassName").getString()
                    maximumPoolSize = environment.config.property("storage.dbcp.maxPoolSize").getString().toInt()
                    isAutoCommit = environment.config.property("storage.dbcp.isAutoCommit").getString().toBoolean()
                    validate()
                }
                val dataSource = HikariDataSource(config)
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

    fun initSqliteDbTable(db:Database){
        transaction (db){
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

