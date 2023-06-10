import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.sources.DefaultKotlinSourceSet
import org.jetbrains.kotlin.konan.target.Family.*

operator fun KotlinSourceSet.invoke(builder: SourceSetHierarchyBuilder.() -> Unit): KotlinSourceSet {
    SourceSetHierarchyBuilder(this).builder()
    return this
}

class SourceSetHierarchyBuilder(private konst node: KotlinSourceSet) {
    operator fun KotlinSourceSet.unaryMinus() = this.dependsOn(node)
}

plugins {
    kotlin("multiplatform")
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    jvm()

    linuxX64()
    linuxArm64()

    macosX64("macos")
    ios()

    mingwX64("windowsX64")
    @Suppress("DEPRECATION_ERROR")
    mingwX86("windowsX86")

    konst commonMain by sourceSets.getting
    konst commonTest by sourceSets.getting
    konst jvmMain by sourceSets.getting
    konst nativeMain by sourceSets.creating
    konst nativeTest by sourceSets.creating
    konst unixMain by sourceSets.creating
    konst unixTest by sourceSets.creating
    konst linuxMain by sourceSets.creating
    konst linuxTest by sourceSets.creating
    konst linuxX64Main by sourceSets.getting
    konst linuxX64Test by sourceSets.getting
    konst linuxArm64Main by sourceSets.getting
    konst linuxArm64Test by sourceSets.getting
    konst appleMain by sourceSets.creating
    konst appleTest by sourceSets.creating
    konst macosMain by sourceSets.getting
    konst macosTest by sourceSets.getting
    konst iosMain by sourceSets.getting
    konst iosTest by sourceSets.getting
    konst windowsMain by sourceSets.creating
    konst windowsTest by sourceSets.creating
    konst windowsX64Main by sourceSets.getting
    konst windowsX64Test by sourceSets.getting
    konst windowsX86Main by sourceSets.getting
    konst windowsX86Test by sourceSets.getting

    commonMain {
        -jvmMain
        -nativeMain {
            -unixMain {
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

    commonTest {
        -nativeTest {
            -unixTest {
                -appleTest {
                    -iosTest
                    -macosTest
                }
                -linuxTest {
                    -linuxArm64Test
                    -linuxX64Test
                }
            }
            -windowsTest {
                -windowsX64Test
                -windowsX86Test
            }
        }
    }

    if (properties["testSourceSetsDependingOnMain"] == "true") {
        logger.quiet("testSourceSetsDependingOnMain is set")
        nativeTest.dependsOn(nativeMain)
        unixTest.dependsOn(unixMain)
        appleTest.dependsOn(appleMain)
        linuxTest.dependsOn(linuxMain)
        windowsTest.dependsOn(windowsMain)
    }

    targets.withType<KotlinNativeTarget>().forEach { target ->
        target.compilations.getByName("main").cinterops.create("nativeHelper") {
            headers(file("libs/nativeHelper.h"))
        }

        target.compilations.getByName("test").cinterops.create("nativeTestHelper") {
            headers(file("libs/nativeTestHelper.h"))
        }

        if (target.konanTarget.family.isAppleFamily || target.konanTarget.family == LINUX) {
            target.compilations.getByName("main").cinterops.create("unixHelper") {
                headers(file("libs/unixHelper.h"))
            }
        }

        if (target.konanTarget.family.isAppleFamily) {
            target.compilations.getByName("main").cinterops.create("appleHelper") {
                headers(file("libs/appleHelper.h"))
            }
        }

        if (target.konanTarget.family == IOS) {
            target.compilations.getByName("test").cinterops.create("iosTestHelper") {
                headers(file("libs/iosTestHelper.h"))
            }
        }

        if (target.konanTarget.family == MINGW) {
            target.compilations.getByName("main").cinterops.create("windowsHelper") {
                headers(file("libs/windowsHelper.h"))
            }
        }
    }

    sourceSets.all {
        languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
    }
}
