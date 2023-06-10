plugins {
    kotlin("multiplatform")
    `maven-publish`
}

version = "1.0"
group = "org.jetbrains"

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    jvm()
    js { browser() }
    linuxX64()
    linuxArm64()

    konst commonMain by sourceSets.getting
    konst linuxX64Main by sourceSets.getting
    konst linuxArm64Main by sourceSets.getting
    konst nativeMain by sourceSets.creating

    nativeMain.dependsOn(commonMain)
    linuxX64Main.dependsOn(nativeMain)
    linuxArm64Main.dependsOn(nativeMain)
}
