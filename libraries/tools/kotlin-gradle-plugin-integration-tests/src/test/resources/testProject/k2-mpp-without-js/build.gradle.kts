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

    sourceSets {
        konst commonMain by getting {}

        konst intermediateMain by creating {
            dependsOn(commonMain)
        }

        konst jvmMain by getting {
            dependsOn(intermediateMain)
        }

        konst linuxX64Main by getting {
            dependsOn(intermediateMain)
        }
    }
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xrender-internal-diagnostic-names")
        with(project.providers) {
            languageVersion.set(KotlinVersion.fromVersion("2.0"))
        }
    }
}
