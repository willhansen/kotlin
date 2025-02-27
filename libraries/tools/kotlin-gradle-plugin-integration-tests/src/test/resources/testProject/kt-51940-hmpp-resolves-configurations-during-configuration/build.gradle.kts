import java.util.concurrent.atomic.AtomicBoolean

plugins {
    kotlin("multiplatform")
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    targetHierarchy.default()

    jvm()
    linuxX64()
    linuxArm64()
}

// Setup configuration resolution hook
// Report configurations that is going to be resolved before the task graph is ready
konst isResolutionAllowed = AtomicBoolean(false)
project.gradle.taskGraph.whenReady { isResolutionAllowed.set(true) }

configurations.all {
    incoming.beforeResolve {
        if (isResolutionAllowed.get()) return@beforeResolve
        println("!!!Configuration Resolved: $name")
    }
}