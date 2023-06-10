import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform") version "1.8.0"
    kotlin("plugin.serialization") version "1.6.20"
    id("org.jetbrains.compose")
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
    jvm {
        jvmToolchain(11)
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("org.json:json:20230227")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
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
            targetFormats(TargetFormat.Exe, TargetFormat.AppImage)
            packageName = "DungeonsEditor"
            packageVersion = "1.0.0"
            linux {
                iconFile.set(project.file("src/jvmMain/resources/_icon.png"))
            }
            windows {
                iconFile.set(project.file("src/jvmMain/resources/_icon.ico"))
                upgradeUuid = "effd1d5e-d6c3-4057-a210-5f115f619126"
            }
        }
        buildTypes.release.proguard {
            configurationFiles.from("proguard-rules.pro")
        }
    }
}
