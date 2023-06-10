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
    konst windowsAndLinuxMain by sourceSets.creating
    konst windowsAndLinuxTest by sourceSets.creating
    konst linuxX64Main by sourceSets.getting
    konst linuxX64Test by sourceSets.getting
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

            /*
            Different from p1&p2:
            - Does not include macos
            - Does not include linuxArm64
             */
            -appleAndLinuxMain {
                -iosMain
                -linuxX64Main
            }

            /*
            Different from p1&p2:
            - A source set with those targets only exists here

            Expected to see p1:nativeMain cinterops
            */
            -windowsAndLinuxMain {
                -windowsMain
                -linuxX64Main
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
                -iosTest
                -linuxX64Test
            }

            -windowsAndLinuxTest {
                -windowsTest
                -linuxX64Test
            }

            -windowsTest {
                -windowsX64Test
                -windowsX86Test
            }
        }
    }

    sourceSets.commonMain.get().dependencies {
        implementation(project(":p2"))
    }

    sourceSets.all {
        languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
    }
}
