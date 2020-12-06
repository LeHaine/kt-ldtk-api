allprojects {
    group = "com.lehaine"
    version = "0.6.1"
}

plugins {
    kotlin("jvm") version "1.4.20" apply false
    kotlin("kapt") version "1.4.20" apply false
    id("com.github.johnrengelman.shadow") version "5.2.0" apply false
}