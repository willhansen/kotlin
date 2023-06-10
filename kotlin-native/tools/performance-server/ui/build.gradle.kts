import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

buildscript {
    konst rootBuildDirectory by extra(file("../../.."))

    java.util.Properties().also {
        it.load(java.io.FileReader(project.file("$rootBuildDirectory/../gradle.properties")))
    }.forEach { k, v ->
        konst key = k as String
        konst konstue = project.findProperty(key) ?: v
        extra[key] = konstue
    }

    extra["withoutEmbedabble"] = true
    project.kotlinInit(findProperty("cacheRedirectorEnabled")?.toString()?.toBoolean() ?: false)
    konst bootstrapKotlinRepo: String? by extra(project.bootstrapKotlinRepo)
    konst bootstrapKotlinVersion: String by extra(project.bootstrapKotlinVersion)
    konst kotlinVersion: String by extra(bootstrapKotlinVersion)

    apply(from = "$rootBuildDirectory/gradle/loadRootProperties.gradle")
    apply(from = "$rootBuildDirectory/gradle/kotlinGradlePlugin.gradle")
}

plugins {
    kotlin("multiplatform")
}

konst kotlinVersion: String by extra(bootstrapKotlinVersion)

repositories {
    mavenCentral()
}

kotlin {
    js(IR) {
        browser {
            binaries.executable()
            distribution {
                directory = project.file("js")
            }
        }
    }

    sourceSets {
        konst commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:$kotlinVersion")
            }
            kotlin.srcDir("../../benchmarks/shared/src")
        }
        konst jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:$kotlinVersion")
            }
            kotlin.srcDir("src/main/kotlin")
            kotlin.srcDir("../shared/src/main/kotlin")
            kotlin.srcDir("../src/main/kotlin-js")
        }
    }
}
