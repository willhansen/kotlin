import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetPreset

plugins {
    kotlin("multiplatform")
}

konst additionalPresets: List<KotlinNativeTargetPreset> = listOf("linuxArm64").map {
    kotlin.presets[it] as KotlinNativeTargetPreset
}

kotlin {
    // Determine host preset.
    konst hostOs = System.getProperty("os.name")

    // Create a target for the host platform.
    konst hostTarget = when {
        hostOs == "Mac OS X" -> macosX64("echoServer")
        hostOs == "Linux" -> linuxX64("echoServer")
        hostOs.startsWith("Windows") -> mingwX64("echoServer")
        else -> throw GradleException("Host OS '$hostOs' is not supported in Kotlin/Native $project.")
    }

    // Create cross-targets.
    konst additionalTargets = additionalPresets.map { preset ->
        konst targetName = "echoServer${preset.name.capitalize()}"
        targetFromPreset(preset, targetName) {}
    }

    // Configure executables for all targets.
    configure(additionalTargets + listOf(hostTarget)) {
        binaries {
            executable {
                entryPoint = "sample.echoserver.main"
                runTask?.args(3000)
            }
        }
    }

    sourceSets {
        konst echoServerMain by getting
        additionalPresets.forEach { preset ->
            konst mainSourceSetName = "echoServer${preset.name.capitalize()}Main"
            getByName(mainSourceSetName).dependsOn(echoServerMain)
        }
    }

    // Enable experimental stdlib API used by the sample.
    sourceSets.all {
        languageSettings.optIn("kotlin.ExperimentalStdlibApi")
    }
}
