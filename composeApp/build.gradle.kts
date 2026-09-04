import org.gradle.kotlin.dsl.implementation
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm()




    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)




        }
        commonMain.dependencies {
            implementation(projects.shared)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(libs.kotlinx.datetime)

            implementation(libs.un7datagrid)
            implementation(libs.koalaplot.core)
            implementation(libs.geojson)
            implementation(libs.compose.treeview)
            implementation(libs.compose.adaptive)
            implementation(libs.compose.adaptive.layout)

            implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")

            implementation(libs.compose.webview.multiplatform)


        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        iosMain.dependencies {

        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(compose.desktop.currentOs)


            implementation(libs.ktor.clientLogging)
            implementation(libs.logback)

        }
    }
}

android {
    namespace = "com.unchil.oceanwaterinfo"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.unchil.oceanwaterinfo"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "com.unchil.oceanwaterinfo.EnvObserverKt"

        jvmArgs(
            "--add-opens=java.desktop/sun.awt=ALL-UNNAMED",
            "--add-opens=java.desktop/java.awt.peer=ALL-UNNAMED",
            "--add-opens=java.desktop/java.awt=ALL-UNNAMED", // 추가 권장
            "--add-exports=java.desktop/sun.awt=ALL-UNNAMED" // AccessError일 경우 export도 도움됨
        )

        if (System.getProperty("os.name").contains("Mac")) {
            jvmArgs(
                "--add-opens=java.desktop/sun.lwawt=ALL-UNNAMED",
                "--add-opens=java.desktop/sun.lwawt.macosx=ALL-UNNAMED"
            )
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "EnvironmentalObservation"
            packageVersion = "1.0.0"

            val iconsRoot = project.projectDir.resolve("src/jvmMain/resources/common")
            macOS {
                iconFile.set(iconsRoot.resolve("app_icon_1024.icns"))
                bundleID = "com.unchil.oceanwaterinfo"
            }

            windows {
                iconFile.set(iconsRoot.resolve("app_icon_1024.ico"))
                // 설치 프로그램의 아이콘 등도 개별 설정 가능
            }

            linux {
                iconFile.set(iconsRoot.resolve("app_icon_1024.png"))
            }
            javaHome="/Users/unchil/Library/Java/JavaVirtualMachines/jbr-21.0.11/Contents/Home"

        }


    }
}

