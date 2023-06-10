plugins {
    kotlin("multiplatform")
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    jvm() // https://youtrack.jetbrains.com/issue/KT-45832
    konst targetA = <targetA>("targetA")
    konst targetB = <targetB>("targetB")

    konst commonMain by sourceSets.getting
    konst nativeMain by sourceSets.creating
    konst targetAMain by sourceSets.getting
    konst targetBMain by sourceSets.getting

    nativeMain.dependsOn(commonMain)
    targetAMain.dependsOn(nativeMain)
    targetBMain.dependsOn(nativeMain)

    sourceSets.all {
        languageSettings.optIn("kotlin.RequiresOptIn")
        languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
    }

    targetA.compilations.getByName("main").cinterops.create("withPosix") {
        header(file("libs/withPosix.h"))
    }
    targetB.compilations.getByName("main").cinterops.create("withPosix") {
        header(file("libs/withPosix.h"))
    }
}
