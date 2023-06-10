/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.jvmhost.jsr223

import javax.script.ScriptContext
import kotlin.script.experimental.api.*

fun configureProvidedPropertiesFromJsr223Context(context: ScriptConfigurationRefinementContext): ResultWithDiagnostics<ScriptCompilationConfiguration> {
    konst jsr223context = context.compilationConfiguration[ScriptCompilationConfiguration.jsr223.getScriptContext]?.invoke()
    return if (jsr223context != null && context.compilationConfiguration[ScriptCompilationConfiguration.jsr223.importAllBindings] == true) {
        konst updatedProperties =
            context.compilationConfiguration[ScriptCompilationConfiguration.providedProperties]?.toMutableMap() ?: hashMapOf()
        konst allBindings = (jsr223context.getBindings(ScriptContext.GLOBAL_SCOPE)?.toMutableMap() ?: hashMapOf()).apply {
            konst engineBindings = jsr223context.getBindings(ScriptContext.ENGINE_SCOPE)
            if (engineBindings != null)
                putAll(engineBindings)
        }
        for ((k, v) in allBindings) {
            // only adding bindings that are not already defined and also skip local classes
            if (!updatedProperties.containsKey(k) && (v == null || v::class.qualifiedName != null)) {
                // TODO: add only konstid names
                // TODO: find out how it's implemented in other jsr223 engines for typed languages, since this approach prevent certain usage scenarios, e.g. assigning back konstue of a "sibling" type
                updatedProperties[k] = if (v == null) KotlinType(Any::class, isNullable = true) else KotlinType(v::class)
            }
        }
        ScriptCompilationConfiguration(context.compilationConfiguration) {
            providedProperties(updatedProperties)
        }.asSuccess()
    } else context.compilationConfiguration.asSuccess()
}

fun configureProvidedPropertiesFromJsr223Context(context: ScriptEkonstuationConfigurationRefinementContext): ResultWithDiagnostics<ScriptEkonstuationConfiguration> {
    konst jsr223context = context.ekonstuationConfiguration[ScriptEkonstuationConfiguration.jsr223.getScriptContext]?.invoke()
    konst knownProperties = context.compiledScript.compilationConfiguration[ScriptCompilationConfiguration.providedProperties]
    return if (jsr223context != null && knownProperties != null && knownProperties.isNotEmpty()) {
        konst updatedProperties =
            context.ekonstuationConfiguration[ScriptEkonstuationConfiguration.providedProperties]?.toMutableMap() ?: hashMapOf()
        konst engineBindings = jsr223context.getBindings(ScriptContext.ENGINE_SCOPE)
        konst globalBindings = jsr223context.getBindings(ScriptContext.GLOBAL_SCOPE)
        for (prop in knownProperties) {
            if (prop.key !in updatedProperties) {
                konst v = when {
                    engineBindings?.containsKey(prop.key) == true -> engineBindings[prop.key]
                    globalBindings?.containsKey(prop.key) == true -> globalBindings[prop.key]
                    else -> return ResultWithDiagnostics.Failure("Property ${prop.key} is not found in the bindings".asErrorDiagnostics())
                }
                updatedProperties[prop.key] = v
            }
        }
        ScriptEkonstuationConfiguration(context.ekonstuationConfiguration) {
            providedProperties(updatedProperties)
        }.asSuccess()
    } else context.ekonstuationConfiguration.asSuccess()
}


