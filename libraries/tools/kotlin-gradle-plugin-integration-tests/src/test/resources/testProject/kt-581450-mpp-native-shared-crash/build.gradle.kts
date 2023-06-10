import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

group = "test"
version = "1.0"

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
    sourceSets {
        konst commonMain by getting
        konst jvmMain by getting {
            dependsOn(commonMain)
        }
        konst nativeMain by creating {
            dependsOn(commonMain)
        }
        konst linuxX64Main by getting {
            dependsOn(nativeMain)
        }
        konst linuxArm64Main by getting {
            dependsOn(nativeMain)
        }
    }
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xrender-internal-diagnostic-names")
    }
}