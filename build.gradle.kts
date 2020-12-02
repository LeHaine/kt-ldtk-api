allprojects {
    group = "com.lehaine"
    version = "1.0-SNAPSHOT"
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven")

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8

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