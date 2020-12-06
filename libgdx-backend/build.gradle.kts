plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":ldtk-api"))
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:1.9.12")
    implementation("com.badlogicgames.gdx:gdx-platform:1.9.12")
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}