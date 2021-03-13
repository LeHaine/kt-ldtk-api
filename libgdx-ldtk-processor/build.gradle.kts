
repositories {
    mavenCentral()
}

tasks.named<Jar>("jar") {
    manifest {
        attributes("Automatic-Module-Name" to "com.lehaine.libgdx-ldtk-processor")
    }
}

publishing {
    publications {
        create<MavenPublication>("libgdx-ldtk-processor") {
            artifactId = "libgdx-ldtk-processor"
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":ldtk-api"))
    implementation(project(":libgdx-backend"))
    implementation(project(":ldtk-processor"))
    implementation("com.google.auto.service:auto-service:1.0-rc7")
    kapt("com.google.auto.service:auto-service:1.0-rc7")
}