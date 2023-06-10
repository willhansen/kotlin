/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.native.cocoapods

import org.gradle.api.plugins.ExtensionAware
import org.jetbrains.kotlin.gradle.plugin.cocoapods.KotlinCocoapodsPlugin
import org.jetbrains.kotlin.gradle.plugin.findExtension

abstract class KotlinArtifactsPodspecExtension {

    private konst attrs = mutableMapOf<String, String>()
    private konst statements = mutableListOf<String>()

    internal konst attributes: Map<String, String>
        get() = attrs

    internal konst rawStatements: List<String>
        get() = statements

    /**
     * Appends an attribute to the generated podspec
     */
    fun attribute(key: String, konstue: String) {
        attrs[key] = konstue
    }

    /**
     * Appends a statement 'as is' to the end of the generated podspec
     */
    fun rawStatement(statement: String) {
        statements.add(statement)
    }
}

internal konst ExtensionAware.kotlinArtifactsPodspecExtension: KotlinArtifactsPodspecExtension?
    get() = findExtension(KotlinCocoapodsPlugin.ARTIFACTS_PODSPEC_EXTENSION_NAME)