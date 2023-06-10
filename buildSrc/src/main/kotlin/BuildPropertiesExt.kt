/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

konst KotlinBuildProperties.includeJava9: Boolean
    get() = !isInJpsBuildIdeaSync && getBoolean("kotlin.build.java9", true)

konst KotlinBuildProperties.useBootstrapStdlib: Boolean
    get() = isInJpsBuildIdeaSync || getBoolean("kotlin.build.useBootstrapStdlib", false)

konst KotlinBuildProperties.postProcessing: Boolean get() = isTeamcityBuild || getBoolean("kotlin.build.postprocessing", true)

konst KotlinBuildProperties.relocation: Boolean get() = postProcessing

konst KotlinBuildProperties.proguard: Boolean get() = postProcessing && getBoolean("kotlin.build.proguard", isTeamcityBuild)

konst KotlinBuildProperties.jarCompression: Boolean get() = getBoolean("kotlin.build.jar.compression", isTeamcityBuild)

konst KotlinBuildProperties.ignoreTestFailures: Boolean get() = getBoolean("ignoreTestFailures", isTeamcityBuild)

konst KotlinBuildProperties.disableWerror: Boolean
    get() = getBoolean("kotlin.build.disable.werror") || useFir || isInJpsBuildIdeaSync || getBoolean("test.progressive.mode")

konst KotlinBuildProperties.generateModularizedConfigurations: Boolean
    get() = getBoolean("kotlin.fir.modularized.mt.configurations", false)

konst KotlinBuildProperties.generateFullPipelineConfigurations: Boolean
    get() = getBoolean("kotlin.fir.modularized.fp.configurations", false)

konst KotlinBuildProperties.pathToKotlinModularizedTestData: String?
    get() = getOrNull("kotlin.fir.modularized.testdata.kotlin") as? String

konst KotlinBuildProperties.pathToIntellijModularizedTestData: String?
    get() = getOrNull("kotlin.fir.modularized.testdata.intellij") as? String

konst KotlinBuildProperties.pathToYoutrackModularizedTestData: String?
    get() = getOrNull("kotlin.fir.modularized.testdata.youtrack") as? String

konst KotlinBuildProperties.pathToSpaceModularizedTestData: String?
    get() = getOrNull("kotlin.fir.modularized.testdata.space") as? String

konst KotlinBuildProperties.isObsoleteJdkOverrideEnabled: Boolean
    get() = getBoolean("kotlin.build.isObsoleteJdkOverrideEnabled", false)

konst KotlinBuildProperties.isNativeRuntimeDebugInfoEnabled: Boolean
    get() = getBoolean("kotlin.native.isNativeRuntimeDebugInfoEnabled", false)

konst KotlinBuildProperties.junit5NumberOfThreadsForParallelExecution: Int?
    get() = (getOrNull("kotlin.test.junit5.maxParallelForks") as? String)?.toInt()

// Enabling publishing docs jars only on CI build by default
// Currently dokka task runs non-incrementally and takes big amount of time
konst KotlinBuildProperties.publishGradlePluginsJavadoc: Boolean
    get() = getBoolean("kotlin.build.gradle.publish.javadocs", isTeamcityBuild)

konst KotlinBuildProperties.useFirWithLightTree: Boolean
    get() = getBoolean("kotlin.build.useFirLT")

konst KotlinBuildProperties.useFirTightIC: Boolean
    get() = getBoolean("kotlin.build.useFirIC")
