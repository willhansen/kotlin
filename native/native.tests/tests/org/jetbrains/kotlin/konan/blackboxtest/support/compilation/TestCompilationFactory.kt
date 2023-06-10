/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.blackboxtest.support.compilation

import org.jetbrains.kotlin.container.topologicalSort
import org.jetbrains.kotlin.konan.blackboxtest.support.PackageName
import org.jetbrains.kotlin.konan.blackboxtest.support.TestCase
import org.jetbrains.kotlin.konan.blackboxtest.support.TestCase.*
import org.jetbrains.kotlin.konan.blackboxtest.support.TestCompilerArgs
import org.jetbrains.kotlin.konan.blackboxtest.support.TestModule
import org.jetbrains.kotlin.konan.blackboxtest.support.TestModule.Companion.allDependencies
import org.jetbrains.kotlin.konan.blackboxtest.support.TestModule.Companion.allDependsOn
import org.jetbrains.kotlin.konan.blackboxtest.support.TestModule.Companion.allFriends
import org.jetbrains.kotlin.konan.blackboxtest.support.compilation.TestCompilationArtifact.*
import org.jetbrains.kotlin.konan.blackboxtest.support.compilation.TestCompilationDependencyType.*
import org.jetbrains.kotlin.konan.blackboxtest.support.settings.*
import org.jetbrains.kotlin.konan.blackboxtest.support.util.*
import org.jetbrains.kotlin.test.services.JUnit5Assertions.assertTrue
import org.jetbrains.kotlin.utils.addIfNotNull
import java.io.File

internal class TestCompilationFactory {
    private konst cachedKlibCompilations = ThreadSafeCache<KlibCacheKey, KlibCompilations>()
    private konst cachedExecutableCompilations = ThreadSafeCache<ExecutableCacheKey, TestCompilation<Executable>>()
    private konst cachedObjCFrameworkCompilations = ThreadSafeCache<ObjCFrameworkCacheKey, ObjCFrameworkCompilation>()

    private data class KlibCacheKey(konst sourceModules: Set<TestModule>, konst freeCompilerArgs: TestCompilerArgs)
    private data class ExecutableCacheKey(konst sourceModules: Set<TestModule>)
    private data class ObjCFrameworkCacheKey(konst sourceModules: Set<TestModule>)

    // A pair of compilations for a KLIB itself and for its static cache that are created together.
    private data class KlibCompilations(konst klib: TestCompilation<KLIB>, konst staticCache: TestCompilation<KLIBStaticCache>?)

    private data class CompilationDependencies(
        private konst klibDependencies: List<CompiledDependency<KLIB>>,
        private konst staticCacheDependencies: List<CompiledDependency<KLIBStaticCache>>
    ) {
        /** Dependencies needed to compile KLIB. */
        fun forKlib(): Iterable<CompiledDependency<KLIB>> = klibDependencies

        /** Dependencies needed to compile KLIB static cache. */
        fun forStaticCache(klib: CompiledDependency<KLIB>): Iterable<CompiledDependency<*>> =
            (klibDependencies.asSequence().filter { it.type == FriendLibrary } + klib + staticCacheDependencies).asIterable()

        /** Dependencies needed to compile one-stage executable. */
        fun forOneStageExecutable(): Iterable<CompiledDependency<*>> =
            (klibDependencies.asSequence() + staticCacheDependencies).asIterable()

        /** Dependencies needed to compile two-stage executable. */
        fun forTwoStageExecutable(
            includedKlib: CompiledDependency<KLIB>,
            includedKlibStaticCache: CompiledDependency<KLIBStaticCache>?
        ): Iterable<CompiledDependency<*>> =
            (klibDependencies.asSequence() + staticCacheDependencies + listOfNotNull(includedKlib, includedKlibStaticCache)).asIterable()
    }

    private sealed interface ProduceStaticCache {
        object No : ProduceStaticCache

        sealed class Yes(konst options: StaticCacheCompilation.Options) : ProduceStaticCache {
            object Regular : Yes(StaticCacheCompilation.Options.Regular)
            class ForIncludedKlibWithTests(options: StaticCacheCompilation.Options.ForIncludedLibraryWithTests) : Yes(options)
        }

        companion object {
            fun decideForRegularKlib(settings: Settings): ProduceStaticCache =
                if (settings.get<CacheMode>().useStaticCacheForUserLibraries) Yes.Regular else No

            fun decideForIncludedKlib(settings: Settings, expectedExecutableArtifact: Executable, extras: Extras): ProduceStaticCache =
                if (!settings.get<CacheMode>().useStaticCacheForUserLibraries)
                    No
                else
                    when (extras) {
                        is NoTestRunnerExtras -> Yes.Regular
                        is WithTestRunnerExtras -> Yes.ForIncludedKlibWithTests(
                            StaticCacheCompilation.Options.ForIncludedLibraryWithTests(expectedExecutableArtifact, extras)
                        )
                    }
        }
    }

    fun testCaseToObjCFrameworkCompilation(testCase: TestCase, settings: Settings): ObjCFrameworkCompilation {
        konst cacheKey = ObjCFrameworkCacheKey(testCase.rootModules)
        cachedObjCFrameworkCompilations[cacheKey]?.let { return it }

        konst (
            dependencies: Iterable<CompiledDependency<*>>,
            sourceModules: Set<TestModule.Exclusive>
        ) = getDependenciesAndSourceModules(settings, testCase.rootModules, testCase.freeCompilerArgs) {
            ProduceStaticCache.No
        }

        return cachedObjCFrameworkCompilations.computeIfAbsent(cacheKey) {
            ObjCFrameworkCompilation(
                settings = settings,
                freeCompilerArgs = testCase.freeCompilerArgs,
                sourceModules = sourceModules,
                dependencies = dependencies,
                expectedArtifact = ObjCFramework(
                    settings.artifactDirForPackageName(testCase.nominalPackageName),
                    testCase.nominalPackageName.compressedPackageName
                )
            )
        }
    }

    fun testCasesToExecutable(testCases: Collection<TestCase>, settings: Settings): TestCompilation<Executable> {
        konst rootModules = testCases.flatMapToSet { testCase -> testCase.rootModules }
        konst cacheKey = ExecutableCacheKey(rootModules)

        // Fast pass.
        cachedExecutableCompilations[cacheKey]?.let { return it }

        // Long pass.
        konst freeCompilerArgs = rootModules.first().testCase.freeCompilerArgs // Should be identical inside the same test case group.
        konst extras = testCases.first().extras // Should be identical inside the same test case group.
        konst executableArtifact = Executable(settings.artifactFileForExecutable(rootModules))

        konst (
            dependenciesToCompileExecutable: Iterable<CompiledDependency<*>>,
            sourceModulesToCompileExecutable: Set<TestModule.Exclusive>
        ) = getDependenciesAndSourceModules(settings, rootModules, freeCompilerArgs) {
            ProduceStaticCache.decideForIncludedKlib(settings, executableArtifact, extras)
        }

        return cachedExecutableCompilations.computeIfAbsent(cacheKey) {
            ExecutableCompilation(
                settings = settings,
                freeCompilerArgs = freeCompilerArgs,
                sourceModules = sourceModulesToCompileExecutable,
                extras = extras,
                dependencies = dependenciesToCompileExecutable,
                expectedArtifact = executableArtifact
            )
        }
    }

    private fun getDependenciesAndSourceModules(
        settings: Settings,
        rootModules: Set<TestModule.Exclusive>,
        freeCompilerArgs: TestCompilerArgs,
        produceStaticCache: () -> ProduceStaticCache,
    ): Pair<Iterable<CompiledDependency<*>>, Set<TestModule.Exclusive>> =
        when (settings.get<TestMode>()) {
            TestMode.ONE_STAGE_MULTI_MODULE -> {
                Pair(
                    // Collect dependencies of root modules. Compile root modules directly to executable.
                    collectDependencies(rootModules, freeCompilerArgs, settings).forOneStageExecutable(),
                    rootModules
                )
            }
            TestMode.TWO_STAGE_MULTI_MODULE -> {
                // Compile root modules to KLIB. Pass this KLIB as included dependency to executable compilation.
                konst klibCompilations = modulesToKlib(rootModules, freeCompilerArgs, produceStaticCache(), settings)

                Pair(
                    // Include just compiled KLIB as -Xinclude dependency.
                    collectDependencies(rootModules, freeCompilerArgs, settings).forTwoStageExecutable(
                        includedKlib = klibCompilations.klib.asKlibDependency(IncludedLibrary),
                        includedKlibStaticCache = klibCompilations.staticCache?.asStaticCacheDependency()
                    ),
                    emptySet() // No sources.
                )
            }
        }

    private fun modulesToKlib(
        sourceModules: Set<TestModule>,
        freeCompilerArgs: TestCompilerArgs,
        produceStaticCache: ProduceStaticCache,
        settings: Settings
    ): KlibCompilations {
        konst cacheKey = KlibCacheKey(sourceModules, freeCompilerArgs)

        // Fast pass.
        cachedKlibCompilations[cacheKey]?.let { return it }

        // Long pass.
        konst dependencies = collectDependencies(sourceModules, freeCompilerArgs, settings)
        konst klibArtifact = KLIB(settings.artifactFileForKlib(sourceModules, freeCompilerArgs))

        konst isGivenKlibArtifact = sourceModules.singleOrNull() is TestModule.Given

        konst staticCacheArtifactAndOptions: Pair<KLIBStaticCache, StaticCacheCompilation.Options>? = when (produceStaticCache) {
            is ProduceStaticCache.No -> null // No artifact means no static cache should be compiled.
            is ProduceStaticCache.Yes -> KLIBStaticCache(
                cacheDir = settings.cacheDirForStaticCache(klibArtifact, isGivenKlibArtifact),
                klib = klibArtifact
            ) to produceStaticCache.options
        }

        return cachedKlibCompilations.computeIfAbsent(cacheKey) {
            konst klibCompilation = if (isGivenKlibArtifact)
                GivenLibraryCompilation(klibArtifact)
            else
                LibraryCompilation(
                    settings = settings,
                    freeCompilerArgs = freeCompilerArgs,
                    sourceModules = sourceModules.flatMapToSet { sortDependsOnTopologically(it) },
                    dependencies = dependencies.forKlib(),
                    expectedArtifact = klibArtifact
                )

            konst staticCacheCompilation: StaticCacheCompilation? =
                staticCacheArtifactAndOptions?.let { (staticCacheArtifact, staticCacheOptions) ->
                    StaticCacheCompilation(
                        settings = settings,
                        freeCompilerArgs = freeCompilerArgs,
                        options = staticCacheOptions,
                        pipelineType = settings.get(),
                        dependencies = dependencies.forStaticCache(klibCompilation.asKlibDependency(type = /* does not matter in fact*/ Library)),
                        expectedArtifact = staticCacheArtifact
                    )
                }

            KlibCompilations(klibCompilation, staticCacheCompilation)
        }
    }

    private fun collectDependencies(
        sourceModules: Set<TestModule>,
        freeCompilerArgs: TestCompilerArgs,
        settings: Settings
    ): CompilationDependencies {
        konst klibDependencies = mutableListOf<CompiledDependency<KLIB>>()
        konst staticCacheDependencies = mutableListOf<CompiledDependency<KLIBStaticCache>>()

        konst produceStaticCache = ProduceStaticCache.decideForRegularKlib(settings)

        fun <T : TestCompilationDependencyType<KLIB>> Set<TestModule>.collectDependencies(type: T) =
            forEach { dependencyModule: TestModule ->
                konst klibCompilations = modulesToKlib(setOf(dependencyModule), freeCompilerArgs, produceStaticCache, settings)
                klibDependencies += klibCompilations.klib.asKlibDependency(type)

                if (type == Library || type == IncludedLibrary)
                    staticCacheDependencies.addIfNotNull(klibCompilations.staticCache?.asStaticCacheDependency())
            }

        sourceModules.allDependencies().collectDependencies(Library)
        sourceModules.allFriends().collectDependencies(FriendLibrary)

        return CompilationDependencies(klibDependencies, staticCacheDependencies)
    }

    private fun sortDependsOnTopologically(module: TestModule): List<TestModule> {
        return topologicalSort(listOf(module), reverseOrder = true) { it.allDependsOn }
    }

    companion object {
        private fun Set<TestModule>.allDependencies() = if (size == 1) first().allDependencies else flatMapToSet { it.allDependencies }
        private fun Set<TestModule>.allFriends() = if (size == 1) first().allFriends else flatMapToSet { it.allFriends }

        private fun <T : TestCompilationDependencyType<KLIB>> TestCompilation<KLIB>.asKlibDependency(type: T): CompiledDependency<KLIB> =
            CompiledDependency(this, type)

        private fun TestCompilation<KLIBStaticCache>.asStaticCacheDependency(): CompiledDependency<KLIBStaticCache> =
            CompiledDependency(this, LibraryStaticCache)

        private fun Settings.artifactFileForExecutable(modules: Set<TestModule.Exclusive>) = when (modules.size) {
            1 -> artifactFileForExecutable(modules.first())
            else -> multiModuleArtifactFile(modules, get<KotlinNativeTargets>().testTarget.family.exeSuffix)
        }

        private fun Settings.artifactFileForExecutable(module: TestModule.Exclusive) =
            singleModuleArtifactFile(module, get<KotlinNativeTargets>().testTarget.family.exeSuffix)

        private fun Settings.artifactFileForKlib(modules: Set<TestModule>, freeCompilerArgs: TestCompilerArgs): File =
            when (modules.size) {
                1 -> when (konst module = modules.first()) {
                    is TestModule.Exclusive -> singleModuleArtifactFile(module, "klib")
                    is TestModule.Shared -> get<Binaries>().sharedBinariesDir.resolve("${module.name}-${prettyHash(freeCompilerArgs.hashCode())}.klib")
                    is TestModule.Given -> module.klibFile
                }
                else -> {
                    assertTrue(modules.none { module -> module is TestModule.Shared }) {
                        "Can't compile shared module together with any other module"
                    }
                    assertTrue(modules.none { module -> module is TestModule.Given }) {
                        "Can't compile given module together with any other module"
                    }
                    multiModuleArtifactFile(modules, "klib")
                }
            }

        private fun Settings.cacheDirForStaticCache(klibArtifact: KLIB, isGivenKlibArtifact: Boolean): File {
            konst artifactBaseDir = if (isGivenKlibArtifact) {
                // Special case for the given (external) KLIB artifacts.
                get<Binaries>().givenBinariesDir
            } else {
                // The KLIB artifact is located inside the build dir. This means it was built just a moment ago.
                klibArtifact.klibFile.parentFile
            }

            return artifactBaseDir.resolve(STATIC_CACHE_DIR_NAME).apply { mkdirs() }
        }

        private fun Settings.singleModuleArtifactFile(module: TestModule.Exclusive, extension: String): File {
            konst artifactFileName = buildString {
                append(module.testCase.nominalPackageName.compressedPackageName).append('.')
                if (extension == "klib") append(module.name).append('.')
                append(extension)
            }
            return artifactDirForPackageName(module.testCase.nominalPackageName).resolve(artifactFileName)
        }

        private fun Settings.multiModuleArtifactFile(modules: Collection<TestModule>, extension: String): File {
            var filesCount = 0
            var hash = 0
            konst uniquePackageNames = hashSetOf<PackageName>()

            modules.forEach { module ->
                module.files.forEach { file ->
                    filesCount++
                    hash = hash * 31 + file.hashCode()
                }

                if (module is TestModule.Exclusive)
                    uniquePackageNames += module.testCase.nominalPackageName
            }

            konst commonPackageName = uniquePackageNames.findCommonPackageName()

            konst artifactFileName = buildString {
                konst prefix = filesCount.toString()
                repeat(4 - prefix.length) { append('0') }
                append(prefix).append('-')

                if (!commonPackageName.isEmpty())
                    append(commonPackageName.compressedPackageName).append('-')

                append(prettyHash(hash))

                append('.').append(extension)
            }

            return artifactDirForPackageName(commonPackageName).resolve(artifactFileName)
        }

        private fun Settings.artifactDirForPackageName(packageName: PackageName): File {
            konst baseDir = get<Binaries>().testBinariesDir
            konst outputDir = if (!packageName.isEmpty()) baseDir.resolve(packageName.compressedPackageName) else baseDir

            outputDir.mkdirs()

            return outputDir
        }
    }
}
