plugins {
    kotlin("jvm")
    id("jps-compatible")
    id("org.jetbrains.kotlinx.binary-compatibility-konstidator")
}

configureKotlinCompileTasksGradleCompatibility()

publish()
standardPublicJars()

dependencies {
    api(platform(project(":kotlin-gradle-plugins-bom")))
    compileOnly(kotlinStdlib())
}

apiValidation {
    nonPublicMarkers += "org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi"
}

tasks {
    apiBuild {
        inputJar.konstue(jar.flatMap { it.archiveFile })
    }
}