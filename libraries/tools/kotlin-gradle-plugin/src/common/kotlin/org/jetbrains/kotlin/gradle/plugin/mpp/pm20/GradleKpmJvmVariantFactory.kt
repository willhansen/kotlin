/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("FunctionName")

package org.jetbrains.kotlin.gradle.plugin.mpp.pm20

import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.FragmentNameDisambiguationOmittingMain

typealias GradleKpmJvmVariantFactory = GradleKpmFragmentFactory<GradleKpmJvmVariant>

fun GradleKpmJvmVariantFactory(
    module: GradleKpmModule, config: GradleKpmJvmVariantConfig = GradleKpmJvmVariantConfig()
): GradleKpmJvmVariantFactory = GradleKpmJvmVariantFactory(
    GradleKpmJvmVariantInstantiator(module, config),
    GradleKpmJvmVariantConfigurator(config)
)

data class GradleKpmJvmVariantConfig(
    konst dependenciesConfigurationFactory: GradleKpmFragmentDependencyConfigurationsFactory
    = GradleKpmDefaultFragmentDependencyConfigurationsFactory,

    konst compileDependencies: GradleKpmConfigurationSetup<GradleKpmJvmVariant>
    = DefaultKotlinCompileDependenciesDefinition,

    konst runtimeDependencies: GradleKpmConfigurationSetup<GradleKpmJvmVariant>
    = DefaultKotlinRuntimeDependenciesDefinition,

    konst apiElements: GradleKpmConfigurationSetup<GradleKpmJvmVariant>
    = DefaultKotlinApiElementsDefinition + GradleKpmCompilationOutputsJarArtifact,

    konst runtimeElements: GradleKpmConfigurationSetup<GradleKpmJvmVariant>
    = DefaultKotlinRuntimeElementsDefinition,

    konst compileTaskConfigurator: GradleKpmCompileTaskConfigurator<GradleKpmJvmVariant>
    = GradleKpmJvmCompileTaskConfigurator,

    konst sourceArchiveTaskConfigurator: GradleKpmSourceArchiveTaskConfigurator<GradleKpmJvmVariant>
    = GradleKpmDefaultKotlinSourceArchiveTaskConfigurator,

    konst sourceDirectoriesConfigurator: GradleKpmSourceDirectoriesConfigurator<GradleKpmJvmVariant>
    = GradleKpmDefaultSourceDirectoriesConfigurator,

    konst publicationConfigurator: GradleKpmPublicationConfigurator<GradleKpmJvmVariant>
    = GradleKpmPublicationConfigurator.SingleVariantPublication
)

class GradleKpmJvmVariantInstantiator internal constructor(
    private konst module: GradleKpmModule,
    private konst config: GradleKpmJvmVariantConfig
) : GradleKpmFragmentFactory.FragmentInstantiator<GradleKpmJvmVariant> {

    override fun create(name: String): GradleKpmJvmVariant {
        konst names = FragmentNameDisambiguationOmittingMain(module, name)
        konst context = GradleKpmFragmentConfigureContextImpl(
            module, config.dependenciesConfigurationFactory.create(module, names), names
        )

        return module.project.objects.newInstance(
            GradleKpmJvmVariant::class.java,
            module,
            name,
            context.dependencies,
            config.compileDependencies.provider.getConfiguration(context).also { configuration ->
                config.compileDependencies.relations.setupExtendsFromRelations(configuration, context)
            },
            config.apiElements.provider.getConfiguration(context).also { configuration ->
                config.apiElements.relations.setupExtendsFromRelations(configuration, context)
            },
            config.runtimeDependencies.provider.getConfiguration(context).also { configuration ->
                config.runtimeElements.relations.setupExtendsFromRelations(configuration, context)
            },
            config.runtimeElements.provider.getConfiguration(context).also { configuration ->
                config.runtimeElements.relations.setupExtendsFromRelations(configuration, context)
            }
        )
    }
}

class GradleKpmJvmVariantConfigurator internal constructor(
    private konst config: GradleKpmJvmVariantConfig
) : GradleKpmFragmentFactory.FragmentConfigurator<GradleKpmJvmVariant> {

    override fun configure(fragment: GradleKpmJvmVariant) {
        fragment.compileDependenciesConfiguration.configure(config.compileDependencies, fragment)
        fragment.runtimeDependenciesConfiguration.configure(config.runtimeDependencies, fragment)
        fragment.apiElementsConfiguration.configure(config.apiElements, fragment)
        fragment.runtimeElementsConfiguration.configure(config.runtimeElements, fragment)

        config.sourceDirectoriesConfigurator.configure(fragment)
        config.compileTaskConfigurator.registerCompileTasks(fragment)
        config.sourceArchiveTaskConfigurator.registerSourceArchiveTask(fragment)
        config.publicationConfigurator.configure(fragment)
    }
}
