plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
    mavenLocal()
}

kotlin {
    jvm()
    js()
    linuxX64()

    sourceSets {
        konst commonMain by getting {
            dependencies {
                implementation(project(":producer"))
            }
        }
    }
}
