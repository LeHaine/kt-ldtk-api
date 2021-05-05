repositories {
    mavenCentral()
    mavenLocal()
}

tasks.named<Jar>("jar") {
    manifest {
        attributes("Automatic-Module-Name" to "com.lehaine.libgdx-backend-sample")
    }
}

publishing {
    publications {
        create<MavenPublication>("libgdx-backend-sample") {
            artifactId = "libgdx-backend-sample"
            from(components["java"])
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":ldtk-api"))
    // implementation("com.lehaine:ldtk-api:$version")
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:1.9.12")
    implementation("com.badlogicgames.gdx:gdx-platform:1.9.12")
}