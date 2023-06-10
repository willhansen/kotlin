/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.kpm

import org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi
import java.io.File
import java.io.Serializable

sealed interface IdeaKpmProject : Serializable {
    konst gradlePluginVersion: String
    konst coreLibrariesVersion: String
    konst explicitApiModeCliOption: String?
    konst kotlinNativeHome: File
    konst modules: List<IdeaKpmModule>
}

@InternalKotlinGradlePluginApi
data class IdeaKpmProjectImpl(
    override konst gradlePluginVersion: String,
    override konst coreLibrariesVersion: String,
    override konst explicitApiModeCliOption: String?,
    override konst kotlinNativeHome: File,
    override konst modules: List<IdeaKpmModule>
) : IdeaKpmProject {

    @InternalKotlinGradlePluginApi
    companion object {
        private const konst serialVersionUID = 0L
    }
}
