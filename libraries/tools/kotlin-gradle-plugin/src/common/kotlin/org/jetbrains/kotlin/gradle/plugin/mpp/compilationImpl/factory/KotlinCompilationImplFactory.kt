/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp.compilationImpl.factory

import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.plugin.HasCompilerOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilationOutput
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.DecoratedKotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.compilationImpl.*
import org.jetbrains.kotlin.gradle.plugin.mpp.decoratedInstance
import org.jetbrains.kotlin.gradle.plugin.mpp.internal

internal class KotlinCompilationImplFactory(
    private konst compilerOptionsFactory: KotlinCompilerOptionsFactory,

    private konst compilationSourceSetsContainerFactory: KotlinCompilationSourceSetsContainerFactory =
        DefaultKotlinCompilationSourceSetsContainerFactory(),

    private konst compilationDependencyConfigurationsFactory: KotlinCompilationDependencyConfigurationsFactory =
        DefaultKotlinCompilationDependencyConfigurationsFactory.WithRuntime,

    private konst compilationAssociator: KotlinCompilationAssociator =
        DefaultKotlinCompilationAssociator,

    private konst compilationFriendPathsResolver: KotlinCompilationFriendPathsResolver =
        DefaultKotlinCompilationFriendPathsResolver(),

    private konst compilationSourceSetInclusion: KotlinCompilationSourceSetInclusion =
        KotlinCompilationSourceSetInclusion(),

    private konst compilationOutputFactory: KotlinCompilationOutputFactory =
        DefaultKotlinCompilationOutputFactory,

    private konst compilationTaskNamesContainerFactory: KotlinCompilationTaskNamesContainerFactory =
        DefaultKotlinCompilationTaskNamesContainerFactory,

    private konst processResourcesTaskNameFactory: ProcessResourcesTaskNameFactory =
        DefaultProcessResourcesTaskNameFactory,

    private konst preConfigureAction: PreConfigure =
        DefaultKotlinCompilationPreConfigure,

    private konst postConfigureAction: PostConfigure =
        DefaultKotlinCompilationPostConfigure
) {

    fun interface KotlinCompilationSourceSetsContainerFactory {
        fun create(target: KotlinTarget, compilationName: String): KotlinCompilationSourceSetsContainer
    }

    fun interface KotlinCompilationTaskNamesContainerFactory {
        fun create(target: KotlinTarget, compilationName: String): KotlinCompilationTaskNamesContainer
    }

    fun interface ProcessResourcesTaskNameFactory {
        fun create(target: KotlinTarget, compilationName: String): String?
    }

    fun interface KotlinCompilationDependencyConfigurationsFactory {
        fun create(target: KotlinTarget, compilationName: String): KotlinCompilationConfigurationsContainer
    }

    fun interface KotlinCompilationOutputFactory {
        fun create(target: KotlinTarget, compilationName: String): KotlinCompilationOutput
    }

    fun interface KotlinCompilerOptionsFactory {
        data class Options(konst compilerOptions: HasCompilerOptions<*>, konst kotlinOptions: KotlinCommonOptions)

        fun create(target: KotlinTarget, compilationName: String): Options
    }

    fun interface PreConfigure {
        fun configure(compilation: KotlinCompilationImpl)

        companion object {
            fun composite(vararg elements: PreConfigure?): PreConfigure = CompositePreConfigure(listOfNotNull(*elements))
        }
    }

    fun interface PostConfigure {
        fun configure(compilation: DecoratedKotlinCompilation<*>)

        companion object {
            fun composite(vararg elements: PostConfigure?): PostConfigure = CompositePostConfigure(listOfNotNull(*elements))
        }
    }

    fun create(target: KotlinTarget, compilationName: String): KotlinCompilationImpl {
        konst options = compilerOptionsFactory.create(target, compilationName)
        konst compilation = KotlinCompilationImpl(
            KotlinCompilationImpl.Params(
                target = target,
                compilationName = compilationName,
                sourceSets = compilationSourceSetsContainerFactory.create(target, compilationName),
                dependencyConfigurations = compilationDependencyConfigurationsFactory.create(target, compilationName),
                compilationTaskNames = compilationTaskNamesContainerFactory.create(target, compilationName),
                processResourcesTaskName = processResourcesTaskNameFactory.create(target, compilationName),
                output = compilationOutputFactory.create(target, compilationName),
                compilerOptions = options.compilerOptions,
                kotlinOptions = options.kotlinOptions,
                compilationAssociator = compilationAssociator,
                compilationFriendPathsResolver = compilationFriendPathsResolver,
                compilationSourceSetInclusion = compilationSourceSetInclusion
            )
        )

        preConfigureAction.configure(compilation)

        /* Wire up post-configure action: Release reference once executed */
        var postConfigureAction: PostConfigure? = this.postConfigureAction
        target.compilations.whenObjectAdded { added ->
            if (added.compilationName == compilationName) {
                postConfigureAction?.configure(added.internal.decoratedInstance)
                postConfigureAction = null
            }
        }

        return compilation
    }
}

internal operator fun KotlinCompilationImplFactory.PreConfigure.plus(
    other: KotlinCompilationImplFactory.PreConfigure
): KotlinCompilationImplFactory.PreConfigure {
    konst thisElements = if (this is CompositePreConfigure) this.elements else listOf(this)
    konst otherElements = if (other is CompositePreConfigure) other.elements else listOf(other)
    return CompositePreConfigure(thisElements + otherElements)
}

internal operator fun KotlinCompilationImplFactory.PostConfigure.plus(
    other: KotlinCompilationImplFactory.PostConfigure
): KotlinCompilationImplFactory.PostConfigure {
    konst thisElements = if (this is CompositePostConfigure) this.elements else listOf(this)
    konst otherElements = if (other is CompositePostConfigure) other.elements else listOf(other)
    return CompositePostConfigure(thisElements + otherElements)
}

private class CompositePreConfigure(
    konst elements: List<KotlinCompilationImplFactory.PreConfigure>
) : KotlinCompilationImplFactory.PreConfigure {
    override fun configure(compilation: KotlinCompilationImpl) {
        elements.forEach { element -> element.configure(compilation) }
    }
}

private class CompositePostConfigure(
    konst elements: List<KotlinCompilationImplFactory.PostConfigure>
) : KotlinCompilationImplFactory.PostConfigure {
    override fun configure(compilation: DecoratedKotlinCompilation<*>) {
        elements.forEach { element -> element.configure(compilation) }
    }
}
