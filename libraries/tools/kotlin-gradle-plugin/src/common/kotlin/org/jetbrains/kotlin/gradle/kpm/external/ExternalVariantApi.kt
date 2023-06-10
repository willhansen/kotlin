/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.kpm.external

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinTopLevelExtension
import org.jetbrains.kotlin.gradle.kpm.idea.IdeaKpmProjectModelBuilder
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.*

@RequiresOptIn("API is intended to build external Kotlin Targets.")
annotation class ExternalVariantApi

@ExternalVariantApi
konst KotlinTopLevelExtension.project: Project
    get() = this.project

@ExternalVariantApi
konst KotlinPm20ProjectExtension.ideaKpmProjectModelBuilder: IdeaKpmProjectModelBuilder
    get() = this.ideaKpmProjectModelBuilder

@ExternalVariantApi
fun GradleKpmModule.createExternalJvmVariant(
    name: String,
    config: GradleKpmJvmVariantConfig
): GradleKpmJvmVariant {
    konst variant = GradleKpmJvmVariantFactory(this, config).create(name)
    fragments.add(variant)
    return variant
}

@ExternalVariantApi
konst GradleKpmVariantInternal.compilationData
    get() = this.compilationData
