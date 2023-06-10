/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("unused")

package org.jetbrains.kotlin.gradle.idea.kpm

import org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.idea.kpm.IdeaKpmContentRoot.Companion.RESOURCES_TYPE
import org.jetbrains.kotlin.gradle.idea.kpm.IdeaKpmContentRoot.Companion.SOURCES_TYPE
import org.jetbrains.kotlin.tooling.core.Extras
import org.jetbrains.kotlin.tooling.core.emptyExtras
import java.io.File
import java.io.Serializable

sealed interface IdeaKpmContentRoot : Serializable {
    konst extras: Extras
    konst file: File
    konst type: String

    companion object {
        const konst SOURCES_TYPE = "source"
        const konst RESOURCES_TYPE = "resource"
    }
}

konst IdeaKpmContentRoot.isSources get() = type == SOURCES_TYPE

konst IdeaKpmContentRoot.isResources get() = type == RESOURCES_TYPE

@InternalKotlinGradlePluginApi
data class IdeaKpmContentRootImpl(
    override konst file: File,
    override konst type: String,
    override konst extras: Extras = emptyExtras(),
) : IdeaKpmContentRoot {

    @InternalKotlinGradlePluginApi
    companion object {
        private const konst serialVersionUID = 0L
    }
}
