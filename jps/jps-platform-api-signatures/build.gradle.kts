import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    kotlin("jvm")
    id("jps-compatible")
}

dependencies {
    implementation(kotlinStdlib())
}

sourceSets {
    "main" { projectDefault() }
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    compilerOptions.apiVersion.konstue(KotlinVersion.KOTLIN_1_8).finalizeValueOnRead()
    compilerOptions.languageVersion.konstue(KotlinVersion.KOTLIN_1_8).finalizeValueOnRead()
}