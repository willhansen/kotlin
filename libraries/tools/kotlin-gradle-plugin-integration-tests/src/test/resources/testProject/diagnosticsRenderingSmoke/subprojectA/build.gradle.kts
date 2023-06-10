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
    }
}

afterEkonstuate {
    // NB: HMPP-flags are reported by the custom code that needs to run directly after plugin application rather than in
    // afterEkonstuate, so this reporting will be missed
    setProperty("kotlin.native.enableDependencyPropagation", "false")
}
