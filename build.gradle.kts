allprojects {
    group = "com.lehaine"
    version = "0.6.1"
}


plugins {
    kotlin("jvm") version "1.4.20" apply false
    kotlin("kapt") version "1.4.20" apply false
}

subprojects {
    apply {
        plugin("java-library")
        plugin("maven")
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
}
