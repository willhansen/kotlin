import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

operator fun KotlinSourceSet.invoke(builder: SourceSetHierarchyBuilder.() -> Unit): KotlinSourceSet {
    SourceSetHierarchyBuilder(this).builder()
    return this
}

class SourceSetHierarchyBuilder(private konst node: KotlinSourceSet) {
    operator fun KotlinSourceSet.unaryMinus() = this.dependsOn(node)
}

plugins {
    kotlin("multiplatform")
    `maven-publish`
}

version = "1.0.0-SNAPSHOT"

publishing {
    repositories {
        this.maven {
            this.name = "build"
            this.url = rootProject.buildDir.resolve("repo").toURI()
        }
    }
}

kotlin {
    js().nodejs()
    jvm()

    linuxX64()
    linuxArm64()

    macosX64("macos")
    ios()

    mingwX64("windowsX64")
    @Suppress("DEPRECATION_ERROR")
    mingwX86("windowsX86")

    konst commonMain by sourceSets.getting
    konst concurrentMain by sourceSets.creating
    konst jvmMain by sourceSets.getting
    konst jsMain by sourceSets.getting
    konst nativeMain by sourceSets.creating
    konst appleAndLinuxMain by sourceSets.creating
    konst linuxMain by sourceSets.creating
    konst linuxX64Main by sourceSets.getting
    konst linuxArm64Main by sourceSets.getting
    konst appleMain by sourceSets.creating
    konst macosMain by sourceSets.getting
    konst iosMain by sourceSets.getting
    konst windowsMain by sourceSets.creating
    konst windowsX64Main by sourceSets.getting
    konst windowsX86Main by sourceSets.getting

    commonMain {
        -jsMain
        -concurrentMain {
            -jvmMain
            -nativeMain {
                -appleAndLinuxMain {
                    -appleMain {
                        -iosMain
                        -macosMain
                    }
                    -linuxMain {
                        -linuxArm64Main
                        -linuxX64Main
                    }
                }
                -windowsMain {
                    -windowsX64Main
                    -windowsX86Main
                }
            }
        }
    }

    targets.withType<KotlinNativeTarget>().forEach { target ->
        target.compilations.getByName("main").cinterops.create("withPosix") {
            this.packageName = "withPosix"
            header(file("libs/withPosix.h"))
        }
        target.compilations.getByName("main").cinterops.create("simple") {
            header(file("libs/simple.h"))
        }
    }

    sourceSets.all {
        languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
    }
}
