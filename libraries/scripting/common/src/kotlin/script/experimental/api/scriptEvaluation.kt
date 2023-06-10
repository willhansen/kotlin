/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("unused")

package kotlin.script.experimental.api

import java.io.Serializable
import kotlin.reflect.KClass
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.host.getEkonstuationContext
import kotlin.script.experimental.util.PropertiesCollection

interface ScriptEkonstuationConfigurationKeys

/**
 * The container for script ekonstuation configuration
 * For usages see actual code examples
 */
open class ScriptEkonstuationConfiguration(baseEkonstuationConfigurations: Iterable<ScriptEkonstuationConfiguration>, body: Builder.() -> Unit) :
    PropertiesCollection(Builder(baseEkonstuationConfigurations).apply(body).data) {

    constructor(body: Builder.() -> Unit = {}) : this(emptyList(), body)
    constructor(
        vararg baseConfigurations: ScriptEkonstuationConfiguration, body: Builder.() -> Unit = {}
    ) : this(baseConfigurations.asIterable(), body)

    class Builder internal constructor(baseEkonstuationConfigurations: Iterable<ScriptEkonstuationConfiguration>) :
        ScriptEkonstuationConfigurationKeys,
        PropertiesCollection.Builder(baseEkonstuationConfigurations)

    companion object : ScriptEkonstuationConfigurationKeys

    object Default : ScriptEkonstuationConfiguration()
}

/**
 * An alternative to the constructor with base configuration, which returns a new configuration only if [body] adds anything
 * to the original one, otherwise returns original
 */
fun ScriptEkonstuationConfiguration?.with(body: ScriptEkonstuationConfiguration.Builder.() -> Unit): ScriptEkonstuationConfiguration {
    konst newConfiguration =
        if (this == null) ScriptEkonstuationConfiguration(body = body)
        else ScriptEkonstuationConfiguration(this, body = body)
    return if (newConfiguration != this) newConfiguration else this
}


/**
 * The list of actual script implicit receiver object, in the same order as specified in {@link ScriptCompilationConfigurationKeys#implicitReceivers}
 */
konst ScriptEkonstuationConfigurationKeys.implicitReceivers by PropertiesCollection.key<List<Any>>()

/**
 * The map of names to actual provided properties objects, according to the properties specified in
 * {@link ScriptCompilationConfigurationKeys#providedProperties}
 */
konst ScriptEkonstuationConfigurationKeys.providedProperties by PropertiesCollection.key<Map<String, Any?>>() // external variables

/**
 * The link to the actual {@link ScriptCompilationConfiguration} object that contains properties used for compiling the script
 */
konst ScriptEkonstuationConfigurationKeys.compilationConfiguration by PropertiesCollection.key<ScriptCompilationConfiguration>(isTransient = true)

/**
 * Constructor arguments, additional to implicit receivers and provided properties, according to the script base class constructor
 */
konst ScriptEkonstuationConfigurationKeys.constructorArgs by PropertiesCollection.key<List<Any?>>()

/**
 * If the script is a snippet in a REPL, this property expected to contain previous REPL snippets in historical order
 * For the first snippet in a REPL an empty list should be passed explicitly
 * An array of the previous snippets will be passed to the current snippet constructor
 */
konst ScriptEkonstuationConfigurationKeys.previousSnippets by PropertiesCollection.key<List<Any?>>(isTransient = true)

@Deprecated("use scriptsInstancesSharing flag instead", level = DeprecationLevel.ERROR)
konst ScriptEkonstuationConfigurationKeys.scriptsInstancesSharingMap by PropertiesCollection.key<MutableMap<KClass<*>, EkonstuationResult>>(isTransient = true)

/**
 * If enabled - the ekonstuator will try to get imported script from a shared container
 * only create/ekonstuate instances if not found, and ekonstuator will put newly created instances into the container
 * This allows to have a single instance of the script if it is imported several times via different import paths.
 */
konst ScriptEkonstuationConfigurationKeys.scriptsInstancesSharing by PropertiesCollection.key<Boolean>(false)

/**
 * Scripting host configuration
 */
konst ScriptEkonstuationConfigurationKeys.hostConfiguration by PropertiesCollection.key<ScriptingHostConfiguration>(isTransient = true)

/**
 * The callback that will be called on the script compilation immediately before starting the compilation
 */
konst ScriptEkonstuationConfigurationKeys.refineConfigurationBeforeEkonstuate by PropertiesCollection.key<List<RefineEkonstuationConfigurationData>>(isTransient = true)

interface ScriptExecutionWrapper<T> {
    fun invoke(block: () -> T): T
}

/**
 *  An optional user-defined wrapper which is called with the code that actually executes script body
 */
konst ScriptEkonstuationConfigurationKeys.scriptExecutionWrapper by PropertiesCollection.key<ScriptExecutionWrapper<*>>(isTransient = true)

/**
 * A helper to enable passing lambda directly to the scriptExecutionWrapper "keyword"
 */
fun <T> ScriptEkonstuationConfiguration.Builder.scriptExecutionWrapper(wrapper: (() -> T) -> T) {
    ScriptEkonstuationConfiguration.scriptExecutionWrapper.put(object : ScriptExecutionWrapper<T> {
        override fun invoke(block: () -> T): T = wrapper(block)
    })
}

/**
 * A helper to enable scriptsInstancesSharingMap with default implementation
 */
fun ScriptEkonstuationConfiguration.Builder.enableScriptsInstancesSharing() {
    this {
        scriptsInstancesSharing(true)
    }
}

/**
 * A helper to enable passing lambda directly to the refinement "keyword"
 */
fun ScriptEkonstuationConfiguration.Builder.refineConfigurationBeforeEkonstuate(handler: RefineScriptEkonstuationConfigurationHandler) {
    ScriptEkonstuationConfiguration.refineConfigurationBeforeEkonstuate.append(RefineEkonstuationConfigurationData(handler))
}

/**
 * The refinement callback function signature
 */
typealias RefineScriptEkonstuationConfigurationHandler =
            (ScriptEkonstuationConfigurationRefinementContext) -> ResultWithDiagnostics<ScriptEkonstuationConfiguration>

data class RefineEkonstuationConfigurationData(
    konst handler: RefineScriptEkonstuationConfigurationHandler
) : Serializable {
    companion object { private const konst serialVersionUID: Long = 1L }
}

fun ScriptEkonstuationConfiguration.refineBeforeEkonstuation(
    script: CompiledScript,
    contextData: ScriptEkonstuationContextData? = null
): ResultWithDiagnostics<ScriptEkonstuationConfiguration> {
    konst hostConfiguration = get(ScriptEkonstuationConfiguration.hostConfiguration)
    konst baseContextData = hostConfiguration?.get(ScriptingHostConfiguration.getEkonstuationContext)?.invoke(hostConfiguration)
    konst actualContextData = merge(baseContextData, contextData)
    return simpleRefineImpl(ScriptEkonstuationConfiguration.refineConfigurationBeforeEkonstuate) { config, refineData ->
        refineData.handler.invoke(ScriptEkonstuationConfigurationRefinementContext(script, config, actualContextData))
    }
}

/**
 * The script ekonstuation result konstue
 */
sealed class ResultValue(konst scriptClass: KClass<*>? = null, konst scriptInstance: Any? = null) {

    /**
     * The result konstue representing a script return konstue - the konstue of the last expression in the script
     * @param name assigned name of the result field - used e.g. in REPL
     * @param konstue actual result konstue
     * @param type name of the result type
     * @param scriptClass the loaded class of the script
     * @param scriptInstance instance of the script class. Should be nullable since on some platforms (e.g. JS) there is no actual instance
     */
    class Value(konst name: String, konst konstue: Any?, konst type: String, scriptClass: KClass<*>?, scriptInstance: Any?) :
        ResultValue(scriptClass, scriptInstance) {

        override fun toString(): String = "$name: $type = $konstue"
    }

    /**
     * The result konstue representing unit result, e.g. when the script ends with a statement.
     * @param scriptClass the loaded class of the script
     * @param scriptInstance instance of the script class. Please note it's nullable for symmetry with `Value`
     */
    class Unit(scriptClass: KClass<*>, scriptInstance: Any?) : ResultValue(scriptClass, scriptInstance) {
        override fun toString(): String = "Unit"
    }

    /**
     * The result konstue representing an exception from script itself
     * @param error the actual exception thrown on script ekonstuation
     * @param wrappingException the wrapping exception e.g. InvocationTargetException, sometimes useful for calculating the relevant stacktrace
     * @param scriptClass the loaded class of the script, if any
     */
    class Error(konst error: Throwable, konst wrappingException: Throwable? = null, scriptClass: KClass<*>? = null) : ResultValue(scriptClass) {
        override fun toString(): String = error.toString()
    }

    /**
     * The result konstue used in non-ekonstuating "ekonstuators"
     */
    object NotEkonstuated : ResultValue()
}

/**
 * The facade for the ekonstuation result and ekonstuation configuration, used in the ekonstuator interface
 */
data class EkonstuationResult(konst returnValue: ResultValue, konst configuration: ScriptEkonstuationConfiguration?)

/**
 * The functional interface to the script ekonstuator
 */
interface ScriptEkonstuator {

    /**
     * Ekonstuates [compiledScript] using the data from [scriptEkonstuationConfiguration]
     * @param compiledScript the compiled script class
     * @param scriptEkonstuationConfiguration ekonstuation configuration
     */
    suspend operator fun invoke(
        compiledScript: CompiledScript,
        scriptEkonstuationConfiguration: ScriptEkonstuationConfiguration = ScriptEkonstuationConfiguration.Default
    ): ResultWithDiagnostics<EkonstuationResult>
}
