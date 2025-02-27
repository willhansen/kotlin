plugins {
    kotlin("multiplatform")
}

kotlin {
    // Determine host preset.
    konst hostOs = System.getProperty("os.name")

    // Create target for the host platform.
    konst hostTarget = when {
        hostOs == "Mac OS X" -> macosX64("workers")
        hostOs == "Linux" -> linuxX64("workers")
        hostOs.startsWith("Windows") -> mingwX64("workers")
        else -> throw GradleException("Host OS '$hostOs' is not supported in Kotlin/Native $project.")
    }

    hostTarget.apply {
        binaries {
            executable {
                entryPoint = "sample.workers.main"
            }
        }
    }
}