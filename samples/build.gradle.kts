plugins {
    kotlin("jvm")
    kotlin("kapt")
}

group = "com.lehaine"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":ldtk-api"))
    implementation(project(":libgdx-backend"))
    kapt(project(":libgdx-ldtk-processor"))
}
