plugins {
    kotlin("jvm") version "1.4.31"
    kotlin("kapt") version "1.4.31"
    id("java-library")
    id("maven")
}

repositories {
    mavenCentral()
}

tasks.named<Jar>("jar") {
    manifest {
        attributes("Automatic-Module-Name" to "com.lehaine.ldtk-processor")
    }
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
    implementation("com.squareup:kotlinpoet:1.7.2")
    implementation("com.google.auto.service:auto-service:1.0-rc7")
    kapt("com.google.auto.service:auto-service:1.0-rc7")
}