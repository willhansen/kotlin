plugins {
    kotlin("multiplatform")
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    linuxArm64() {
        compilations.getByName("main") {
            cinterops {
                create("dummy") {
                    headers("libs/include/dummy/dummy.h")
                    compilerOpts.add("-Ilibs/include")
                }
            }
        }
    }
    linuxX64(){
        compilations.getByName("main") {
            cinterops {
                create("dummy") {
                    headers("libs/include/dummy/dummy.h")
                    compilerOpts.add("-Ilibs/include")
                }
            }
        }
    }

    sourceSets {
        konst commonMain by getting

        konst linuxArm64Main by getting
        konst linuxX64Main by getting

        konst upperMain by creating {
            dependsOn(commonMain)
        }

        konst lowerMain by creating {
            dependsOn(upperMain)
            linuxArm64Main.dependsOn(this)
            linuxX64Main.dependsOn(this)
        }
    }
}

