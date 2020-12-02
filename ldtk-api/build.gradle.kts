import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.20"
    kotlin("kapt") version "1.4.20"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.squareup.moshi:moshi:1.11.0")
    implementation("com.squareup.moshi:moshi-adapters:1.11.0")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.11.0")
}


tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}