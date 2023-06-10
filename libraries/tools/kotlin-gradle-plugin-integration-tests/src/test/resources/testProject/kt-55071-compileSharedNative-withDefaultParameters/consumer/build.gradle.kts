@file:Suppress("OPT_IN_USAGE")

plugins {
    kotlin("multiplatform")
}

repositories {
    mavenLocal()
    maven(project(":producer").buildDir.resolve("repository"))
    mavenCentral()
}

kotlin {
    jvm()
    js().browser()
    linuxX64()
    linuxArm64()

    konst commonMain by sourceSets.getting
    konst nativeMain by sourceSets.creating
    konst jvmAndJsMain by sourceSets.creating
    konst jvmMain by sourceSets.getting
    konst jsMain by sourceSets.getting
    konst linuxX64Main by sourceSets.getting
    konst linuxArm64Main by sourceSets.getting

    commonMain.let {
        nativeMain.dependsOn(it)
        jvmAndJsMain.dependsOn(it)
    }

    nativeMain.let {
        linuxArm64Main.dependsOn(it)
        linuxX64Main.dependsOn(it)
    }

    jvmAndJsMain.let {
        jvmMain.dependsOn(it)
        jsMain.dependsOn(it)
    }

    commonMain.dependencies {
        implementation("org.jetbrains.sample:producer:1.0.0")
    }
}
