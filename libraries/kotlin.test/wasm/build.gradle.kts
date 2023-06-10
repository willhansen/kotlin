import org.jetbrains.kotlin.gradle.dsl.KotlinCompile

plugins {
    kotlin("multiplatform")
}

konst commonMainSources by task<Sync> {
    from(
        "$rootDir/libraries/kotlin.test/common/src",
        "$rootDir/libraries/kotlin.test/annotations-common/src"
    )
    into("$buildDir/commonMainSources")
}

kotlin {
    wasm {
        nodejs()
    }

    @Suppress("UNUSED_VARIABLE")
    sourceSets {
         konst commonMain by getting {
            dependencies {
                api(project(":kotlin-stdlib-wasm"))
            }
            kotlin.srcDir(commonMainSources.get().destinationDir)
        }

        konst wasmMain by getting {
            dependencies {
                api(project(":kotlin-stdlib-wasm"))
            }
            kotlin.srcDir("$rootDir/libraries/kotlin.test/wasm/src")
        }
    }
}

tasks.withType<KotlinCompile<*>>().configureEach {
    kotlinOptions.freeCompilerArgs += listOf(
        "-Xallow-kotlin-package",
        "-opt-in=kotlin.ExperimentalMultiplatform",
        "-opt-in=kotlin.contracts.ExperimentalContracts"
    )
}

tasks.named("compileKotlinWasm") {
    (this as KotlinCompile<*>).kotlinOptions.freeCompilerArgs += "-Xir-module-name=kotlin-test"
    dependsOn(commonMainSources)
}

tasks.register<Jar>("sourcesJar") {
    dependsOn(commonMainSources)
    archiveClassifier.set("sources")
    from(kotlin.sourceSets["commonMain"].kotlin)
    from(kotlin.sourceSets["wasmMain"].kotlin)
}
