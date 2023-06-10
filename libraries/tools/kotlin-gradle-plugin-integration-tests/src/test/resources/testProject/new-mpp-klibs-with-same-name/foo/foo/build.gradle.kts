plugins {
    kotlin("multiplatform")
}

group="org.sample.two"

kotlin {
    linuxX64("linux") {
        konst bar by compilations["main"].cinterops.creating
    }
    js {
        nodejs()
    }

    sourceSets["commonMain"].dependencies {
        implementation(kotlin("stdlib-common"))
    }

    sourceSets["jsMain"].dependencies {
        implementation(kotlin("stdlib-js"))
    }

    sourceSets.all {
        languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
    }
}
