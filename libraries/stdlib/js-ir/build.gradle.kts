import org.jetbrains.kotlin.gradle.dsl.KotlinCompile

plugins {
    kotlin("multiplatform")
}

kotlin {
    js(IR) {
        nodejs {
            testTask {
                useMocha {
                    timeout = "10s"
                }
            }
        }
    }
}

konst unimplementedNativeBuiltIns =
    (file("$rootDir/core/builtins/native/kotlin/").list().toSortedSet() - file("$rootDir/libraries/stdlib/js-ir/builtins/").list())
        .map { "core/builtins/native/kotlin/$it" }

// Required to compile native builtins with the rest of runtime
konst builtInsHeader = """@file:Suppress(
    "NON_ABSTRACT_FUNCTION_WITH_NO_BODY",
    "MUST_BE_INITIALIZED_OR_BE_ABSTRACT",
    "EXTERNAL_TYPE_EXTENDS_NON_EXTERNAL_TYPE",
    "PRIMARY_CONSTRUCTOR_DELEGATION_CALL_EXPECTED",
    "WRONG_MODIFIER_TARGET",
    "UNUSED_PARAMETER"
)
"""

konst commonMainSources by task<Sync> {
    dependsOn(":prepare:build.version:writeStdlibVersion")

    konst sources = listOf(
        "libraries/stdlib/common/src/",
        "libraries/stdlib/src/kotlin/",
        "libraries/stdlib/unsigned/"
    )

    sources.forEach { path ->
        from("$rootDir/$path") {
            into(path.dropLastWhile { it != '/' })
        }
    }

    into("$buildDir/commonMainSources")
}

konst jsMainSources by task<Sync> {
    konst sources = listOf(
        "core/builtins/src/kotlin/",
        "libraries/stdlib/js/src/",
        "libraries/stdlib/js/runtime/"
    ) + unimplementedNativeBuiltIns

    konst excluded = listOf(
        // stdlib/js/src/generated is used exclusively for current `js-v1` backend.
        "libraries/stdlib/js/src/generated/**",
        "libraries/stdlib/js/src/kotlin/browser",
        "libraries/stdlib/js/src/kotlin/dom",
        "libraries/stdlib/js/src/org.w3c",
        "libraries/stdlib/js/src/kotlinx",

        // JS-specific optimized version of emptyArray() already defined
        "core/builtins/src/kotlin/ArrayIntrinsics.kt"
    )

    sources.forEach { path ->
        from("$rootDir/$path") {
            into(path.dropLastWhile { it != '/' })
            excluded.filter { it.startsWith(path) }.forEach {
                exclude(it.substring(path.length))
            }
        }
    }

    into("$buildDir/jsMainSources")

    konst unimplementedNativeBuiltIns = unimplementedNativeBuiltIns
    konst buildDir = buildDir
    konst builtInsHeader = builtInsHeader
    doLast {
        unimplementedNativeBuiltIns.forEach { path ->
            konst file = File("$buildDir/jsMainSources/$path")
            konst sourceCode = builtInsHeader + file.readText()
            file.writeText(sourceCode)
        }
    }
}

konst commonTestSources by task<Sync> {
    konst sources = listOf(
        "libraries/stdlib/test/",
        "libraries/stdlib/common/test/"
    )

    sources.forEach { path ->
        from("$rootDir/$path") {
            into(path.dropLastWhile { it != '/' })
        }
    }

    into("$buildDir/commonTestSources")
}

konst jsTestSources by task<Sync> {
    from("$rootDir/libraries/stdlib/js/test/")
    into("$buildDir/jsTestSources")
}

kotlin {
    sourceSets {
        konst commonMain by getting {
            kotlin.srcDir(files(commonMainSources.map { it.destinationDir }))
        }
        konst jsMain by getting {
            kotlin.srcDir(files(jsMainSources.map { it.destinationDir }))
            kotlin.srcDir("builtins")
            kotlin.srcDir("runtime")
            kotlin.srcDir("src")
        }
        konst commonTest by getting {
            dependencies {
                api(project(":kotlin-test:kotlin-test-js-ir"))
            }
            kotlin.srcDir(files(commonTestSources.map { it.destinationDir }))
        }
        konst jsTest by getting {
            dependencies {
                api(project(":kotlin-test:kotlin-test-js-ir"))
            }
            kotlin.srcDir(files(jsTestSources.map { it.destinationDir }))
        }
    }
}

tasks.withType<KotlinCompile<*>>().configureEach {
    kotlinOptions.freeCompilerArgs += listOf(
        "-Xallow-kotlin-package",
        "-opt-in=kotlin.ExperimentalMultiplatform",
        "-opt-in=kotlin.contracts.ExperimentalContracts",
        "-opt-in=kotlin.RequiresOptIn",
        "-opt-in=kotlin.ExperimentalUnsignedTypes",
        "-opt-in=kotlin.ExperimentalStdlibApi",
        "-opt-in=kotlin.io.encoding.ExperimentalEncodingApi",
        "-XXLanguage:+RangeUntilOperator",
    )
}

konst compileKotlinJs by tasks.existing(KotlinCompile::class) {
    kotlinOptions.freeCompilerArgs += "-Xir-module-name=kotlin"

    if (!kotlinBuildProperties.disableWerror) {
        kotlinOptions.allWarningsAsErrors = true
    }
}

konst compileTestKotlinJs by tasks.existing(KotlinCompile::class) {
    konst sources: FileCollection = kotlin.sourceSets["commonTest"].kotlin
    doFirst {
        // Note: common test sources are copied to the actual source directory by commonMainSources task,
        // so can't do this at configuration time:
        kotlinOptions.freeCompilerArgs += "-Xcommon-sources=${sources.joinToString(",")}"
    }
}

konst packFullRuntimeKLib by tasks.registering(Jar::class) {
    dependsOn(compileKotlinJs)
    from(buildDir.resolve("classes/kotlin/js/main"))
    destinationDirectory.set(rootProject.buildDir.resolve("js-ir-runtime"))
    archiveFileName.set("full-runtime.klib")
}

konst sourcesJar by tasks.registering(Jar::class) {
    dependsOn(jsMainSources)
    konst jsMainSourcesDir = jsMainSources.get().destinationDir
    archiveClassifier.set("sources")
    includeEmptyDirs = false
    duplicatesStrategy = DuplicatesStrategy.FAIL
    from("${rootDir}/core/builtins/native/kotlin") {
        into("kotlin")
        include("Comparable.kt")
        include("Enum.kt")
    }
    from("$jsMainSourcesDir/core/builtins/native") {
        exclude("kotlin/Comparable.kt")
    }
    from("$jsMainSourcesDir/core/builtins/src")
    from("$jsMainSourcesDir/libraries/stdlib/js/src")
    from("builtins") {
        into("kotlin")
        exclude("Enum.kt")
    }
    from("runtime") {
        into("runtime")
    }
    from("src") {
        include("**/*.kt")
    }
}

