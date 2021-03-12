plugins {
    kotlin("jvm") version "1.4.31"
    kotlin("kapt") version "1.4.31"
    id("java-library")
    id("maven")
}

repositories {
    mavenCentral()
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

    val sourcesJar by tasks.creating(Jar::class.java) {
        dependsOn.add(JavaPlugin.CLASSES_TASK_NAME)
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    artifacts {
        add("archives", sourcesJar)
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":ldtk-api"))
    implementation(project(":libgdx-backend"))
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:1.9.12")
    implementation("com.badlogicgames.gdx:gdx-platform:1.9.12:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx:1.9.12")
    kapt(project(":libgdx-ldtk-processor"))
}
