plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
    alias(libs.plugins.kotlin.serialization)
}

group = "com.unchil.oceanwaterinfo"
version = "1.0.0"
application {
    mainClass.set("com.unchil.oceanwaterinfo.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)


    implementation(libs.ktor.serverHeaders)
    implementation(libs.ktor.serverConfigYaml)
    implementation(libs.ktor.serverNegotiation)
    implementation(libs.ktor.serializationJsonJvm)

    implementation(libs.kotlinx.serialization)
    implementation(libs.sqlite)
    implementation(libs.h2)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)

    implementation(libs.hikaricp)
}