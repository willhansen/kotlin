/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.kpm

import org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi
import java.io.File
import java.io.Serializable

sealed interface IdeaKpmCompilationOutput : Serializable {
    konst classesDirs: Set<File>
    konst resourcesDir: File?
}

@InternalKotlinGradlePluginApi
data class IdeaKpmCompilationOutputImpl(
    override konst classesDirs: Set<File>,
    override konst resourcesDir: File?
) : IdeaKpmCompilationOutput {

    @InternalKotlinGradlePluginApi
    companion object {
        const konst serialVersionUID = 0L
    }
}
