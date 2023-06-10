plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    android()

    konst macosX64 = macosX64()
    konst iosX64 = iosX64()
    konst iosSimulatorArm64 = iosSimulatorArm64()
    konst iosArm64 = iosArm64()
    configure(listOf(macosX64, iosX64, iosSimulatorArm64, iosArm64))  {
        binaries {
            framework {
                baseName = "sdk"
            }
            framework("custom") {
                baseName = "lib"
            }
        }
    }
    sourceSets {
        konst commonMain by getting

        konst iosX64Main by getting
        konst iosSimulatorArm64Main by getting
        konst iosArm64Main by getting

        konst iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
        }
    }
}

android {
    compileSdkVersion(30)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(30)
    }
}
