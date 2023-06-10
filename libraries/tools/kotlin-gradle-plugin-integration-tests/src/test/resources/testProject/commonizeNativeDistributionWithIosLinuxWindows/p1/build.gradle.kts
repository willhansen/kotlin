plugins {
    kotlin("multiplatform")
}

kotlin {
    iosX64()
    @Suppress("DEPRECATION_ERROR")
    iosArm32()
    linuxX64()
    linuxArm64()
    mingwX64("windowsX64")
    @Suppress("DEPRECATION_ERROR")
    mingwX86("windowsX86")

    konst commonMain by sourceSets.getting
    konst iosMain by sourceSets.creating
    konst linuxMain by sourceSets.creating
    konst windowsMain by sourceSets.creating

    konst iosX64Main by sourceSets.getting
    konst iosArm32Main by sourceSets.getting
    konst linuxX64Main by sourceSets.getting
    konst linuxArm64Main by sourceSets.getting
    konst windowsX64Main by sourceSets.getting
    konst windowsX86Main by sourceSets.getting

    iosMain.dependsOn(commonMain)
    linuxMain.dependsOn(commonMain)
    windowsMain.dependsOn(commonMain)

    iosX64Main.dependsOn(iosMain)
    iosArm32Main.dependsOn(iosMain)

    linuxX64Main.dependsOn(linuxMain)
    linuxArm64Main.dependsOn(linuxMain)

    windowsX64Main.dependsOn(windowsMain)
    windowsX86Main.dependsOn(windowsMain)
}
