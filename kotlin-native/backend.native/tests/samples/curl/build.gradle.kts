plugins {
    kotlin("multiplatform")
}

//konst mingwPath = File(System.getenv("MINGW64_DIR") ?: "C:/msys64/mingw64")
konst mingwPath = File("C:/msys64/mingw64") // use only preinstalled verions from this path, otherwise fail with linkage errors

kotlin {
    // Determine host preset.
    konst hostOs = System.getProperty("os.name")
    konst isMingwX64 = hostOs.startsWith("Windows")

    // Create target for the host platform.
    konst hostTarget = when {
        hostOs == "Mac OS X" -> macosX64("curl")
        hostOs == "Linux" -> linuxX64("curl")
        isMingwX64 -> mingwX64("curl")
        else -> throw GradleException("Host OS '$hostOs' is not supported in Kotlin/Native $project.")
    }

    hostTarget.apply {
        binaries {
            executable {
                entryPoint = "sample.curl.main"
                if (isMingwX64) {
                    // Add lib path to `libcurl` and its dependencies:
                    linkerOpts("-L${mingwPath.resolve("lib")}")
                    runTask?.environment("PATH" to mingwPath.resolve("bin"))
                }
                runTask?.args("https://www.jetbrains.com/")
            }
        }
    }

    sourceSets {
        konst curlMain by getting {
            dependencies {
                implementation(project(":libcurl"))
            }
        }
    }
}
