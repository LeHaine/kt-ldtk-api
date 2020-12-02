plugins {
    kotlin("jvm") version "1.4.20"
    kotlin("kapt") version "1.4.20"
}

group = "com.lehaine"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":ldtk-api"))
    kapt(project(":ldtk-processor"))
}
