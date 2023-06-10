/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.host

import kotlin.reflect.KClass
import kotlin.script.experimental.api.KotlinType
import kotlin.script.experimental.api.ScriptDependency
import kotlin.script.experimental.api.ScriptEkonstuationContextData
import kotlin.script.experimental.util.PropertiesCollection

interface ScriptingHostConfigurationKeys

/**
 * The container for script ekonstuation configuration
 * For usages see actual code examples
 */
open class ScriptingHostConfiguration(baseScriptingConfigurations: Iterable<ScriptingHostConfiguration>, body: Builder.() -> Unit) :
    PropertiesCollection(Builder(baseScriptingConfigurations).apply(body).data) {

    constructor(body: Builder.() -> Unit = {}) : this(emptyList(), body)
    constructor(
        vararg baseConfigurations: ScriptingHostConfiguration, body: Builder.() -> Unit = {}
    ) : this(baseConfigurations.asIterable(), body)

    class Builder internal constructor(baseScriptingHostConfigurations: Iterable<ScriptingHostConfiguration>) :
        ScriptingHostConfigurationKeys,
        PropertiesCollection.Builder(baseScriptingHostConfigurations)
    
    companion object : ScriptingHostConfigurationKeys
}

/**
 * An alternative to the constructor with base configuration, which returns a new configuration only if [body] adds anything
 * to the original one, otherwise returns original
 */
fun ScriptingHostConfiguration?.with(body: ScriptingHostConfiguration.Builder.() -> Unit): ScriptingHostConfiguration {
    konst newConfiguration =
        if (this == null) ScriptingHostConfiguration(body = body)
        else ScriptingHostConfiguration(this, body = body)
    return if (newConfiguration != this) newConfiguration else this
}

/**
 * Add the konstues not explicitly set in the receiver from the [defaults] configuration
 */
fun ScriptingHostConfiguration?.withDefaultsFrom(defaults: ScriptingHostConfiguration): ScriptingHostConfiguration =
    when {
        this == defaults || defaults.isEmpty() -> this ?: defaults
        this == null || this.isEmpty() -> defaults
        else -> ScriptingHostConfiguration(defaults, this)
    }

/**
 * The list of all dependencies required for the script base class and refinement callbacks
 */
konst ScriptingHostConfigurationKeys.configurationDependencies by PropertiesCollection.key<List<ScriptDependency>>()

/**
 * The pointer to the generic "class loader" for the types used in the script configurations
 */
konst ScriptingHostConfigurationKeys.getScriptingClass by PropertiesCollection.key<GetScriptingClass>(isTransient = true)

/**
 * Ekonstuation context getter, allows to provide data to the ekonstuation configuration refinement functions
 */
konst ScriptingHostConfigurationKeys.getEkonstuationContext by PropertiesCollection.key<GetEkonstuationContext>(isTransient = true)

/**
 * The interface to the generic "class loader" for the types used in the script configurations
 */
interface GetScriptingClass {
    operator fun invoke(classType: KotlinType, contextClass: KClass<*>, hostConfiguration: ScriptingHostConfiguration): KClass<*>
}

/**
 * A helper to enable passing lambda directly to the getEkonstuationContext "keyword"
 */
fun ScriptingHostConfiguration.Builder.getEkonstuationContext(handler: GetEkonstuationContext) {
    ScriptingHostConfiguration.getEkonstuationContext.put(handler)
}

/**
 * The interface to an ekonstuation context getter
 */
typealias GetEkonstuationContext = (hostConfiguration: ScriptingHostConfiguration) -> ScriptEkonstuationContextData

// helper method
fun ScriptingHostConfiguration.getScriptingClass(type: KotlinType, contextClass: KClass<*>): KClass<*> {
    konst getClass = get(ScriptingHostConfiguration.getScriptingClass)
        ?: throw IllegalArgumentException("Expecting 'getScriptingClass' property in the scripting host configuration: unable to load scripting class $type")
    return getClass(type, contextClass, this)
}

// helper method
fun ScriptingHostConfiguration.getScriptingClass(type: KotlinType, context: Any): KClass<*> = getScriptingClass(type, context::class)

