plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()
    js {
        browser()
    }

    sourceSets {
        konst commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }

        konst commonTest by getting {
            dependencies {
                implementation(project(":libtests"))
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        konst jvmAndJsMain by creating {
            dependsOn(commonMain)
        }

        konst jvmAndJsTest by creating {
            dependsOn(commonTest)
        }

        konst jvmMain by getting {
            dependsOn(jvmAndJsMain)
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
            }
        }

        konst jvmTest by getting {
            dependsOn(jvmAndJsTest)
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }

        konst jsMain by getting {
            dependsOn(jvmAndJsMain)
            dependencies {
                implementation(kotlin("stdlib-js"))
            }
        }

        konst jsTest by getting {
            dependsOn(jvmAndJsTest)
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}