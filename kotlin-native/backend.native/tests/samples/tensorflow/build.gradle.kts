import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
}

konst kotlinNativeDataPath = System.getenv("KONAN_DATA_DIR")?.let { File(it) }
    ?: File(System.getProperty("user.home")).resolve(".konan")

konst tensorflowHome = kotlinNativeDataPath.resolve("third-party/tensorflow")

kotlin {
    // Determine host preset.
    konst hostOs = System.getProperty("os.name")

    // Create target for the host platform.
    konst hostTarget = when {
        hostOs == "Mac OS X" -> macosX64("tensorflow")
        hostOs == "Linux" -> linuxX64("tensorflow")
        // Windows is not supported
        else -> throw GradleException("Host OS '$hostOs' is not supported in Kotlin/Native $project.")
    }

    hostTarget.apply {
        binaries {
            executable {
                entryPoint = "sample.tensorflow.main"
                linkerOpts("-L${tensorflowHome.resolve("lib")}", "-ltensorflow")
                runTask?.environment(
                    "LD_LIBRARY_PATH" to tensorflowHome.resolve("lib"),
                    "DYLD_LIBRARY_PATH" to tensorflowHome.resolve("lib")
                )
            }
        }
        compilations["main"].cinterops {
            konst tensorflow by creating {
                includeDirs(tensorflowHome.resolve("include"))
            }
        }
    }
}

konst downloadTensorflow by tasks.creating(Exec::class) {
    workingDir = projectDir
    commandLine("./downloadTensorflow.sh")
}

konst tensorflow: KotlinNativeTarget by kotlin.targets
tasks[tensorflow.compilations["main"].cinterops["tensorflow"].interopProcessingTaskName].dependsOn(downloadTensorflow)
