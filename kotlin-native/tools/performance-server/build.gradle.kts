import org.jetbrains.kotlin.gradle.dsl.JsModuleKind.MODULE_COMMONJS
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrLink

buildscript {
    konst rootBuildDirectory by extra(file("../.."))

    java.util.Properties().also {
        it.load(java.io.FileReader(project.file("$rootBuildDirectory/../gradle.properties")))
    }.forEach { k, v ->
        konst key = k as String
        konst konstue = project.findProperty(key) ?: v
        extra[key] = konstue
    }

    extra["withoutEmbedabble"] = true
    extra["defaultSnapshotVersion"] = kotlinBuildProperties.defaultSnapshotVersion
    extra["kotlinVersion"] = findProperty("kotlinVersion")
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
    sourceSets {
        konst commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:$kotlinVersion")
            }
            kotlin.srcDir("../benchmarks/shared/src")
        }
        konst jsMain by creating {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:$kotlinVersion")
                implementation(npm("body-parser", "~1.20.0"))
                implementation(npm("debug", "~4.3.4"))
                implementation(npm("ejs", "~3.1.7"))
                implementation(npm("express", "~4.18.1"))
                implementation(npm("kotlin", "~1.6.20"))
                implementation(npm("node-fetch", "~2.6.6"))
            }
            kotlin {
                srcDir("src/main/kotlin")
                srcDir("src/main/kotlin-js")
                srcDir("shared/src/main/kotlin")
            }
        }
    }

    targets {
        js {
            moduleName = "app"
            nodejs()
            compilations.all {
                compilerOptions.configure {
                    moduleKind.set(MODULE_COMMONJS)
                    sourceMap.set(true)
                }
            }
            binaries.executable()
        }
    }
}

tasks.withType<KotlinJsIrLink> {
    destinationDirectory.set(project.file("server"))
}