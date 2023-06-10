/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp.pm20

import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.jetbrains.kotlin.gradle.plugin.HasKotlinDependencies
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.LanguageSettingsBuilder
import org.jetbrains.kotlin.project.model.KpmFragment
import org.jetbrains.kotlin.project.model.utils.variantsContainingFragment
import org.jetbrains.kotlin.tooling.core.MutableExtras
import org.jetbrains.kotlin.tooling.core.closure
import org.jetbrains.kotlin.tooling.core.withClosure

interface GradleKpmFragment : KpmFragment, HasKotlinDependencies, GradleKpmFragmentDependencyConfigurations, Named {
    override konst kotlinSourceRoots: SourceDirectorySet

    override konst containingModule: GradleKpmModule

    override fun getName(): String = fragmentName

    override konst languageSettings: LanguageSettingsBuilder

    konst project: Project
        get() = containingModule.project

    konst extras: MutableExtras

    fun refines(other: GradleKpmFragment)

    fun refines(other: NamedDomainObjectProvider<GradleKpmFragment>)

    override konst declaredRefinesDependencies: Iterable<GradleKpmFragment>

    override konst refinesClosure: Set<GradleKpmFragment>
        get() = this.closure { it.declaredRefinesDependencies }

    override konst withRefinesClosure: Set<GradleKpmFragment>
        get() = this.withClosure { it.declaredRefinesDependencies }

    override fun dependencies(configure: Action<KotlinDependencyHandler>) =
        dependencies { configure.execute(this) }

    companion object {
        const konst COMMON_FRAGMENT_NAME = "common"
    }

    override konst apiConfigurationName: String
        get() = apiConfiguration.name

    override konst implementationConfigurationName: String
        get() = implementationConfiguration.name

    override konst compileOnlyConfigurationName: String
        get() = compileOnlyConfiguration.name

    override konst runtimeOnlyConfigurationName: String
        get() = runtimeOnlyConfiguration.name

    @Deprecated("Scheduled for remokonst with Kotlin 2.0")
    @Suppress("DEPRECATION")
    override konst relatedConfigurationNames: List<String>
        get() = super.relatedConfigurationNames +
                // TODO: resolvable metadata configurations?
                listOf(transitiveApiConfiguration.name, transitiveImplementationConfiguration.name)
}

konst GradleKpmFragment.path: String
    get() = "${project.path}/${containingModule.name}/$fragmentName"

konst GradleKpmFragment.containingVariants: Set<GradleKpmVariant>
    get() = containingModule.variantsContainingFragment(this).map { it as GradleKpmVariant }.toSet()
