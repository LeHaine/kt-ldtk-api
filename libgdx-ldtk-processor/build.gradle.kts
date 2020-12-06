import org.jetbrains.kotlin.kapt3.base.Kapt.kapt

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
    implementation(project(":libgdx-backend"))
    implementation(project(":ldtk-processor"))
    implementation("com.google.auto.service:auto-service:1.0-rc7")
    kapt("com.google.auto.service:auto-service:1.0-rc7")
}


tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
    var kotlinPath = ""
    dependencies {
        kotlinPath = kotlin("stdlib").toString()
    }
    withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        minimize()
        dependencies {
            exclude(dependency(kotlinPath))
            exclude(project(":ldtk-api"))
            exclude(project(":libgdx-backend"))
            exclude("**/*.kotlin_*")
        }
        archiveClassifier.set("")
    }
}