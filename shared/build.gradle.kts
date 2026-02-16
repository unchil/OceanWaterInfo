
import org.gradle.kotlin.dsl.implementation
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    iosArm64()
    iosSimulatorArm64()

    jvm()

    js {
        browser()
    }



    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            // put your Multiplatform dependencies here
            implementation(libs.kotlinx.serialization)
            implementation(libs.ktor.clientCore)
            implementation(libs.ktor.clientLogging)
            implementation(libs.ktor.clientNegotiation)
            implementation(libs.ktor.serializationJson)

        }

        androidMain.dependencies {
            implementation(libs.ktor.clientCio)
        }

        jvmMain.dependencies {
            implementation(libs.ktor.clientCio)
            implementation(libs.logback)
        }

        wasmJsMain.dependencies {
            implementation(libs.ktor.clientCio)
        }

        iosMain.dependencies {
            implementation(libs.ktor.clientDarwin)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.unchil.oceanwaterinfo.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
