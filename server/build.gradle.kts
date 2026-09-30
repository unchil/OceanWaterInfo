plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
    alias(libs.plugins.kotlin.serialization)
}

group = "com.unchil.oceanwaterinfo"
version = "1.0.0"
application {
    mainClass.set("com.unchil.oceanwaterinfo.EnvInfoServerKt")

    val userHome = System.getProperty("user.home")
    val isDevelopment: Boolean = project.ext.has("development")
    val externalConfigPath = "$userHome/.EnvInfoServer/application.yaml"
    val externalLogFilePath = "$userHome/.EnvInfoServer/logback.xml"

    applicationDefaultJvmArgs = listOf(
        "-Dio.ktor.development=$isDevelopment" ,
        "-Dlogback.configurationFile=$externalLogFilePath",
    )
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


tasks.named<JavaExec>("run") {
    // 외부 application.yaml 파일 경로 지정
    args("-config=/Users/unchil/.EnvInfoServer/application.yaml")
}
