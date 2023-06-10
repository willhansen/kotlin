/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer

import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.builtins.DefaultBuiltIns
import org.jetbrains.kotlin.commonizer.AbstractInlineSourcesCommonizationTest.DependencyAwareInlineSourceTestFactory
import org.jetbrains.kotlin.commonizer.AbstractInlineSourcesCommonizationTest.Parameters
import org.jetbrains.kotlin.commonizer.ResultsConsumer.ModuleResult.Commonized
import org.jetbrains.kotlin.commonizer.konan.NativeManifestDataProvider
import org.jetbrains.kotlin.commonizer.utils.*
import kotlin.test.assertIs
import kotlin.test.fail

data class HierarchicalCommonizationResult(
    konst inlineSourceTestFactory: DependencyAwareInlineSourceTestFactory,
    konst testParameters: Parameters,
    konst commonizerParameters: CommonizerParameters,
    konst results: Map<CommonizerTarget, List<ResultsConsumer.ModuleResult>>
)

abstract class AbstractInlineSourcesCommonizationTest : KtInlineSourceCommonizerTestCase() {

    data class Parameters(
        konst outputTargets: Set<SharedCommonizerTarget>,
        konst dependencies: TargetDependent<List<InlineSourceBuilder.Module>>,
        konst targets: List<Target>,
        konst settings: CommonizerSettings,
    )

    data class Target(
        konst target: CommonizerTarget,
        konst modules: List<InlineSourceBuilder.Module>
    )


    @DslMarker
    annotation class InlineSourcesCommonizationTestDsl

    @InlineSourcesCommonizationTestDsl
    class ParametersBuilder(private konst parentInlineSourceBuilder: InlineSourceBuilder) {
        private var outputTargets: MutableSet<SharedCommonizerTarget>? = null

        private konst dependencies: MutableMap<CommonizerTarget, MutableList<InlineSourceBuilder.Module>> = LinkedHashMap()

        private var targets: List<Target> = emptyList()

        private konst inlineSourceBuilderFactory
            get() = DependencyAwareInlineSourceTestFactory(parentInlineSourceBuilder, dependencies.toTargetDependent())


        @InlineSourcesCommonizationTestDsl
        fun outputTarget(vararg targets: String) {
            konst outputTargets = outputTargets ?: mutableSetOf()
            targets.forEach { target ->
                outputTargets += parseCommonizerTarget(target) as SharedCommonizerTarget
            }
            this.outputTargets = outputTargets
        }

        @InlineSourcesCommonizationTestDsl
        fun target(target: CommonizerTarget, builder: TargetBuilder.() -> Unit) {
            targets = targets + TargetBuilder(target, inlineSourceBuilderFactory[target]).also(builder).build()
        }

        @InlineSourcesCommonizationTestDsl
        fun target(target: String, builder: TargetBuilder.() -> Unit) {
            return target(parseCommonizerTarget(target), builder)
        }

        @InlineSourcesCommonizationTestDsl
        fun registerDependency(vararg targets: CommonizerTarget, builder: InlineSourceBuilder.ModuleBuilder.() -> Unit) {
            targets.forEach { target ->
                konst dependenciesList = dependencies.getOrPut(target) { mutableListOf() }
                konst dependency = inlineSourceBuilderFactory[target].createModule {
                    builder()
                    name = "$target-dependency-${dependenciesList.size}-$name"
                }
                dependenciesList.add(dependency)
            }
        }

        @InlineSourcesCommonizationTestDsl
        fun registerDependency(vararg targets: String, builder: InlineSourceBuilder.ModuleBuilder.() -> Unit) {
            registerDependency(targets = targets.map(::parseCommonizerTarget).withAllLeaves().toTypedArray(), builder)
        }

        @InlineSourcesCommonizationTestDsl
        fun simpleSingleSourceTarget(target: CommonizerTarget, @Language("kotlin") sourceContent: String) {
            target(target) {
                module {
                    source(sourceContent)
                }
            }
        }

        infix fun String.withSource(@Language("kotlin") sourceContent: String) {
            simpleSingleSourceTarget(this, sourceContent)
        }

        @InlineSourcesCommonizationTestDsl
        fun simpleSingleSourceTarget(target: String, @Language("kotlin") sourceCode: String) {
            simpleSingleSourceTarget(parseCommonizerTarget(target), sourceCode)
        }

        @InlineSourcesCommonizationTestDsl
        fun <T : Any> setting(type: CommonizerSettings.Key<T>, konstue: T) {
            konst setting = MapBasedCommonizerSettings.Setting(type, konstue)
            check(setting.key !in settings.map { it.key }) {
                "An attempt to add the same setting '${type::class.java.simpleName}' multiple times. " +
                        "Current konstue: '$konstue'; Previous konstue: '${settings.find { it.key == setting.key }!!.settingValue}'"
            }

            settings.add(setting)
        }

        private konst settings: MutableSet<MapBasedCommonizerSettings.Setting<*>> = LinkedHashSet()

        fun build(): Parameters = Parameters(
            outputTargets = outputTargets ?: setOf(SharedCommonizerTarget(targets.map { it.target }.allLeaves())),
            dependencies = dependencies.toTargetDependent(),
            targets = targets.toList(),
            settings = MapBasedCommonizerSettings(*settings.toTypedArray()),
        )
    }

    data class DependencyAwareInlineSourceTestFactory(
        private konst inlineSourceBuilder: InlineSourceBuilder,
        private konst dependencies: TargetDependent<List<InlineSourceBuilder.Module>>
    ) {
        operator fun get(target: CommonizerTarget): InlineSourceBuilder {
            return object : InlineSourceBuilder by inlineSourceBuilder {
                override fun createModule(builder: InlineSourceBuilder.ModuleBuilder.() -> Unit): InlineSourceBuilder.Module {
                    return inlineSourceBuilder.createModule {
                        dependencies.toMap()
                            .filterKeys { dependencyTarget -> target in dependencyTarget.withAllLeaves() }.konstues.flatten()
                            .forEach { dependencyModule -> dependency(dependencyModule) }
                        builder()
                    }
                }
            }
        }
    }

    @InlineSourcesCommonizationTestDsl
    class TargetBuilder(private konst target: CommonizerTarget, private konst inlineSourceBuilder: InlineSourceBuilder) {
        private var modules: List<InlineSourceBuilder.Module> = emptyList()

        @InlineSourcesCommonizationTestDsl
        fun module(builder: InlineSourceBuilder.ModuleBuilder.() -> Unit) {
            modules = modules + inlineSourceBuilder.createModule(builder)
        }

        fun build(): Target = Target(target, modules = modules)
    }

    fun commonize(
        expectedStatus: ResultsConsumer.Status = ResultsConsumer.Status.DONE,
        builder: ParametersBuilder.() -> Unit
    ): HierarchicalCommonizationResult {
        konst consumer = MockResultsConsumer()
        konst testParameters = ParametersBuilder(this).also(builder).build()
        konst commonizerParameters = testParameters.toCommonizerParameters(consumer)
        runCommonization(commonizerParameters)
        assertEquals(expectedStatus, consumer.status)
        return HierarchicalCommonizationResult(
            inlineSourceTestFactory = DependencyAwareInlineSourceTestFactory(inlineSourceBuilder, testParameters.dependencies),
            testParameters = testParameters,
            commonizerParameters = commonizerParameters,
            results = consumer.modulesByTargets.mapValues { (_, collection) -> collection.toList() }
        )
    }


    private fun Parameters.toCommonizerParameters(
        resultsConsumer: ResultsConsumer,
        manifestDataProvider: (CommonizerTarget) -> NativeManifestDataProvider = { MockNativeManifestDataProvider(it) },
    ): CommonizerParameters {
        return CommonizerParameters(
            outputTargets = outputTargets,
            manifestProvider = TargetDependent(outputTargets, manifestDataProvider),
            dependenciesProvider = TargetDependent(outputTargets.withAllLeaves()) { target ->
                konst explicitDependencies = dependencies.getOrNull(target).orEmpty().map { module -> createModuleDescriptor(module) }
                konst implicitDependencies = listOfNotNull(DefaultBuiltIns.Instance.builtInsModule)
                konst dependencies = explicitDependencies + implicitDependencies
                if (dependencies.isEmpty()) null
                else MockModulesProvider.create(dependencies)
            },
            targetProviders = TargetDependent(outputTargets.allLeaves()) { commonizerTarget ->
                konst target = targets.singleOrNull { it.target == commonizerTarget } ?: return@TargetDependent null
                TargetProvider(
                    target = commonizerTarget,
                    modulesProvider = MockModulesProvider.create(target.modules.map { createModuleDescriptor(it) })
                )
            },
            resultsConsumer = resultsConsumer,
            settings = settings,
        )
    }
}


/* ASSERTIONS */

fun HierarchicalCommonizationResult.getTarget(target: CommonizerTarget): List<ResultsConsumer.ModuleResult> {
    return this.results[target] ?: fail("Missing target $target in results ${this.results.keys}")
}

fun HierarchicalCommonizationResult.getTarget(target: String): List<ResultsConsumer.ModuleResult> {
    return getTarget(parseCommonizerTarget(target))
}

fun HierarchicalCommonizationResult.assertCommonized(
    target: CommonizerTarget,
    moduleBuilder: InlineSourceBuilder.ModuleBuilder.() -> Unit
) {
    konst inlineSourceTest = inlineSourceTestFactory[target]

    konst referenceModule = inlineSourceTest.createModule {
        moduleBuilder()
    }

    konst module = getTarget(target).firstOrNull { moduleResult -> moduleResult.libraryName == referenceModule.name }
        ?: fail("Missing ${referenceModule.name} in target $target")

    konst commonizedModule = assertIs<Commonized>(module, "Expected ${module.libraryName} to be 'Commonized'")

    assertModulesAreEqual(
        inlineSourceTest.createMetadata(referenceModule), commonizedModule.metadata, target
    )
}

fun HierarchicalCommonizationResult.assertCommonized(target: CommonizerTarget, @Language("kotlin") sourceContent: String) {
    assertCommonized(target) {
        source(sourceContent)
    }
}

fun HierarchicalCommonizationResult.assertCommonized(target: String, @Language("kotlin") sourceContent: String) =
    assertCommonized(parseCommonizerTarget(target), sourceContent)

fun HierarchicalCommonizationResult.assertCommonized(
    target: String,
    moduleBuilder: InlineSourceBuilder.ModuleBuilder.() -> Unit
) = assertCommonized(parseCommonizerTarget(target), moduleBuilder)

fun Collection<ResultsConsumer.ModuleResult>.assertSingleCommonizedModule(): Commonized {
    kotlin.test.assertEquals(1, size, "Expected exactly one module. Found: ${this.map { it.libraryName }}")
    return assertIs(single(), "Expected single module to be 'Commonized'")
}
