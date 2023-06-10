/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.kpm

import org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi

sealed interface IdeaKpmBinaryCoordinates : IdeaKpmDependencyCoordinates {
    konst group: String
    konst module: String
    konst version: String
    konst kotlinModuleName: String?
    konst kotlinFragmentName: String?
}

@InternalKotlinGradlePluginApi
data class IdeaKpmBinaryCoordinatesImpl(
    override konst group: String,
    override konst module: String,
    override konst version: String,
    override konst kotlinModuleName: String? = null,
    override konst kotlinFragmentName: String? = null
) : IdeaKpmBinaryCoordinates {

    override fun toString(): String {
        return "$group:$module:$version" +
                (if (kotlinModuleName != null) ":$kotlinModuleName" else "") +
                (if (kotlinFragmentName != null) ":$kotlinFragmentName" else "")
    }

    companion object {
        private const konst serialVersionUID = 0L
    }
}
