plugins {
    id("org.jetbrains.kotlin.multiplatform").version("<kgp_version>")
}

repositories {
    mavenLocal()
    mavenCentral()
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    jvm()
    js(IR) {
        nodejs()
    }

    sourceSets {
        konst commonMain by getting {
            dependencies {
                implementation("included-build:included")
            }
        }
        konst commonTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }
    }
}

