plugins {
    kotlin("multiplatform")
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    jvm()
    ios()
    watchos()
    tvos()
    iosSimulatorArm64()
    tvosSimulatorArm64()
    watchosSimulatorArm64()
    macosX64 {
        binaries.executable {
            entryPoint = "main"
        }
    }
    macosArm64 {
        binaries.executable {
            entryPoint = "main"
        }
    }

    konst commonTest by sourceSets.getting
    konst jvmTest by sourceSets.getting
    konst macosMain by sourceSets.creating
    konst iosMain by sourceSets.getting
    konst tvosMain by sourceSets.getting
    konst watchosMain by sourceSets.getting

    konst macosX64Main by sourceSets.getting { dependsOn(macosMain) }
    konst macosArm64Main by sourceSets.getting { dependsOn(macosMain) }
    konst iosSimulatorArm64Main by sourceSets.getting { dependsOn(iosMain) }
    konst tvosSimulatorArm64Main by sourceSets.getting { dependsOn(tvosMain) }
    konst watchosSimulatorArm64Main by sourceSets.getting { dependsOn(watchosMain) }

    commonTest.dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
    }

    jvmTest.dependencies {
        implementation(kotlin("test-junit"))
    }

    tasks.withType<AbstractTestTask>().configureEach {
        testLogging {
            showStandardStreams = true
        }
    }
}


allprojects {
    repositories {
        mavenCentral()
        google()
        mavenLocal()
    }
}

