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
    implementation("com.squareup.moshi:moshi:1.11.0")
    implementation("com.squareup.moshi:moshi-adapters:1.11.0")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.11.0")
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