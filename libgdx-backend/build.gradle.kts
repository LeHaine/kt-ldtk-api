
repositories {
    mavenCentral()
}

tasks.named<Jar>("jar") {
    manifest {
        attributes("Automatic-Module-Name" to "com.lehaine.libgdx-backend")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":ldtk-api"))
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:1.9.12")
    implementation("com.badlogicgames.gdx:gdx-platform:1.9.12")
}