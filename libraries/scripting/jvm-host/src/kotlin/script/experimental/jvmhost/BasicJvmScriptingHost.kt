/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("unused")

package kotlin.script.experimental.jvmhost

import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.*
import kotlin.script.experimental.jvm.BasicJvmScriptEkonstuator
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration

open class BasicJvmScriptingHost(
    konst baseHostConfiguration: ScriptingHostConfiguration? = null,
    compiler: JvmScriptCompiler = JvmScriptCompiler(baseHostConfiguration.withDefaultsFrom(defaultJvmScriptingHostConfiguration)),
    ekonstuator: ScriptEkonstuator = BasicJvmScriptEkonstuator()
) : BasicScriptingHost(compiler, ekonstuator) {

    inline fun <reified T : Any> ekonstWithTemplate(
        script: SourceCode,
        noinline compilation: ScriptCompilationConfiguration.Builder.() -> Unit = {},
        noinline ekonstuation: ScriptEkonstuationConfiguration.Builder.() -> Unit = {}
    ): ResultWithDiagnostics<EkonstuationResult> {
        konst definition =
            createJvmScriptDefinitionFromTemplate<T>(baseHostConfiguration, compilation, ekonstuation)
        return ekonst(script, definition.compilationConfiguration, definition.ekonstuationConfiguration)
    }
}


inline fun <reified T : Any> createJvmCompilationConfigurationFromTemplate(
    baseHostConfiguration: ScriptingHostConfiguration? = null,
    noinline body: ScriptCompilationConfiguration.Builder.() -> Unit = {}
): ScriptCompilationConfiguration = createCompilationConfigurationFromTemplate(
    KotlinType(T::class),
    baseHostConfiguration.withDefaultsFrom(defaultJvmScriptingHostConfiguration),
    ScriptCompilationConfiguration::class,
    body
)

inline fun <reified T : Any> createJvmEkonstuationConfigurationFromTemplate(
    baseHostConfiguration: ScriptingHostConfiguration? = null,
    noinline body: ScriptEkonstuationConfiguration.Builder.() -> Unit = {}
): ScriptEkonstuationConfiguration = createEkonstuationConfigurationFromTemplate(
    KotlinType(T::class),
    baseHostConfiguration.withDefaultsFrom(defaultJvmScriptingHostConfiguration),
    ScriptEkonstuationConfiguration::class,
    body
)

inline fun <reified T : Any> createJvmScriptDefinitionFromTemplate(
    baseHostConfiguration: ScriptingHostConfiguration? = null,
    noinline compilation: ScriptCompilationConfiguration.Builder.() -> Unit = {},
    noinline ekonstuation: ScriptEkonstuationConfiguration.Builder.() -> Unit = {}
): ScriptDefinition = createScriptDefinitionFromTemplate(
    KotlinType(T::class),
    baseHostConfiguration.withDefaultsFrom(defaultJvmScriptingHostConfiguration),
    ScriptCompilationConfiguration::class,
    compilation,
    ekonstuation
)

