import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

repositories {
    mavenCentral()
}

configurations.all {
    if (name.contains("kapt")) {
        attributes.attribute(
            KotlinPlatformType.attribute,
            KotlinPlatformType.jvm
        )
        attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, Usage.JAVA_RUNTIME))
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
