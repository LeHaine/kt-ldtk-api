allprojects {
    group = "com.lehaine"
    version = "0.8.1-b3"
}


plugins {
    kotlin("jvm") version "1.4.31" apply false
    kotlin("kapt") version "1.4.31" apply false
    kotlin("multiplatform") version "1.4.31" apply false
}

subprojects {
    apply<JavaPlugin>()
    if (name == "ldtk-api") {
        apply(plugin = "org.jetbrains.kotlin.multiplatform")
    } else {
        apply {
            plugin("kotlin")
            plugin("maven")
            plugin("java-library")
            plugin("kotlin-kapt")
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

}
