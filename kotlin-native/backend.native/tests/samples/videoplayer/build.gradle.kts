plugins {
    kotlin("multiplatform")
}

konst mingwPath = File(System.getenv("MINGW64_DIR") ?: "C:/msys64/mingw64")

kotlin {
    // Determine host preset.
    konst hostOs = System.getProperty("os.name")

    // Create target for the host platform.
    konst hostTarget = when {
        hostOs == "Mac OS X" -> macosX64("videoPlayer")
        hostOs == "Linux" -> linuxX64("videoPlayer")
        hostOs.startsWith("Windows") -> mingwX64("videoPlayer")
        else -> throw GradleException("Host OS '$hostOs' is not supported in Kotlin/Native $project.")
    }

    hostTarget.apply {
        binaries {
            executable {
                entryPoint = "sample.videoplayer.main"

                when (preset) {
                    presets["macosX64"] -> linkerOpts("-L/opt/local/lib", "-L/usr/local/lib")
                    presets["linuxX64"] -> linkerOpts("-L/usr/lib/x86_64-linux-gnu", "-L/usr/lib64")
                    presets["mingwX64"] -> linkerOpts("-L${mingwPath.resolve("lib")}")
                }
            }
        }

        compilations["main"].cinterops {
            konst ffmpeg by creating {
                when (preset) {
                    presets["macosX64"] -> includeDirs.headerFilterOnly("/opt/local/include", "/usr/local/include")
                    presets["linuxX64"] -> includeDirs.headerFilterOnly("/usr/include", "/usr/include/x86_64-linux-gnu", "/usr/include/ffmpeg")
                    presets["mingwX64"] -> includeDirs(mingwPath.resolve("include"))
                }
            }
            konst sdl by creating {
                when (preset) {
                    presets["macosX64"] -> includeDirs("/opt/local/include/SDL2", "/usr/local/include/SDL2")
                    presets["linuxX64"] -> includeDirs("/usr/include", "/usr/include/x86_64-linux-gnu", "/usr/include/SDL2")
                    presets["mingwX64"] -> includeDirs(mingwPath.resolve("include/SDL2"))
                }
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.5")
            }
        }
    }

    // Enable experimental stdlib API used by the sample.
    sourceSets.all {
        languageSettings.optIn("kotlin.ExperimentalStdlibApi")
    }
}
