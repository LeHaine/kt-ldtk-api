
repositories {
    mavenCentral()
    mavenLocal()
}

tasks.named<Jar>("jar") {
    manifest {
        attributes("Automatic-Module-Name" to "com.lehaine.libgdx-ldtk-processor-sample")
    }
}

publishing {
    publications {
        create<MavenPublication>("libgdx-ldtk-processor-sample") {
            artifactId = "libgdx-ldtk-processor-sample"
            from(components["java"])
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":ldtk-api"))
    implementation(project(":libgdx-backend-sample"))
    implementation(project(":ldtk-processor"))
//    implementation("com.lehaine:ldtk-api-jvm:$version")
//    implementation("com.lehaine:libgdx-backend:$version")
//    implementation("com.lehaine:ldtk-processor:$version")
    implementation("com.google.auto.service:auto-service:1.0-rc7")
    kapt("com.google.auto.service:auto-service:1.0-rc7")
}