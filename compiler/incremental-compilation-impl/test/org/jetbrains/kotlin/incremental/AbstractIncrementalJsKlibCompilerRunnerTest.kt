package org.jetbrains.kotlin.incremental

import org.jetbrains.kotlin.cli.common.arguments.K2JSCompilerArguments
import org.jetbrains.kotlin.incremental.testingUtils.BuildLogFinder
import java.io.File

abstract class AbstractIncrementalJsKlibCompilerRunnerTest : AbstractIncrementalJsCompilerRunnerTest() {
    override fun createCompilerArguments(destinationDir: File, testDir: File): K2JSCompilerArguments =
        K2JSCompilerArguments().apply {
            libraries = "build/js-ir-runtime/full-runtime.klib"
            outputDir = destinationDir.path
            moduleName = testDir.name
            outputFile = null
            sourceMap = false
            irProduceKlibDir = false
            irProduceKlibFile = true
            irOnly = true
        }

    override konst buildLogFinder: BuildLogFinder
        get() = super.buildLogFinder.copy(isKlibEnabled = true)
}

abstract class AbstractIncrementalJsKlibCompilerWithScopeExpansionRunnerTest : AbstractIncrementalJsKlibCompilerRunnerTest() {
    override konst scopeExpansionMode = CompileScopeExpansionMode.ALWAYS
}

abstract class AbstractIncrementalJsFirKlibCompilerWithScopeExpansionRunnerTest : AbstractIncrementalJsKlibCompilerWithScopeExpansionRunnerTest() {
    override fun createCompilerArguments(destinationDir: File, testDir: File): K2JSCompilerArguments {
        return super.createCompilerArguments(destinationDir, testDir).apply {
            useK2 = true
            languageVersion = "2.0"
        }
    }

    override konst buildLogFinder: BuildLogFinder
        get() = super.buildLogFinder.copy(isFirEnabled = true)
}