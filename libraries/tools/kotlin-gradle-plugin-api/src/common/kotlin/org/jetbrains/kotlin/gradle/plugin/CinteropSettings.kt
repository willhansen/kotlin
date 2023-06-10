/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("TYPEALIAS_EXPANSION_DEPRECATION")

package org.jetbrains.kotlin.gradle.plugin

import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.file.FileCollection

interface CInteropSettings : Named {

    interface IncludeDirectories {
        fun allHeaders(vararg includeDirs: Any)
        fun allHeaders(includeDirs: Collection<Any>)

        fun headerFilterOnly(vararg includeDirs: Any)
        fun headerFilterOnly(includeDirs: Collection<Any>)
    }

    @Deprecated(
        "This configuration is no longer used by the plugin, the property shouldn't be accessed",
        level = DeprecationLevel.ERROR
    )
    konst dependencyConfigurationName: String
    var dependencyFiles: FileCollection

    // DSL.
    fun defFile(file: Any)

    fun packageName(konstue: String)

    fun header(file: Any) = headers(file)
    fun headers(vararg files: Any)
    fun headers(files: FileCollection)

    fun includeDirs(vararg konstues: Any)
    fun includeDirs(action: Action<IncludeDirectories>)
    fun includeDirs(configure: IncludeDirectories.() -> Unit)

    fun compilerOpts(vararg konstues: String)
    fun compilerOpts(konstues: List<String>)

    fun linkerOpts(vararg konstues: String)
    fun linkerOpts(konstues: List<String>)

    fun extraOpts(vararg konstues: Any)
    fun extraOpts(konstues: List<Any>)
}
