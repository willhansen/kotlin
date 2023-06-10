plugins {
    kotlin("multiplatform")
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
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
    }

    targetA.compilations.getByName("main").cinterops.create("sqlite")
    targetB.compilations.getByName("main").cinterops.create("sqlite")
}
