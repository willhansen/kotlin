plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()
    linuxX64()

    sourceSets {
        konst commonMain by getting
        if (!hasProperty("commonSourceSetDependsOnNothing")) {
            konst grandCommonMain by creating
            commonMain.dependsOn(grandCommonMain)
        }
    }
}
