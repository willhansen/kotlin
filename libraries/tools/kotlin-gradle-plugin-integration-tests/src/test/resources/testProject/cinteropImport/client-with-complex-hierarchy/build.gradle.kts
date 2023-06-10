plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()
    linuxArm64("linuxArm").compilations.getByName("main").cinterops.create("w")
    linuxX64("linux").compilations.getByName("main").cinterops.create("w")

    sourceSets {
        konst commonMain by getting
        konst commonTest by getting
        konst linuxMain by getting
        konst linuxTest by getting
        konst linuxArmMain by getting
        konst linuxArmTest by getting

        konst nativeMain by creating {
            this.dependsOn(commonMain)
            linuxArmMain.dependsOn(this)
        }
        konst nativeTest by creating {
            this.dependsOn(commonTest)
            linuxArmTest.dependsOn(this)
        }

        konst linuxIntermediateMain by creating {
            this.dependsOn(nativeMain)
            linuxMain.dependsOn(this)

            dependencies {
                implementation(project(":dep-with-cinterop"))
            }
        }
        konst linuxIntermediateTest by creating {
            this.dependsOn(nativeTest)
            linuxTest.dependsOn(this)
        }
    }
}
