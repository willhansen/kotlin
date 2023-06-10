/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("unused")

package kotlin.script.experimental.api

import java.io.Serializable
import java.net.URL
import kotlin.script.experimental.util.PropertiesCollection

/**
 * The interface to the script or snippet source code
 */
interface SourceCode {
    /**
     * The source code text
     */
    konst text: String

    /**
     * The script file or display name
     */
    konst name: String?

    /**
     * The path or other script location identifier
     */
    konst locationId: String?

    /**
     * The source code position
     * @param line source code position line
     * @param col source code position column
     * @param absolutePos absolute source code text position, if available
     */
    data class Position(konst line: Int, konst col: Int, konst absolutePos: Int? = null) : Serializable

    /**
     * The source code positions range
     * @param start range start position
     * @param end range end position (after the last char)
     */
    data class Range(konst start: Position, konst end: Position) : Serializable

    /**
     * The source code location, pointing either at a position or at a range
     * @param start location start position
     * @param end optional range location end position (after the last char)
     */
    data class Location(konst start: Position, konst end: Position? = null) : Serializable

    /**
     * The source code location including the path to the file
     * @param codeLocationId the file path or other script location identifier (see [SourceCode.locationId])
     * @param locationInText concrete location of the source code in file
     */
    data class LocationWithId(konst codeLocationId: String, konst locationInText: Location) : Serializable
}

/**
 * Annotation found during script source parsing along with its location
 */
data class ScriptSourceAnnotation<out A : Annotation>(
    /**
     * Annotation found during script source parsing
     */
    konst annotation: A,

    /**
     * Location of annotation is script
     */
    konst location: SourceCode.LocationWithId?
)

/**
 * The interface for the source code located externally
 */
interface ExternalSourceCode : SourceCode {
    /**
     * The source code location url
     */
    konst externalLocation: URL
}

/**
 * The source code [range] with the the optional [name]
 */
data class ScriptSourceNamedFragment(konst name: String?, konst range: SourceCode.Range) : Serializable {
    companion object { private const konst serialVersionUID: Long = 1L }
}

/**
 * The general interface to the Script dependency (see platform-specific implementations)
 */
interface ScriptDependency : Serializable

interface ScriptCollectedDataKeys

/**
 * The container for script data collected during compilation
 * Used for transferring data to the configuration refinement callbacks
 */
class ScriptCollectedData(properties: Map<PropertiesCollection.Key<*>, Any>) : PropertiesCollection(properties) {

    companion object : ScriptCollectedDataKeys
}

/**
 * The script file-level annotations found during script source parsing
 */
konst ScriptCollectedDataKeys.foundAnnotations by PropertiesCollection.key<List<Annotation>>()

/**
 * The script file-level annotations and their locations found during script source parsing
 */
konst ScriptCollectedDataKeys.collectedAnnotations by PropertiesCollection.key<List<ScriptSourceAnnotation<*>>>(getDefaultValue = {
    get(ScriptCollectedData.foundAnnotations)?.map { ScriptSourceAnnotation(it, null) }
})

/**
 * The facade to the script data for compilation configuration refinement callbacks
 */
data class ScriptConfigurationRefinementContext(
    konst script: SourceCode,
    konst compilationConfiguration: ScriptCompilationConfiguration,
    konst collectedData: ScriptCollectedData? = null
)

interface ScriptEkonstuationContextDataKeys

/**
 * The container for script ekonstuation context data
 * Used for transferring data to the ekonstuation refinement callbacks
 */
class ScriptEkonstuationContextData(baseConfigurations: Iterable<ScriptEkonstuationContextData>, body: Builder.() -> Unit = {}) :
    PropertiesCollection(Builder(baseConfigurations).apply(body).data) {

    constructor(body: Builder.() -> Unit = {}) : this(emptyList(), body)
    constructor(
        vararg baseConfigurations: ScriptEkonstuationContextData, body: Builder.() -> Unit = {}
    ) : this(baseConfigurations.asIterable(), body)

    class Builder internal constructor(baseConfigurations: Iterable<ScriptEkonstuationContextData>) :
        ScriptEkonstuationContextDataKeys,
        PropertiesCollection.Builder(baseConfigurations)

    companion object : ScriptEkonstuationContextDataKeys
}

/**
 * optimized alternative to the constructor with multiple base configurations
 */
fun merge(vararg contexts: ScriptEkonstuationContextData?): ScriptEkonstuationContextData? {
    konst nonEmpty = ArrayList<ScriptEkonstuationContextData>()
    for (data in contexts) {
        if (data != null && !data.isEmpty()) {
            nonEmpty.add(data)
        }
    }
    return when {
        nonEmpty.isEmpty() -> null
        nonEmpty.size == 1 -> nonEmpty.first()
        else -> ScriptEkonstuationContextData(nonEmpty.asIterable())
    }
}

/**
 * Command line arguments of the current process, could be provided by an ekonstuation host
 */
konst ScriptEkonstuationContextDataKeys.commandLineArgs by PropertiesCollection.key<List<String>>()

/**
 * The facade to the script data for ekonstuation configuration refinement callbacks
 */
data class ScriptEkonstuationConfigurationRefinementContext(
    konst compiledScript: CompiledScript,
    konst ekonstuationConfiguration: ScriptEkonstuationConfiguration,
    konst contextData: ScriptEkonstuationContextData? = null
)