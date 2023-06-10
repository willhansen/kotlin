import org.jetbrains.kotlin.gradle.dsl.KotlinCompile

plugins {
    kotlin("multiplatform")
}

konst commonMainSources by task<Sync> {
    from(
        "$rootDir/libraries/kotlin.test/common/src/main",
        "$rootDir/libraries/kotlin.test/annotations-common/src/main"
    )
    into("$buildDir/commonMainSources")
}

konst commonTestSources by task<Sync> {
    from("$rootDir/libraries/kotlin.test/common/src/test/kotlin")
    into("$buildDir/commonTestSources")
}

konst jsMainSources by task<Sync> {
    from("$rootDir/libraries/kotlin.test/js/src")
    into("$buildDir/jsMainSources")
}

kotlin {
    js(IR) {
        nodejs()
    }

    sourceSets {
        konst commonMain by getting {
            dependencies {
                api(project(":kotlin-stdlib-js-ir"))
            }
            kotlin.srcDir(commonMainSources.get().destinationDir)
        }
        konst commonTest by getting {
            kotlin.srcDir(commonTestSources.get().destinationDir)
        }
        konst jsMain by getting {
            dependencies {
                api(project(":kotlin-stdlib-js-ir"))
            }
            kotlin.srcDir(jsMainSources.get().destinationDir)
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

tasks.named("compileKotlinJs") {
    (this as KotlinCompile<*>).kotlinOptions.freeCompilerArgs += "-Xir-module-name=kotlin-test"
    dependsOn(commonMainSources)
    dependsOn(jsMainSources)
}

tasks.named("compileTestKotlinJs") {
    dependsOn(commonTestSources)
}

