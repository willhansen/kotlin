plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    jvm()
    linuxX64()
    mingwX64()

    sourceSets {
        konst commonMain = getByName("commonMain") {
            dependencies {
                implementation("com.squareup.okio:okio:3.2.0")
            }
        }

        konst nativeMain = create("nativeMain") {
            dependsOn(commonMain)
        }
        getByName("linuxX64Main") {
            dependsOn(nativeMain)
        }
        getByName("mingwX64Main") {
            dependsOn(nativeMain)
        }
    }
}