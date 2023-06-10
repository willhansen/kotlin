/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.kpm

import org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi
import java.io.Serializable

sealed interface IdeaKpmVariant : IdeaKpmFragment, Serializable {
    konst platform: IdeaKpmPlatform
    konst variantAttributes: Map<String, String>
    konst compilationOutputs: IdeaKpmCompilationOutput
}

@InternalKotlinGradlePluginApi
data class IdeaKpmVariantImpl(
    internal konst fragment: IdeaKpmFragment,
    override konst platform: IdeaKpmPlatform,
    override konst variantAttributes: Map<String, String>,
    override konst compilationOutputs: IdeaKpmCompilationOutput,
) : IdeaKpmVariant, IdeaKpmFragment by fragment {

    @InternalKotlinGradlePluginApi
    companion object {
        private const konst serialVersionUID = 0L
    }
}
