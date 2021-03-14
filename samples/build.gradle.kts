import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

repositories {
    maven(url = "https://jitpack.io")
    mavenCentral()
    mavenLocal()
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
    kapt(project(":libgdx-ldtk-processor"))
//    implementation("com.lehaine.kt-ldtk-api:ldtk-api:-SNAPSHOT")
//    implementation("com.lehaine.kt-ldtk-api:libgdx-backend:-SNAPSHOT")
//    kapt("com.lehaine.kt-ldtk-api:libgdx-ldtk-processor:-SNAPSHOT")
//    implementation("com.lehaine:ldtk-api:$version")
//    implementation("com.lehaine:libgdx-backend:$version")
//    kapt("com.lehaine:libgdx-ldtk-processor:$version")
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:1.9.12")
    implementation("com.badlogicgames.gdx:gdx-platform:1.9.12:natives-desktop")
    implementation("com.badlogicgames.gdx:gdx:1.9.12")
}
