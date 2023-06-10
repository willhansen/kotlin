plugins {
    kotlin("multiplatform")
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        maven("<localRepo>")
    }
}

kotlin {
    jvm()
    linuxX64 {
        binaries { sharedLib() }
        compilations.getByName("main").apply {
            cinterops {
                konst foo by creating {
                    defFile(project.file("foo.def"))
                }
            }
        }
    }

    sourceSets {
        konst commonMain by getting {
            dependencies {
                implementation("test:lib:1.0")
            }
        }
    }
}
