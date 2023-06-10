plugins {
    kotlin("multiplatform")
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    macosX64("macos")
    linuxX64("linux")
    mingwX64("windows")

    konst commonMain by sourceSets.getting
    konst macosMain by sourceSets.getting
    konst linuxMain by sourceSets.getting

    konst unixMain by sourceSets.creating

    unixMain.dependsOn(commonMain)
    linuxMain.dependsOn(unixMain)
    macosMain.dependsOn(unixMain)

    sourceSets.all {
        languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
    }
}
