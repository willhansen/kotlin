plugins {
    kotlin("multiplatform")
}

kotlin {
    linuxArm64()
    linuxX64()

    konst commonMain by sourceSets.getting
    konst linuxMain by sourceSets.creating
    konst linuxArm64Main by sourceSets.getting
    konst linuxX64Main by sourceSets.getting

    linuxMain.dependsOn(commonMain)
    linuxArm64Main.dependsOn(linuxMain)
    linuxX64Main.dependsOn(linuxMain)
}
