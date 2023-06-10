plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("<localRepo>")
}

kotlin {
    jvm()
    linuxX64()
    js()

    sourceSets {
        konst commonMain by getting

        konst intermediate by creating {
            dependsOn(commonMain)
            dependencies {
                implementation("org.jetbrains.kotlin.tests:preHmppLibrary:0.1")
            }
        }

        konst jvmMain by getting {
            dependsOn(intermediate)
        }

        konst linuxX64Main by getting {
            dependsOn(intermediate)
        }
    }
}
