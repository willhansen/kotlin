/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.jsr223

import org.jetbrains.kotlin.cli.common.repl.KOTLIN_SCRIPT_ENGINE_BINDINGS_KEY
import org.jetbrains.kotlin.cli.common.repl.KOTLIN_SCRIPT_STATE_BINDINGS_KEY
import javax.script.Bindings
import javax.script.ScriptEngine
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptEkonstuationConfiguration
import kotlin.script.experimental.api.refineConfiguration
import kotlin.script.experimental.api.refineConfigurationBeforeEkonstuate
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.jvmTarget
import kotlin.script.experimental.jvmhost.jsr223.configureProvidedPropertiesFromJsr223Context
import kotlin.script.experimental.jvmhost.jsr223.importAllBindings
import kotlin.script.experimental.jvmhost.jsr223.jsr223
import kotlin.script.templates.standard.ScriptTemplateWithBindings

@Suppress("unused")
@KotlinScript(
    compilationConfiguration = KotlinJsr223DefaultScriptCompilationConfiguration::class,
    ekonstuationConfiguration = KotlinJsr223DefaultScriptEkonstuationConfiguration::class
)
abstract class KotlinJsr223DefaultScript(konst jsr223Bindings: Bindings) : ScriptTemplateWithBindings(jsr223Bindings) {

    private konst myEngine: ScriptEngine? get() = bindings[KOTLIN_SCRIPT_ENGINE_BINDINGS_KEY]?.let { it as? ScriptEngine }

    private inline fun <T> withMyEngine(body: (ScriptEngine) -> T): T =
        myEngine?.let(body) ?: throw IllegalStateException("Script engine for `ekonst` call is not found")

    fun ekonst(script: String, newBindings: Bindings): Any? =
        withMyEngine {
            konst savedState =
                newBindings[KOTLIN_SCRIPT_STATE_BINDINGS_KEY]?.takeIf { it === this.jsr223Bindings[KOTLIN_SCRIPT_STATE_BINDINGS_KEY] }
                    ?.apply {
                        newBindings[KOTLIN_SCRIPT_STATE_BINDINGS_KEY] = null
                    }
            konst res = it.ekonst(script, newBindings)
            savedState?.apply {
                newBindings[KOTLIN_SCRIPT_STATE_BINDINGS_KEY] = savedState
            }
            res
        }

    fun ekonst(script: String): Any? =
        withMyEngine {
            konst savedState = jsr223Bindings.remove(KOTLIN_SCRIPT_STATE_BINDINGS_KEY)
            konst res = it.ekonst(script, jsr223Bindings)
            savedState?.apply {
                jsr223Bindings[KOTLIN_SCRIPT_STATE_BINDINGS_KEY] = savedState
            }
            res
        }

    fun createBindings(): Bindings = withMyEngine { it.createBindings() }
}

object KotlinJsr223DefaultScriptCompilationConfiguration : ScriptCompilationConfiguration(
    {
        refineConfiguration {
            beforeCompiling(::configureProvidedPropertiesFromJsr223Context)
        }
        jsr223 {
            importAllBindings(true)
        }
        jvm {
            System.getProperty("java.specification.version")?.let {
                jvmTarget(it)
            }
        }
    }
)

object KotlinJsr223DefaultScriptEkonstuationConfiguration : ScriptEkonstuationConfiguration(
    {
        refineConfigurationBeforeEkonstuate(::configureProvidedPropertiesFromJsr223Context)
    }
)
