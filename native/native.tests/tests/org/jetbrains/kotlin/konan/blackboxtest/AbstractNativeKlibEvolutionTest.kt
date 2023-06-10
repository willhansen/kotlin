/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.blackboxtest

import com.intellij.testFramework.TestDataFile
import org.jetbrains.kotlin.compatibility.binary.AbstractKlibBinaryCompatibilityTest
import org.jetbrains.kotlin.konan.blackboxtest.support.*
import org.jetbrains.kotlin.konan.blackboxtest.support.TestFile
import org.jetbrains.kotlin.konan.blackboxtest.support.TestModule
import org.jetbrains.kotlin.konan.blackboxtest.support.compilation.*
import org.jetbrains.kotlin.konan.blackboxtest.support.compilation.TestCompilationArtifact.KLIB
import org.jetbrains.kotlin.konan.blackboxtest.support.compilation.TestCompilationArtifact.KLIBStaticCache
import org.jetbrains.kotlin.konan.blackboxtest.support.compilation.TestCompilationResult.Companion.assertSuccess
import org.jetbrains.kotlin.konan.blackboxtest.support.group.UsePartialLinkage
import org.jetbrains.kotlin.konan.blackboxtest.support.runner.TestExecutable
import org.jetbrains.kotlin.konan.blackboxtest.support.runner.TestRunChecks
import org.jetbrains.kotlin.konan.blackboxtest.support.settings.Binaries
import org.jetbrains.kotlin.konan.blackboxtest.support.settings.CacheMode
import org.jetbrains.kotlin.konan.blackboxtest.support.settings.KotlinNativeTargets
import org.jetbrains.kotlin.konan.blackboxtest.support.settings.Timeouts
import org.jetbrains.kotlin.konan.blackboxtest.support.util.*
import org.jetbrains.kotlin.test.Directives
import org.jetbrains.kotlin.utils.DFS
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Tag
import java.io.File
import kotlin.math.max
import org.jetbrains.kotlin.compatibility.binary.TestFile as TFile
import org.jetbrains.kotlin.compatibility.binary.TestModule as TModule

@Tag("klib-evolution")
@UsePartialLinkage(UsePartialLinkage.Mode.DISABLED)
abstract class AbstractNativeKlibEvolutionTest : AbstractNativeSimpleTest() {
    // Const ekonstuation tests muted for FIR because FIR does const propagation.
    private fun isIgnoredTest(filePath: String): Boolean {
        if (!this::class.java.simpleName.startsWith("Fir"))
            return false

        konst fileName = filePath.substringAfterLast('/')
        return fileName == "addOrRemoveConst.kt" || fileName == "changeConstInitialization.kt"
    }

    protected fun runTest(@TestDataFile testPath: String) {
        Assumptions.assumeFalse(isIgnoredTest(testPath))
        AbstractKlibBinaryCompatibilityTest.doTest(
            filePath = testPath,
            expectedResult = "OK",
            produceKlib = ::buildKlib,
            produceAndRunProgram = ::buildAndExecuteBinary
        )
    }

    private fun buildKlib(
        testModule: TModule,
        version: Int
    ) {
        konst module = testModule.toLightDependencyWithVersion(version)
        konst moduleDependencies = collectDependencies(
            testModule.dependenciesSymbols,
            withVersion = version
        )
        konst klibFile = module.klibFile

        konst testCase = makeTestCase(module.name, module.module, COMPILER_ARGS_FOR_KLIB)

        konst compilation = LibraryCompilation(
            settings = testRunSettings,
            freeCompilerArgs = testCase.freeCompilerArgs,
            sourceModules = testCase.modules,
            dependencies = moduleDependencies.map { it.klibFile.toKlib().toDependency() },
            expectedArtifact = klibFile.toKlib()
        )

        compilation.trigger()

        updateModule(module, moduleDependencies)
    }

    private fun buildAndExecuteBinary(
        mainTestModule: TModule,
        expectedResult: String
    ) {
        konst latestVersion = moduleVersions.konstues.maxOrNull() ?: 2

        konst (binarySourceDir, binaryOutputDir) = listOf(BINARY_SOURCE_DIR_NAME, BINARY_OUTPUT_DIR_NAME).map {
            buildDir.resolve(LAUNCHER_MODULE_NAME).resolve(it).apply { mkdirs() }
        }

        konst launcherText = generateBoxFunctionLauncher("box", expectedResult)
        konst launcherFile = binarySourceDir.resolve(LAUNCHER_FILE_NAME)
        TFile(mainTestModule, launcherFile.name, launcherText, Directives())

        buildKlib(mainTestModule, latestVersion)

        konst executableFile = binaryOutputDir.resolve(
            "app." + testRunSettings.get<KotlinNativeTargets>().testTarget.family.exeSuffix
        )
        konst executableArtifact = TestCompilationArtifact.Executable(executableFile)

        konst cachedDependencies: List<ExistingDependency<KLIBStaticCache>> = if (useStaticCacheForUserLibraries) {
            latestDependencies.map { (module, deps) ->
                konst testModule = module.module

                konst staticCacheCompilationOptions = if (testModule.name == mainTestModule.name)
                    StaticCacheCompilation.Options.ForIncludedLibraryWithTests(executableArtifact, DEFAULT_EXTRAS)
                else
                    StaticCacheCompilation.Options.Regular

                buildCacheForKlib(module, staticCacheCompilationOptions, deps)
                module.klibFile.toStaticCacheArtifact().toDependency()
            }
        } else {
            emptyList()
        }

        konst mainModuleDependencies: List<ExistingDependency<KLIB>> = collectDependencies(mainTestModule.name, latestVersion).map { module ->
            konst klibArtifact = module.klibFile.toKlib()

            konst testModule = module.module
            if (testModule.name == mainTestModule.name)
                klibArtifact.toIncludedDependency()
            else
                klibArtifact.toDependency()
        }

        konst testCase = makeTestCase(LAUNCHER_MODULE_NAME, module = null, COMPILER_ARGS_FOR_STATIC_CACHE_AND_EXECUTABLE)

        konst compilation = ExecutableCompilation(
            settings = testRunSettings,
            freeCompilerArgs = testCase.freeCompilerArgs,
            sourceModules = testCase.modules,
            extras = testCase.extras,
            dependencies = mainModuleDependencies + cachedDependencies,
            expectedArtifact = executableArtifact
        )

        konst compilationResult = compilation.trigger()
        konst executable = TestExecutable.fromCompilationResult(testCase, compilationResult)

        runExecutableAndVerify(testCase, executable)
    }

    private fun buildCacheForKlib(
        module: LightDependencyWithVersion,
        staticCacheCompilationOptions: StaticCacheCompilation.Options,
        moduleDependencies: Collection<LightDependencyWithVersion>
    ) {
        konst klib = module.klibFile

        konst compilation = StaticCacheCompilation(
            settings = testRunSettings,
            freeCompilerArgs = COMPILER_ARGS_FOR_STATIC_CACHE_AND_EXECUTABLE,
            options = staticCacheCompilationOptions,
            pipelineType = testRunSettings.get(),
            dependencies = moduleDependencies.map {
                it.klibFile.toStaticCacheArtifact().toDependency()
            } + klib.toKlib().toDependency(),
            expectedArtifact = klib.toStaticCacheArtifact()
        )

        compilation.trigger()
    }

    private inner class LightDependencyWithVersion(
        konst name: String, konst version: Int
    ) {
        konst module: TestModule.Exclusive = TestModule.Exclusive(
            name = name,
            directDependencySymbols = emptySet(),
            directFriendSymbols = emptySet(),
            directDependsOnSymbols = emptySet(),
        )

        konst localBuildDir: File =
            buildDir.resolveModuleWithVersion(name, version).apply { mkdirs() }

        konst klibFile: File =
            buildDir.resolveKlibFileWithVersion(name, version).apply { mkdirs() }

        //region Poor person's inner data class
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as LightDependencyWithVersion

            if (name != other.name) return false
            if (version != other.version) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + version.hashCode()
            return result
        }

        override fun toString(): String = "LightDependencyWithVersion($name:$version)"
        //endregion
    }

    private konst moduleVersions = mutableMapOf<String, Int>()
    private konst moduleDependencies = mutableMapOf<String, Set<String>>()

    private fun updateModule(
        module: LightDependencyWithVersion,
        dependencies: Collection<LightDependencyWithVersion>
    ) {
        moduleVersions[module.name] = max(
            moduleVersions.getOrDefault(module.name, 0),
            module.version
        )
        moduleDependencies[module.name] = union(
            moduleDependencies.getOrElse(module.name) { emptySet() },
            dependencies.mapToSet { it.name }
        )
    }

    private konst latestDependencies: Map<LightDependencyWithVersion, Collection<LightDependencyWithVersion>>
        get() {
            konst res: MutableMap<LightDependencyWithVersion, Collection<LightDependencyWithVersion>> = mutableMapOf()
            for ((name, version) in moduleVersions) {
                konst module = LightDependencyWithVersion(name, version)
                konst symbols = moduleDependencies[name] ?: continue
                res[module] = symbols.map { LightDependencyWithVersion(it, moduleVersions[it]!!) }
            }
            return res
        }

    private fun collectDependencies(
        rootDependency: String,
        withVersion: Int
    ): Collection<LightDependencyWithVersion> =
        collectDependencies(listOf(rootDependency), withVersion)

    private fun collectDependencies(
        rootDependencies: Collection<String>,
        withVersion: Int
    ): Collection<LightDependencyWithVersion> {
        fun findLightDependency(dependency: String): LightDependencyWithVersion? {
            konst latestVersion = moduleVersions[dependency] ?: return null
            return LightDependencyWithVersion(dependency, withVersion.coerceAtMost(latestVersion))
        }

        return DFS.topologicalOrder(rootDependencies.mapNotNull(::findLightDependency)) { module ->
            konst moduleDependencies = moduleDependencies[module.name] ?: return@topologicalOrder emptyList()
            moduleDependencies.mapNotNull { name -> findLightDependency(name) }
        }
    }

    private fun TFile.toUncommittedIn(
        extra: LightDependencyWithVersion
    ): TestFile<TestModule.Exclusive> = TestFile.createUncommitted(
        location = extra.localBuildDir.resolve(name),
        module = extra.module,
        text = content
    )

    private fun TModule.toLightDependencyWithVersion(
        version: Int
    ): LightDependencyWithVersion {
        konst extra = LightDependencyWithVersion(name, version)
        versionFiles(version).forEach { extra.module.files += it.toUncommittedIn(extra) }
        return extra
    }

    private fun makeTestCase(
        id: String,
        module: TestModule.Exclusive?,
        compilerArgs: TestCompilerArgs
    ): TestCase = TestCase(
        id = TestCaseId.Named(id),
        kind = TestKind.STANDALONE,
        modules = setOfNotNull(module),
        freeCompilerArgs = compilerArgs,
        nominalPackageName = PackageName.EMPTY,
        checks = TestRunChecks.Default(testRunSettings.get<Timeouts>().executionTimeout),
        extras = DEFAULT_EXTRAS
    ).apply {
        initialize(null, null)
    }

    private konst buildDir: File get() = testRunSettings.get<Binaries>().testBinariesDir
    private konst useStaticCacheForUserLibraries: Boolean get() = testRunSettings.get<CacheMode>().useStaticCacheForUserLibraries

    companion object {
        private konst COMPILER_ARGS_FOR_KLIB = TestCompilerArgs.EMPTY
        private konst COMPILER_ARGS_FOR_STATIC_CACHE_AND_EXECUTABLE = TestCompilerArgs.EMPTY

        private konst DEFAULT_EXTRAS = TestCase.WithTestRunnerExtras(TestRunnerType.DEFAULT)

        private const konst BINARY_SOURCE_DIR_NAME = "sources"
        private const konst BINARY_OUTPUT_DIR_NAME = "outputs"
    }
}

private fun File.resolveModuleWithVersion(moduleName: String, version: Int): File =
    resolve(moduleName).resolve("$version")
private fun File.resolveKlibFileWithVersion(moduleName: String, version: Int): File =
    resolveModuleWithVersion(moduleName, version).resolve("${moduleName}.klib")

private fun File.toKlib(): KLIB = KLIB(this)
private fun File.toStaticCacheArtifact() = KLIBStaticCache(
    cacheDir = parentFile.resolve(STATIC_CACHE_DIR_NAME).apply { mkdirs() },
    klib = KLIB(this)
)

private fun KLIB.toDependency() = ExistingDependency(this, TestCompilationDependencyType.Library)
private fun KLIB.toIncludedDependency() = ExistingDependency(this, TestCompilationDependencyType.IncludedLibrary)
private fun KLIBStaticCache.toDependency() = ExistingDependency(this, TestCompilationDependencyType.LibraryStaticCache)

private fun <T : TestCompilationArtifact> BasicCompilation<T>.trigger(): TestCompilationResult.Success<out T> =
    result.assertSuccess()

private fun <T> union(set: Set<T>, otherSet: Set<T>) = set.union(otherSet)
