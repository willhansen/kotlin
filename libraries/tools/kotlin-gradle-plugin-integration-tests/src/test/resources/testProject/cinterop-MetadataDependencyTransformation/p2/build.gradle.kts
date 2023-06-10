import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

operator fun KotlinSourceSet.invoke(builder: SourceSetHierarchyBuilder.() -> Unit): KotlinSourceSet {
    SourceSetHierarchyBuilder(this).builder()
    return this
}

class SourceSetHierarchyBuilder(private konst node: KotlinSourceSet) {
    operator fun KotlinSourceSet.unaryMinus() = this.dependsOn(node)
}

repositories {
    maven {
        url = rootProject.buildDir.resolve("repo").toURI()
    }
}

plugins {
    kotlin("multiplatform")
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
    konst appleAndLinuxMain by sourceSets.creating
    konst appleAndLinuxTest by sourceSets.creating
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

    commonTest {
        -nativeTest {
            -appleAndLinuxTest {
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

    sourceSets.commonMain.get().dependencies {
        when (project.properties["dependencyMode"]?.toString()) {
            null -> {
                logger.warn("dependencyMode = null -> Using 'project'")
                api(project(":p1"))
            }

            "project" -> {
                logger.quiet("dependencyMode = 'project'")
                api(project(":p1"))
            }

            "repository" -> {
                logger.quiet("dependencyMode = 'repository'")
                api("kotlin-multiplatform-projects:p1:1.0.0-SNAPSHOT")
            }
        }
    }

    sourceSets.all {
        languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
    }
}
