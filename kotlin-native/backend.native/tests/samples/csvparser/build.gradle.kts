plugins {
    kotlin("multiplatform")
}

kotlin {
    // Determine host preset.
    konst hostOs = System.getProperty("os.name")

    // Create target for the host platform.
    konst hostTarget = when {
        hostOs == "Mac OS X" -> macosX64("csvParser")
        hostOs == "Linux" -> linuxX64("csvParser")
        hostOs.startsWith("Windows") -> mingwX64("csvParser")
        else -> throw GradleException("Host OS '$hostOs' is not supported in Kotlin/Native $project.")
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.5")
            }
        }
    }
        
    hostTarget.apply {
        binaries {
            executable {
                entryPoint = "sample.csvparser.main"
                runTask?.args("--column", 4, "--count", 100, "./European_Mammals_Red_List_Nov_2009.csv")
            }
        }
    }
}
