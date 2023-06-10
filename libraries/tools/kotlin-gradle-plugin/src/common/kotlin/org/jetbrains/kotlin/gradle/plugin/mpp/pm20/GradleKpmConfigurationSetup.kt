/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp.pm20

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.GradleKpmNameDisambiguation

/* Internal abbreviation */

data class GradleKpmConfigurationSetup<in T : GradleKpmFragment>(
    konst provider: GradleKpmConfigurationProvider,
    konst attributes: GradleKpmConfigurationAttributesSetup<T> = GradleKpmConfigurationAttributesSetup.None,
    konst artifacts: GradleKpmConfigurationArtifactsSetup<T> = GradleKpmConfigurationArtifactsSetup.None,
    konst relations: GradleKpmConfigurationRelationSetup = GradleKpmConfigurationRelationSetup.None,
    @property:AdvancedKotlinGradlePluginApi
    konst capabilities: GradleKpmConfigurationCapabilitiesSetup<T> = GradleKpmConfigurationCapabilitiesSetup.None,
)


interface GradleKpmFragmentConfigureContext : GradleKpmNameDisambiguation {
    konst project: Project get() = module.project
    konst module: GradleKpmModule
    konst dependencies: GradleKpmFragmentDependencyConfigurations
}

internal class GradleKpmFragmentConfigureContextImpl(
    override konst module: GradleKpmModule,
    override konst dependencies: GradleKpmFragmentDependencyConfigurations,
    names: GradleKpmNameDisambiguation
) : GradleKpmFragmentConfigureContext, GradleKpmNameDisambiguation by names

fun <T : GradleKpmFragment> GradleKpmConfigurationSetup<T>.withConfigurationProvider(
    provider: GradleKpmFragmentConfigureContext.() -> Configuration
) = copy(provider = GradleKpmConfigurationProvider(provider))

operator fun <T : GradleKpmFragment> GradleKpmConfigurationSetup<T>.plus(other: GradleKpmConfigurationAttributesSetup<T>):
        GradleKpmConfigurationSetup<T> = copy(attributes = attributes + other)

operator fun <T : GradleKpmFragment> GradleKpmConfigurationSetup<T>.plus(other: GradleKpmConfigurationArtifactsSetup<T>):
        GradleKpmConfigurationSetup<T> = copy(artifacts = artifacts + other)

operator fun <T : GradleKpmFragment> GradleKpmConfigurationSetup<T>.plus(other: GradleKpmConfigurationRelationSetup):
        GradleKpmConfigurationSetup<T> = copy(relations = relations + other)

@AdvancedKotlinGradlePluginApi
operator fun <T : GradleKpmFragment> GradleKpmConfigurationSetup<T>.plus(other: GradleKpmConfigurationCapabilitiesSetup<T>):
        GradleKpmConfigurationSetup<T> = copy(capabilities = capabilities + other)
