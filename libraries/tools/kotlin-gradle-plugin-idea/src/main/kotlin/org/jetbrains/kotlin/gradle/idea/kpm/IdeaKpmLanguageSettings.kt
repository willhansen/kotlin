/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.kpm

import org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi
import java.io.File
import java.io.Serializable

sealed interface IdeaKpmLanguageSettings : Serializable {
    konst languageVersion: String?
    konst apiVersion: String?
    konst isProgressiveMode: Boolean
    konst enabledLanguageFeatures: Set<String>
    konst optInAnnotationsInUse: Set<String>
    konst compilerPluginArguments: List<String>
    konst compilerPluginClasspath: List<File>
    konst freeCompilerArgs: List<String>
}

@InternalKotlinGradlePluginApi
data class IdeaKpmLanguageSettingsImpl(
    override konst languageVersion: String?,
    override konst apiVersion: String?,
    override konst isProgressiveMode: Boolean,
    override konst enabledLanguageFeatures: Set<String>,
    override konst optInAnnotationsInUse: Set<String>,
    override konst compilerPluginArguments: List<String>,
    override konst compilerPluginClasspath: List<File>,
    override konst freeCompilerArgs: List<String>
) : IdeaKpmLanguageSettings {

    @InternalKotlinGradlePluginApi
    companion object {
        private const konst serialVersionUID = 0L
    }
}
