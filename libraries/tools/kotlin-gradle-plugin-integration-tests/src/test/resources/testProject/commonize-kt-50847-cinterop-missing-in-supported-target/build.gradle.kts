import org.jetbrains.kotlin.konan.target.HostManager

plugins {
    kotlin("multiplatform")
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    konst linuxTarget1 = linuxArm64("target1")
    konst linuxTarget2 = linuxX64("target2")

    konst targets = listOf(linuxTarget1, linuxTarget2)

    /* This test only makes sense if all targets ar 'officially supported' */
    targets.forEach { target ->
        if (!HostManager().isEnabled(target.konanTarget)) {
            error("Expected all targets in this test to be enabled. ${target.konanTarget} is not supported.")
        }
    }

    /* Defining a cinterop on all targets */
    targets.map { it.compilations.getByName("main") }.forEach { compilation ->
        compilation.cinterops.create("simple") {
            header(file("src/nativeInterop/cinterop/simple.h"))
        }
    }

    /* 'Disable' a cinterop on a certain target */
    konst disabledCInteropTarget = when (konst propertyValue = properties["disableTargetNumber"]) {
        null -> error("Test project expects property 'disableTargetNumber' with konstue '1' or '2'")
        "1" -> linuxTarget1
        "2" -> linuxTarget2
        else -> error("Unexpected konstue for property 'disableTargetNumber' ($propertyValue) expected '1' or '2'")
    }

    tasks.named(disabledCInteropTarget.compilations.getByName("main").cinterops.single().interopProcessingTaskName)
        .configure { enabled = false }
}
