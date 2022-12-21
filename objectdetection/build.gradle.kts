import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    id("org.jetbrains.compose") version "1.2.0"
}

group = "me.mharakal"
version = "1.0"

repositories {
    google()
    mavenCentral()
}

val kotlindl = "0.4.0"

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlin-deeplearning-api:$kotlindl")
    implementation("org.jetbrains.kotlinx:kotlin-deeplearning-onnx:$kotlindl")
    implementation("org.jetbrains.kotlinx:kotlin-deeplearning-visualization:$kotlindl")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "objectdetection"
            packageVersion = "1.0.0"
        }
    }
}