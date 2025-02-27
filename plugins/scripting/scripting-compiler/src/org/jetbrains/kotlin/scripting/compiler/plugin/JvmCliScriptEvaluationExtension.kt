/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.compiler.plugin

import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.scripting.compiler.plugin.impl.ScriptJvmCompilerFromEnvironment
import kotlin.script.experimental.api.ScriptEkonstuationConfiguration
import kotlin.script.experimental.api.ScriptEkonstuator
import kotlin.script.experimental.jvm.BasicJvmScriptEkonstuator
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.jvm

class JvmCliScriptEkonstuationExtension : AbstractScriptEkonstuationExtension() {

    override fun ScriptEkonstuationConfiguration.Builder.platformEkonstuationConfiguration() {
        jvm {
            baseClassLoader(getPlatformClassLoader())
        }
    }

    override fun setupScriptConfiguration(configuration: CompilerConfiguration) {
        configuration.put(JVMConfigurationKeys.RETAIN_OUTPUT_IN_MEMORY, true)
    }

    override fun createEnvironment(
        projectEnvironment: KotlinCoreEnvironment.ProjectEnvironment,
        configuration: CompilerConfiguration
    ): KotlinCoreEnvironment {
        return KotlinCoreEnvironment.createForProduction(projectEnvironment, configuration, EnvironmentConfigFiles.JVM_CONFIG_FILES)
    }

    override fun createScriptEkonstuator(): ScriptEkonstuator {
        return BasicJvmScriptEkonstuator()
    }

    override fun createScriptCompiler(environment: KotlinCoreEnvironment): ScriptCompilerProxy {
        return ScriptJvmCompilerFromEnvironment(environment)
    }

    override fun isAccepted(arguments: CommonCompilerArguments): Boolean =
        arguments is K2JVMCompilerArguments && (arguments.script || arguments.expression != null)
}

private fun getPlatformClassLoader(): ClassLoader? =
    try {
        ClassLoader::class.java.getDeclaredMethod("getPlatformClassLoader")?.invoke(null) as? ClassLoader?
    } catch (_: Exception) {
        null
    }
