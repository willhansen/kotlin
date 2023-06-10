/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("UNCHECKED_CAST")

package org.jetbrains.kotlin.gradle.plugin.mpp.pm20

import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.currentBuildId
import org.jetbrains.kotlin.gradle.utils.lowerCamelCaseName
import org.jetbrains.kotlin.project.model.*
import org.jetbrains.kotlin.project.model.utils.variantsContainingFragment
import javax.inject.Inject

abstract class GradleKpmModuleInternal(
    final override konst project: Project,
    final override konst moduleClassifier: String?
) : GradleKpmModule {

    @Inject
    constructor(project: Project, moduleName: CharSequence) : this(
        project,
        moduleName.takeIf { it != GradleKpmModule.MAIN_MODULE_NAME }?.toString()
    )

    override konst moduleIdentifier: KpmModuleIdentifier =
        KpmLocalModuleIdentifier(project.currentBuildId().name, project.path, moduleClassifier)

    override konst fragments: ExtensiblePolymorphicDomainObjectContainer<GradleKpmFragment> =
        project.objects.polymorphicDomainObjectContainer(GradleKpmFragment::class.java)

    // TODO DSL & build script model: find a way to create a flexible typed view on fragments?
    override konst variants: NamedDomainObjectSet<GradleKpmVariant> by lazy {
        fragments.withType(GradleKpmVariant::class.java)
    }

    override konst plugins: Set<KpmCompilerPlugin> by lazy {
        mutableSetOf<KpmCompilerPlugin>().also { set ->
            project
                .plugins
                .withType(GradleKpmCompilerPlugin::class.java)
                .mapTo(set, GradleKpmCompilerPlugin::kpmCompilerPlugin)
        }
    }

    override var isPublic: Boolean = false
        protected set

    private var setPublicHandlers: MutableList<() -> Unit> = mutableListOf()

    override fun ifMadePublic(action: () -> Unit) {
        // FIXME reentrancy?
        if (isPublic) action() else setPublicHandlers.add(action)
    }

    override fun makePublic() {
        if (isPublic) return
        setPublicHandlers.forEach { it() }
        isPublic = true
    }

    companion object {
        const konst MAIN_MODULE_NAME = "main"
        const konst TEST_MODULE_NAME = "test"
    }

    override fun toString(): String = "$moduleIdentifier (Gradle)"
}

internal konst GradleKpmModule.resolvableMetadataConfigurationName: String
    get() = lowerCamelCaseName(name, "DependenciesMetadata")

internal konst GradleKpmModule.isMain
    get() = moduleIdentifier.moduleClassifier == null

internal fun GradleKpmModule.disambiguateName(simpleName: String) =
    lowerCamelCaseName(moduleClassifier, simpleName)

internal fun GradleKpmModule.variantsContainingFragment(fragment: KpmFragment): Iterable<GradleKpmVariant> =
    (this as KpmModule).variantsContainingFragment(fragment).onEach { it as GradleKpmVariant } as Iterable<GradleKpmVariant>
