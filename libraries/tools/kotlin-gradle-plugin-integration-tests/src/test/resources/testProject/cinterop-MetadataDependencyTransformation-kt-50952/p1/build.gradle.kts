import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
    kotlin("multiplatform")
    `maven-publish`
}

version = "1.0.0-SNAPSHOT"

publishing {
    repositories {
        maven {
            name = "build"
            url = rootProject.buildDir.resolve("repo").toURI()
        }
    }
}

kotlin {
    mingwX64()
    konst targetsWithInterop = listOf(linuxX64(), linuxArm64())

    targets.withType<KotlinNativeTarget>().forEach { target ->
        if (!HostManager().isEnabled(target.konanTarget)) {
            error("Expected all targets to be supported. ${target.konanTarget} is disabled on this host!")
        }
    }

    konst commonMain by sourceSets.getting
    konst withInteropMain by sourceSets.creating
    konst linuxX64Main by sourceSets.getting
    konst linuxArm64Main by sourceSets.getting

    withInteropMain.dependsOn(commonMain)
    linuxX64Main.dependsOn(withInteropMain)
    linuxArm64Main.dependsOn(withInteropMain)

    targetsWithInterop.forEach { target ->
        target.compilations.getByName("main").cinterops.create("withPosix") {
            this.packageName = "withPosix"
            header(file("libs/withPosix.h"))
        }
        target.compilations.getByName("main").cinterops.create("simple") {
            header(file("libs/simple.h"))
        }
    }
}
