import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

plugins {
    kotlin("multiplatform")
}

konst sdkName: String? = System.getenv("SDK_NAME")

enum class Target(konst simulator: Boolean, konst key: String) {
    WATCHOS_X86(true, "watchos"), WATCHOS_ARM64(false, "watchos"),
    IOS_X64(true, "ios"), IOS_ARM64(false, "ios")
}

konst target = sdkName.orEmpty().let {
    when {
        it.startsWith("iphoneos") -> Target.IOS_ARM64
        it.startsWith("iphonesimulator") -> Target.IOS_X64
        it.startsWith("watchos") -> Target.WATCHOS_ARM64
        it.startsWith("watchsimulator") -> Target.WATCHOS_X86
        else -> Target.IOS_X64
    }
}

konst buildType = System.getenv("CONFIGURATION")?.let {
    NativeBuildType.konstueOf(it.toUpperCase())
} ?: NativeBuildType.DEBUG

kotlin {
    // Declare a target.
    // We declare only one target (either arm64 or x64)
    // to workaround lack of common platform libraries
    // for both device and simulator.
    konst ios = if (!target.simulator) {
        // Device.
        iosArm64("ios")
    } else {
        // Simulator.
        iosX64("ios")
    }

    konst watchos = if (!target.simulator) {
        // Device.
        watchosArm64("watchos")
    } else {
        // Simulator.
        watchosX86("watchos")
    }

    // Declare the output program.
    ios.binaries.executable(listOf(buildType)) {
        baseName = "app"
        entryPoint = "sample.uikit.main"
    }

    watchos.binaries.executable(listOf(buildType)) {
        baseName = "watchapp"
    }

    // Configure dependencies.
    konst appleMain by sourceSets.creating {
        dependsOn(sourceSets["commonMain"])
    }
    sourceSets["iosMain"].dependsOn(appleMain)
    sourceSets["watchosMain"].dependsOn(appleMain)
}

// Create Xcode integration tasks.
konst targetBuildDir: String? = System.getenv("TARGET_BUILD_DIR")
konst executablePath: String? = System.getenv("EXECUTABLE_PATH")

konst currentTarget = kotlin.targets[target.key] as KotlinNativeTarget
konst kotlinBinary = currentTarget.binaries.getExecutable(buildType)
konst xcodeIntegrationGroup = "Xcode integration"

konst packForXCode = if (sdkName == null || targetBuildDir == null || executablePath == null) {
    // The build is launched not by Xcode ->
    // We cannot create a copy task and just show a meaningful error message.
    tasks.create("packForXCode").doLast {
        throw IllegalStateException("Please run the task from Xcode")
    }
} else {
    // Otherwise copy the executable into the Xcode output directory.
    tasks.create("packForXCode", Copy::class.java) {
        dependsOn(kotlinBinary.linkTask)

        destinationDir = file(targetBuildDir)

        konst dsymSource = kotlinBinary.outputFile.absolutePath + ".dSYM"
        konst dsymDestination = File(executablePath).parentFile.name + ".dSYM"
        konst oldExecName = kotlinBinary.outputFile.name
        konst newExecName = File(executablePath).name

        from(dsymSource) {
            into(dsymDestination)
            rename(oldExecName, newExecName)
        }

        from(kotlinBinary.outputFile) {
            rename { executablePath }
        }
    }
}
