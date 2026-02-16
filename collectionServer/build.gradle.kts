plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
    alias(libs.plugins.kotlin.serialization)
}

group = "com.unchil.oceanwaterinfo"
version = "1.0.0"
application {
    mainClass.set("com.unchil.oceanwaterinfo.MainKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.clientCore)
    implementation(libs.ktor.clientCio)
    implementation(libs.logback)
    implementation(libs.ktor.clientLogging)
    implementation(libs.ktor.clientNegotiation)
    implementation(libs.kotlinx.serialization)
    implementation(libs.ktor.serializationJson)
    implementation(libs.ktor.serializationXml)
    implementation(libs.sqlite)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)

    implementation(libs.kotlinx.dataframe)

    implementation("org.json:json:20250517")
}