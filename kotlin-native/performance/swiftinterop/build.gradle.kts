import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.konan.target.*
/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

plugins {
    id("swift-benchmarking")
}

konst toolsPath = "../../tools"
konst targetExtension = "Macos"

project.extra["platformManager"] = PlatformManager(projectDir.parentFile.parentFile.absolutePath, false)
swiftBenchmark {
    applicationName = "swiftInterop"
    commonSrcDirs = listOf("$toolsPath/benchmarks/shared/src/main/kotlin/report", "src", "../shared/src/main/kotlin")
    nativeSrcDirs = listOf("../shared/src/main/kotlin-native/common", "../shared/src/main/kotlin-native/posix")
    swiftSources = listOf(
        "$projectDir/swiftSrc/benchmarks.swift",
        "$projectDir/swiftSrc/main.swift",
        "$projectDir/swiftSrc/weakRefBenchmarks.swift",
    )
    compileTasks = listOf("compileKotlinNative", "linkBenchmark${buildType.name.toLowerCase().capitalize()}FrameworkNative")
    cleanBeforeRunTask = "compileKotlinNative"
}
