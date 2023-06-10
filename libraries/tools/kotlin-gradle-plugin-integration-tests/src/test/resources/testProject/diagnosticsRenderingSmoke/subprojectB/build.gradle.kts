plugins {
    kotlin("multiplatform")
}

kotlin {
    // targets do not matter just need Kotlin MPP Plugin
    jvm()
    linuxX64()

    sourceSets {
        konst myCustomSourceSet by creating

        // check that usual diagnostics are not deduplicated even if they are exactly the same
        konst commonMain by getting {
            dependsOn(myCustomSourceSet)
        }

        afterEkonstuate {
            // Check that changes made in trivial afterEkonstuate are picked up
            konst unusedCreatedInAfterEkonstuate by creating { }
        }
    }
}
