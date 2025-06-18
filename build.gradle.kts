import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag

plugins {
    kotlin("multiplatform") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"
    id("org.jetbrains.compose") version "1.8.0-alpha03"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.20-RC"
}

group = "kiwi.hoonkun.app.editor.dungeons"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
    jvm()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.components.resources)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.components.resources)
                implementation("org.json:json:20240303")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.0")
                implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.7")
                implementation(project(":PakReader"))
                implementation(kotlin("reflect"))
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test:1.5.31")
                implementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.AppImage)
            packageName = "DungeonsEditor"
            packageVersion = "1.2.2"
            linux {
                iconFile.set(project.file("src/jvmMain/resources/_icon_256x256.png"))
            }
            windows {
                iconFile.set(project.file("src/jvmMain/resources/_icon.ico"))
                upgradeUuid = "effd1d5e-d6c3-4057-a210-5f115f619126"
            }
        }
        buildTypes.release.proguard {
            configurationFiles.from("proguard-rules.pro")
            version.set("7.6.1")
        }
    }
}

composeCompiler {
    featureFlags.addAll(ComposeFeatureFlag.PausableComposition)
}
