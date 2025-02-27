/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("unused", "RemoveExplicitTypeArguments")

package kotlin.script.experimental.api

import java.io.File
import java.io.Serializable
import kotlin.reflect.KClass
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.util.PropertiesCollection

interface ScriptCompilationConfigurationKeys

/**
 * The container for script compilation configuration
 * For usages see {@link KotlinScript} and actual code examples
 */
open class ScriptCompilationConfiguration(baseConfigurations: Iterable<ScriptCompilationConfiguration>, body: Builder.() -> Unit) :
    PropertiesCollection(Builder(baseConfigurations).apply(body).data) {

    constructor(body: Builder.() -> Unit = {}) : this(emptyList(), body)
    constructor(
        vararg baseConfigurations: ScriptCompilationConfiguration, body: Builder.() -> Unit = {}
    ) : this(baseConfigurations.asIterable(), body)

    class Builder internal constructor(baseConfigurations: Iterable<ScriptCompilationConfiguration>) :
        ScriptCompilationConfigurationKeys,
        PropertiesCollection.Builder(baseConfigurations)

    // inherited from script compilationConfiguration for using as a keys anchor
    companion object : ScriptCompilationConfigurationKeys

    object Default : ScriptCompilationConfiguration()

    override fun toString(): String {
        return "ScriptCompilationConfiguration($properties)"
    }
}


/**
 * An alternative to the constructor with base configuration, which returns a new configuration only if [body] adds anything
 * to the original one, otherwise returns original
 */
fun ScriptCompilationConfiguration?.with(body: ScriptCompilationConfiguration.Builder.() -> Unit): ScriptCompilationConfiguration {
    konst newConfiguration =
        if (this == null) ScriptCompilationConfiguration(body = body)
        else ScriptCompilationConfiguration(this, body = body)
    return if (newConfiguration == this) this else newConfiguration
}

/**
 * The script type display name
 */
konst ScriptCompilationConfigurationKeys.displayName by PropertiesCollection.key<String>()

/**
 * The default script class name
 */
konst ScriptCompilationConfigurationKeys.defaultIdentifier by PropertiesCollection.key<String>("Script")

/**
 * The script filename extension
 * Used for the primary script definition selection as well as to assign a kotlin-specific file type to the files with the extension in Intellij IDEA
 * For Intellij IDEA support, it is important to have this extension set to a non-ambiguous name.
 * See also {@link ScriptCompilationConfigurationKeys#filePathPattern} parameter for more fine-grained script definition selection
 */
konst ScriptCompilationConfigurationKeys.fileExtension by PropertiesCollection.key<String>("kts")

/**
 * Additional (to the filename extension) RegEx pattern with that the script file path is checked
 * It is used in the hosts that may have several script definitions registered and need to distinguish script file types not only by extension
 * The argument passed to the RegEx matcher is equikonstent to the File.path, taken relatively from a base path defined by the host
 * (usually should be the project root or the current directory)
 * See also {@link ScriptCompilationConfigurationKeys#fileExtension} parameter for the primary script definition selection
 */
konst ScriptCompilationConfigurationKeys.filePathPattern by PropertiesCollection.key<String>()

/**
 * The superclass for target script class
 */
konst ScriptCompilationConfigurationKeys.baseClass by PropertiesCollection.key<KotlinType>(KotlinType(Any::class)) // script base class

/**
 * The list of classes that will be used as implicit receivers in the script body, as if the whole body is wrapped with "with" calls:
 * <pre>
 * {@code
 *   with (receiver1) {
 *     ...
 *       with (receiverN) {
 *         // script body
 *       }
 *   }
 * }
 * </pre>
 *
 * Note: the actual receivers konstues should be passed to the constructor of the generated script class
 */
konst ScriptCompilationConfigurationKeys.implicitReceivers by PropertiesCollection.key<List<KotlinType>>() // in the order from outer to inner scope

/**
 * The map of names to the types
 */
konst ScriptCompilationConfigurationKeys.providedProperties by PropertiesCollection.key<Map<String, KotlinType>>() // external variables

/**
 * Variable name that holds a {@link File} instance pointing to the location of the script file
 */
konst ScriptCompilationConfigurationKeys.scriptFileLocationVariable by PropertiesCollection.key<String>()

/**
 * File pointing to the location of the script file. Note that in some cases it might not be possible
 * to determine script file location properly - in this case the file is an empty file
 */
konst ScriptCompilationConfigurationKeys.scriptFileLocation by PropertiesCollection.key<File>()

/**
 * The list of import expressions that will be implicitly applied to the script body, the syntax is the same as for the "import" statement
 */
konst ScriptCompilationConfigurationKeys.defaultImports by PropertiesCollection.key<List<String>>()

/**
 * The list of script sources that should be compiled along with the script and imported into it
 */
konst ScriptCompilationConfigurationKeys.importScripts by PropertiesCollection.key<List<SourceCode>>()

/**
 * The name of the generated script class field to assign the script results to, empty means disabled
 * see also ReplScriptCompilationConfigurationKeys.resultFieldPrefix
 */
konst ScriptCompilationConfigurationKeys.resultField by PropertiesCollection.key<String>("\$\$result")

/**
 * The list of script dependencies - platform specific
 */
konst ScriptCompilationConfigurationKeys.dependencies by PropertiesCollection.key<List<ScriptDependency>>()

/**
 * The list of compiler options that will be applied on script compilation, the syntax is the same as for CLI compiler
 */
konst ScriptCompilationConfigurationKeys.compilerOptions by PropertiesCollection.key<List<String>>() // Q: CommonCompilerOptions instead?

/**
 * The callback that will be called on the script compilation before parsing the script
 */
konst ScriptCompilationConfigurationKeys.refineConfigurationBeforeParsing by PropertiesCollection.key<List<RefineConfigurationUnconditionallyData>>(isTransient = true)

/**
 * The callback that will be called on the script compilation after parsing script file annotations
 */
konst ScriptCompilationConfigurationKeys.refineConfigurationOnAnnotations by PropertiesCollection.key<List<RefineConfigurationOnAnnotationsData>>(isTransient = true)

/**
 * The callback that will be called on the script compilation immediately before starting the compilation
 */
konst ScriptCompilationConfigurationKeys.refineConfigurationBeforeCompiling by PropertiesCollection.key<List<RefineConfigurationUnconditionallyData>>(isTransient = true)

/**
 * The list of script fragments that should be compiled instead of the whole text
 * (for use primary with the refinement callbacks)
 */
konst ScriptCompilationConfigurationKeys.sourceFragments by PropertiesCollection.key<List<ScriptSourceNamedFragment>>()

/**
 * Scripting host configuration
 */
konst ScriptCompilationConfigurationKeys.hostConfiguration by PropertiesCollection.key<ScriptingHostConfiguration>(isTransient = true)

/**
 * Should the script be always considered standalone
 * If true, it is ignored when compiled along with other sources (starting from 1.9, according to SkipStandaloneScriptsInSourceRoots language feature)
 * true by default
 */
konst ScriptCompilationConfigurationKeys.isStandalone by PropertiesCollection.key<Boolean>(true)

/**
 * The sub-builder DSL for configuring refinement callbacks
 */
konst ScriptCompilationConfiguration.Builder.refineConfiguration get() = RefineConfigurationBuilder()


class RefineConfigurationBuilder : PropertiesCollection.Builder() {

    /**
     * The callback that will be called on the script compilation before parsing the script
     * @param handler the callback that will be called
     */
    fun beforeParsing(handler: RefineScriptCompilationConfigurationHandler) {
        ScriptCompilationConfiguration.refineConfigurationBeforeParsing.append(RefineConfigurationUnconditionallyData(handler))
    }

    /**
     * The callback that will be called on the script compilation after parsing script file annotations
     * @param annotations the list of annotations to trigger the callback on
     * @param handler the callback that will be called
     */
    fun onAnnotations(annotations: List<KotlinType>, handler: RefineScriptCompilationConfigurationHandler) {
        // TODO: implement handlers composition
        ScriptCompilationConfiguration.refineConfigurationOnAnnotations.append(RefineConfigurationOnAnnotationsData(annotations, handler))
    }

    /**
     * The callback that will be called on the script compilation after parsing script file annotations
     * @param annotations the list of annotations to trigger the callback on
     * @param handler the callback that will be called
     */
    fun onAnnotations(vararg annotations: KotlinType, handler: RefineScriptCompilationConfigurationHandler) {
        onAnnotations(annotations.asList(), handler)
    }

    /**
     * The callback that will be called on the script compilation after parsing script file annotations
     * @param T the annotation to trigger the callback on
     * @param handler the callback that will be called
     */
    inline fun <reified T : Annotation> onAnnotations(noinline handler: RefineScriptCompilationConfigurationHandler) {
        onAnnotations(listOf(KotlinType(T::class)), handler)
    }

    /**
     * The callback that will be called on the script compilation after parsing script file annotations
     * @param annotations the list of annotations to trigger the callback on
     * @param handler the callback that will be called
     */
    fun onAnnotations(vararg annotations: KClass<out Annotation>, handler: RefineScriptCompilationConfigurationHandler) {
        onAnnotations(annotations.map { KotlinType(it) }, handler)
    }

    /**
     * The callback that will be called on the script compilation after parsing script file annotations
     * @param annotations the list of annotations to trigger the callback on
     * @param handler the callback that will be called
     */
    fun onAnnotations(annotations: Iterable<KClass<out Annotation>>, handler: RefineScriptCompilationConfigurationHandler) {
        onAnnotations(annotations.map { KotlinType(it) }, handler)
    }

    /**
     * The callback that will be called on the script compilation  immediately before starting the compilation
     * @param handler the callback that will be called
     */
    fun beforeCompiling(handler: RefineScriptCompilationConfigurationHandler) {
        ScriptCompilationConfiguration.refineConfigurationBeforeCompiling.append(RefineConfigurationUnconditionallyData(handler))
    }
}

/**
 * The refinement callback function signature
 */
typealias RefineScriptCompilationConfigurationHandler =
            (ScriptConfigurationRefinementContext) -> ResultWithDiagnostics<ScriptCompilationConfiguration>

/**
 * The refinement callback function signature for simple handlers (without diagnostics or errors)
 */
typealias SimpleRefineScriptCompilationConfigurationHandler =
            (ScriptConfigurationRefinementContext) -> ScriptCompilationConfiguration

data class RefineConfigurationUnconditionallyData(
    konst handler: RefineScriptCompilationConfigurationHandler
) : Serializable {
    companion object {
        private const konst serialVersionUID: Long = 1L
    }
}

data class RefineConfigurationOnAnnotationsData(
    konst annotations: List<KotlinType>,
    konst handler: RefineScriptCompilationConfigurationHandler
) : Serializable {
    companion object {
        private const konst serialVersionUID: Long = 1L
    }
}


fun ScriptCompilationConfiguration.refineBeforeParsing(
    script: SourceCode,
    collectedData: ScriptCollectedData? = null
): ResultWithDiagnostics<ScriptCompilationConfiguration> =
    simpleRefineImpl(ScriptCompilationConfiguration.refineConfigurationBeforeParsing) { config, refineData ->
        refineData.handler.invoke(ScriptConfigurationRefinementContext(script, config, collectedData))
    }

fun ScriptCompilationConfiguration.refineOnAnnotations(
    script: SourceCode,
    collectedData: ScriptCollectedData
): ResultWithDiagnostics<ScriptCompilationConfiguration> {
    konst foundAnnotationNames = collectedData[ScriptCollectedData.foundAnnotations]?.mapTo(HashSet()) { it.annotationClass.java.name }
    if (foundAnnotationNames.isNullOrEmpty()) return this.asSuccess()

    konst thisResult: ResultWithDiagnostics<ScriptCompilationConfiguration> = this.asSuccess()
    return this[ScriptCompilationConfiguration.refineConfigurationOnAnnotations]
        ?.fold(thisResult) { config, (annotations, handler) ->
            config.onSuccess {
                // checking that the collected data contains expected annotations
                if (annotations.none { foundAnnotationNames.contains(it.typeName) }) it.asSuccess()
                else handler.invoke(ScriptConfigurationRefinementContext(script, it, collectedData))
            }
        } ?: thisResult
}

fun ScriptCompilationConfiguration.refineBeforeCompiling(
    script: SourceCode,
    collectedData: ScriptCollectedData? = null
): ResultWithDiagnostics<ScriptCompilationConfiguration> =
    simpleRefineImpl(ScriptCompilationConfiguration.refineConfigurationBeforeCompiling) { config, refineData ->
        refineData.handler.invoke(ScriptConfigurationRefinementContext(script, config, collectedData))
    }

internal inline fun <Configuration: PropertiesCollection, RefineData> Configuration.simpleRefineImpl(
    key: PropertiesCollection.Key<List<RefineData>>,
    refineFn: (Configuration, RefineData) -> ResultWithDiagnostics<Configuration>
): ResultWithDiagnostics<Configuration> = (
        this[key]
            ?.fold(this) { config, refineData ->
                refineFn(config, refineData).konstueOr { return it }
            } ?: this
        ).asSuccess()

/**
 * The functional interface to the script compiler
 */
interface ScriptCompiler {

    /**
     * Compiles the [script] according to the [scriptCompilationConfiguration]
     * @param script the interface to the script source code
     * @param scriptCompilationConfiguration the script compilation configuration properties
     * @return result wrapper, if successful - with compiled script
     */
    suspend operator fun invoke(
        script: SourceCode,
        scriptCompilationConfiguration: ScriptCompilationConfiguration
    ): ResultWithDiagnostics<CompiledScript>
}

/**
 * The interface to the compiled script
 */
interface CompiledScript {

    /**
     * The location identifier for the script source, taken from SourceCode.locationId
     */
    konst sourceLocationId: String?
        get() = null

    /**
     * The compilation configuration used for script compilation
     */
    konst compilationConfiguration: ScriptCompilationConfiguration

    /**
     * The function that loads compiled script class
     * @param scriptEkonstuationConfiguration the script ekonstuation configuration properties
     * @return result wrapper, if successful - with loaded KClass
     */
    suspend fun getClass(scriptEkonstuationConfiguration: ScriptEkonstuationConfiguration?): ResultWithDiagnostics<KClass<*>>

    /**
     * The scripts compiled along with this one in one module, imported or otherwise included into compilation
     */
    konst otherScripts: List<CompiledScript>
        get() = emptyList()

    /**
     * The name and the type of the script's result field, if any
     */
    konst resultField: Pair<String, KotlinType>?
        get() = null
}
