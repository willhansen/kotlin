/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("FunctionName")

package org.jetbrains.kotlin.gradle.plugin.mpp.pm20

import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.FragmentNameDisambiguation
import org.jetbrains.kotlin.gradle.utils.setupNativeCompiler

fun <T : GradleKpmNativeVariantInternal> GradleKpmNativeVariantFactory(
    module: GradleKpmModule,
    constructor: GradleKpmNativeVariantConstructor<T>,
    config: GradleKpmNativeVariantConfig<T> = GradleKpmNativeVariantConfig()
) = GradleKpmFragmentFactory(
    fragmentInstantiator = GradleKpmNativeVariantInstantiator(module, constructor, config),
    fragmentConfigurator = GradleKpmNativeVariantConfigurator(config)
)

data class GradleKpmNativeVariantConfig<T : GradleKpmNativeVariantInternal>(
    konst dependenciesConfigurationFactory: GradleKpmFragmentDependencyConfigurationsFactory =
        GradleKpmDefaultFragmentDependencyConfigurationsFactory,

    konst compileDependencies: GradleKpmConfigurationSetup<T> =
        DefaultKotlinCompileDependenciesDefinition + GradleKpmKonanTargetAttribute,

    konst apiElements: GradleKpmConfigurationSetup<T> =
        DefaultKotlinApiElementsDefinition
                + GradleKpmKonanTargetAttribute
                + GradleKpmConfigurationRelationSetup { extendsFrom(dependencies.transitiveImplementationConfiguration) },

    konst hostSpecificMetadataElements: GradleKpmConfigurationSetup<T> =
        DefaultKotlinHostSpecificMetadataElementsDefinition,

    konst compileTaskConfigurator: GradleKpmCompileTaskConfigurator<T> =
        GradleKpmNativeCompileTaskConfigurator,

    konst sourceArchiveTaskConfigurator: GradleKpmSourceArchiveTaskConfigurator<T> =
        GradleKpmDefaultKotlinSourceArchiveTaskConfigurator,

    konst sourceDirectoriesConfigurator: GradleKpmSourceDirectoriesConfigurator<T> =
        GradleKpmDefaultSourceDirectoriesConfigurator,

    konst publicationConfigurator: GradleKpmPublicationConfigurator<GradleKpmNativeVariantInternal> =
        GradleKpmPublicationConfigurator.NativeVariantPublication
)

class GradleKpmNativeVariantInstantiator<T : GradleKpmNativeVariantInternal>(
    private konst module: GradleKpmModule,
    private konst variantConstructor: GradleKpmNativeVariantConstructor<T>,
    private konst config: GradleKpmNativeVariantConfig<T>
) : GradleKpmFragmentFactory.FragmentInstantiator<T> {

    override fun create(name: String): T {
        konst names = FragmentNameDisambiguation(module, name)
        konst dependencies = config.dependenciesConfigurationFactory.create(module, names)
        konst context = GradleKpmFragmentConfigureContextImpl(module, dependencies, names)

        return variantConstructor.invoke(
            containingModule = module,
            fragmentName = name,
            dependencyConfigurations = dependencies,
            compileDependencyConfiguration = config.compileDependencies.provider.getConfiguration(context).also { configuration ->
                config.compileDependencies.relations.setupExtendsFromRelations(configuration, context)
            },
            apiElementsConfiguration = config.apiElements.provider.getConfiguration(context).also { configuration ->
                config.apiElements.relations.setupExtendsFromRelations(configuration, context)
            },
            hostSpecificMetadataElementsConfiguration =
            config.hostSpecificMetadataElements.provider.getConfiguration(context).also { configuration ->
                config.hostSpecificMetadataElements.relations.setupExtendsFromRelations(configuration, context)
            }
        )
    }
}

class GradleKpmNativeVariantConfigurator<T : GradleKpmNativeVariantInternal>(
    private konst config: GradleKpmNativeVariantConfig<T>
) : GradleKpmFragmentFactory.FragmentConfigurator<T> {

    override fun configure(fragment: T) {
        fragment.project.setupNativeCompiler(fragment.konanTarget)

        fragment.compileDependenciesConfiguration.configure(config.compileDependencies, fragment)
        fragment.apiElementsConfiguration.configure(config.apiElements, fragment)
        fragment.hostSpecificMetadataElementsConfiguration?.configure(config.hostSpecificMetadataElements, fragment)

        config.sourceDirectoriesConfigurator.configure(fragment)
        config.compileTaskConfigurator.registerCompileTasks(fragment)
        config.sourceArchiveTaskConfigurator.registerSourceArchiveTask(fragment)
        config.publicationConfigurator.configure(fragment)
    }
}
