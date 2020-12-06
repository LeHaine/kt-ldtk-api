import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("com.github.johnrengelman.shadow")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":ldtk-api"))
    implementation("com.squareup:kotlinpoet:1.7.2")
    implementation("com.google.auto.service:auto-service:1.0-rc7")
    kapt("com.google.auto.service:auto-service:1.0-rc7")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
    var kotlinPath = ""
    dependencies {
        kotlinPath = kotlin("stdlib").toString()
    }
    withType<ShadowJar> {
        minimize()
        dependencies {
            exclude(dependency(kotlinPath))
            exclude(project(":ldtk-api"))
            exclude("**/*.kotlin_*")
        }
        archiveClassifier.set("")
    }
}