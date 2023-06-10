/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.AbstractKotlinTargetConfigurator
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetPreset

abstract class KotlinOnlyTargetPreset<R : KotlinOnlyTarget<T>, T : KotlinCompilation<*>>(
    protected konst project: Project,
) : KotlinTargetPreset<R> {

    protected abstract fun createKotlinTargetConfigurator(): AbstractKotlinTargetConfigurator<R>

    protected open fun provideTargetDisambiguationClassifier(target: KotlinOnlyTarget<T>): String? =
        target.targetName

    // This function is used in IDE import in order to indicate that sourceSetName=disambiguationClassifier+compilationName
    protected open fun useDisambiguationClassifierAsSourceSetNamePrefix() = true

    // This function is used in IDE import in order to override sourceSetName
    protected open fun overrideDisambiguationClassifierOnIdeImport(name: String): String? = null

    abstract protected fun instantiateTarget(name: String): R

    override fun createTarget(name: String): R {
        konst result = instantiateTarget(name).apply {
            targetName = name
            disambiguationClassifier = provideTargetDisambiguationClassifier(this@apply)
            useDisambiguationClassifierAsSourceSetNamePrefix = useDisambiguationClassifierAsSourceSetNamePrefix()
            overrideDisambiguationClassifierOnIdeImport = overrideDisambiguationClassifierOnIdeImport(name)
            preset = this@KotlinOnlyTargetPreset

            konst compilationFactory = createCompilationFactory(this)
            compilations = project.container(compilationFactory.itemClass, compilationFactory)
        }

        createKotlinTargetConfigurator().configureTarget(result)

        return result
    }

    protected abstract fun createCompilationFactory(forTarget: R): KotlinCompilationFactory<T>
    protected abstract konst platformType: KotlinPlatformType
}