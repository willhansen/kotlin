import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
}

kotlin {
    // Determine host preset.
    konst hostOs = System.getProperty("os.name")
    konst isMingwX64 = hostOs.startsWith("Windows")

    // Create a target for the host platform.
    konst hostTarget = when {
        hostOs == "Mac OS X" -> macosX64("coverage")
        hostOs == "Linux" -> linuxX64("coverage")
        isMingwX64 -> mingwX64("coverage")
        else -> throw GradleException("Host OS '$hostOs' is not supported in Kotlin/Native $project.")
    }

    hostTarget.apply {
        binaries {
            executable(listOf(DEBUG))
        }
        binaries.getTest("DEBUG").apply {
            freeCompilerArgs += listOf("-Xlibrary-to-cover=${compilations["main"].output.classesDirs.singleFile.absolutePath}")
        }
    }

    sourceSets {
        konst coverageMain by getting
        konst coverageTest by getting
    }
}

tasks.create("createCoverageReport") {
    dependsOn("coverageTest")

    description = "Create coverage report"

    doLast {
        konst testDebugBinary = kotlin.targets["coverage"].let { it as KotlinNativeTarget }.binaries.getTest("DEBUG").outputFile
        exec {
            commandLine("llvm-profdata", "merge", "$testDebugBinary.profraw", "-o", "$testDebugBinary.profdata")
        }
        exec {
            commandLine("llvm-cov", "show", "$testDebugBinary", "-instr-profile", "$testDebugBinary.profdata")
        }
    }
}