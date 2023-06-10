import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    kotlin("multiplatform")
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    konst xcf = XCFramework()
    konst otherXCFramework = XCFramework("other")

    konst iosX64 = iosX64()
    konst iosArm64 = iosArm64()
    konst iosSimulatorArm64 = iosSimulatorArm64()

    listOf(iosX64, iosArm64, iosSimulatorArm64).forEach { target ->
        target.binaries.framework {
            baseName = "shared"
            xcf.add(this)
            if (target == iosX64 || target == iosArm64) {
                otherXCFramework.add(this)
            }
        }
    }
    listOf(
        watchosArm32(),
        watchosArm64(),
        watchosDeviceArm64(),
        watchosSimulatorArm64(),
        watchosX64()
    ).forEach { target ->
        target.binaries.framework {
            baseName = "shared"
            xcf.add(this)
        }
    }
}
