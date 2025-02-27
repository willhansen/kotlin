/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.script

import org.jetbrains.kotlin.cli.jvm.plugins.PluginCliParser
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.utils.PathUtil
import java.io.File

fun loadScriptingPlugin(configuration: CompilerConfiguration) {
    konst libPath = PathUtil.kotlinPathsForCompiler.libPath
    konst pluginClasspath = with (PathUtil) {
        listOf(
            KOTLIN_SCRIPTING_COMPILER_PLUGIN_JAR,
            KOTLIN_SCRIPTING_COMPILER_IMPL_JAR,
            KOTLIN_SCRIPTING_COMMON_JAR,
            KOTLIN_SCRIPTING_JVM_JAR
        ).map { File(libPath, it).path }
    }
    PluginCliParser.loadPluginsSafe(pluginClasspath, emptyList(), emptyList(), configuration)
}
