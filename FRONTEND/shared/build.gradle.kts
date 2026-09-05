@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    jvm()

    js(IR) {
        browser()
    }

    wasmJs {
        browser()
    }

    android {
        namespace = "org.com.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        // =========================
        // COMMON MAIN
        // =========================
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)

            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Ktor
            implementation("io.ktor:ktor-client-core:3.2.3")
            implementation("io.ktor:ktor-client-content-negotiation:3.2.3")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.2.3")
            implementation("io.ktor:ktor-client-logging:3.2.3")

            implementation(libs.compottie)
            implementation(libs.compottie.resources)
            implementation(libs.androidx.navigation.compose)

            implementation("com.mohamedrejeb.calf:calf-file-picker:0.5.3")
            implementation("com.mohamedrejeb.calf:calf-io:0.5.3")
            implementation("com.google.code.gson:gson:2.10.1")

            implementation(libs.kamel.image)
            implementation(compose.materialIconsExtended)
            implementation(libs.kotlinx.serialization.json)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        val iosArm64Main by getting {
            dependsOn(iosMain.get())
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain.get())
        }

        // =========================
        // ANDROID
        // =========================
        androidMain.dependencies {
            implementation("io.ktor:ktor-client-okhttp:3.2.3")

            // Google Maps
            implementation("com.google.maps.android:maps-compose:4.4.1")
            implementation("com.google.android.gms:play-services-maps:19.0.0")
            implementation("com.google.android.gms:play-services-location:21.3.0")

            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
        }
        // =========================
        // JVM / DESKTOP
        // =========================
        jvmMain.dependencies {
            implementation("io.ktor:ktor-client-cio:3.2.3")
            implementation(compose.desktop.currentOs)

            implementation(libs.maplibre.compose)

            runtimeOnly(
                "org.maplibre.compose:maplibre-native-bindings-jni:0.11.0"
            ) {
                capabilities {
                    requireCapability(
                        "org.maplibre.compose:maplibre-native-bindings-jni-linux-amd64-opengl"
                    )
                }
            }
        }

        // =========================
        // JS
        // =========================
        jsMain.dependencies {
            implementation("io.ktor:ktor-client-js:3.2.3")
            implementation(libs.wrappers.browser)

            implementation(libs.maplibre.compose)
            implementation(libs.maplibre.compose.material3)
            implementation(libs.spatialk.geojson)
        }

        // =========================
        // iOS
        // =========================
        iosMain.dependencies {
            implementation(libs.maplibre.compose)
            implementation(libs.maplibre.compose.material3)
            implementation(libs.spatialk.geojson)
        }

        // =========================
        // WASM
        // =========================
        wasmJsMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-browser:0.3")

            // No MapLibre
        }
    }
}