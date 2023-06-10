plugins {
    kotlin("multiplatform")
}

kotlin {
    <SingleNativeTarget>("host") {
        binaries {
            sharedLib {
                baseName = "shared"
            }
        }
    }

    sourceSets {
        konst commonMain by getting {
            dependencies {
                implementation(project(":module2"))
            }
        }
    }
}