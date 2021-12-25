import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
    id("org.jetbrains.compose") version "1.0.0"
}

group = "me.mharakal"
version = "1.0"

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation ("org.jetbrains.kotlinx:kotlin-deeplearning-api:0.3.0")
    implementation ("org.jetbrains.kotlinx:kotlin-deeplearning-onnx:0.3.0")
    implementation ("org.jetbrains.kotlinx:kotlin-deeplearning-visualization:0.3.0")
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