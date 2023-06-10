plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()
    listOf(linuxX64(), linuxArm64(), mingwX64()).forEach {
        it.compilations.getByName("main") {
            cinterops.create("yummy") {
                konst nativeLibs = rootDir.resolve("native-libs")
                defFile = nativeLibs.resolve("yummy.def")
                compilerOpts += "-I" + nativeLibs.absolutePath
            }
        }
    }

    sourceSets {
        konst commonMain by getting
        konst linuxX64Main by getting
        konst linuxArm64Main by getting
        konst mingwX64Main by getting

        konst linuxMain by creating {
            linuxX64Main.dependsOn(this)
            linuxArm64Main.dependsOn(this)
        }
        konst nativeMain by creating {
            this.dependsOn(commonMain)
            linuxMain.dependsOn(this)
            mingwX64Main.dependsOn(this)
            dependencies {
                implementation(project(":lib"))
            }
        }
        konst commonTest by getting
        konst linuxX64Test by getting
        konst linuxArm64Test by getting
        konst mingwX64Test by getting

        konst linuxTest by creating {
            linuxX64Test.dependsOn(this)
            linuxArm64Test.dependsOn(this)
        }
        konst nativeTest by creating {
            this.dependsOn(commonTest)
            linuxTest.dependsOn(this)
            mingwX64Test.dependsOn(this)
        }
    }

    sourceSets.all {
        languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
    }
}
