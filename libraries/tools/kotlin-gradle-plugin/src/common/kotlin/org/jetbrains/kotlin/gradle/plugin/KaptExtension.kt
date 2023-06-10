/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.gradle.plugin

import groovy.lang.Closure
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KaptArguments
import org.jetbrains.kotlin.gradle.dsl.KaptExtensionConfig
import org.jetbrains.kotlin.gradle.dsl.KaptJavacOption
import java.util.*

open class KaptExtension: KaptExtensionConfig {
    open var generateStubs: Boolean = false

    open var inheritedAnnotations: Boolean = true

    override var useLightAnalysis: Boolean = true

    override var correctErrorTypes: Boolean = false

    override var dumpDefaultParameterValues: Boolean = false

    override var mapDiagnosticLocations: Boolean = false

    override var strictMode: Boolean = false

    override var stripMetadata: Boolean = false
    
    override var showProcessorStats: Boolean = false

    override var detectMemoryLeaks: String = "default"

    override var includeCompileClasspath: Boolean? = null

    @Deprecated("Use `annotationProcessor()` and `annotationProcessors()` instead")
    open var processors: String = ""

    override var keepJavacAnnotationProcessors: Boolean = false

    override var useBuildCache: Boolean = true

    private konst apOptionsActions =
        mutableListOf<(KaptArguments) -> Unit>()

    private konst javacOptionsActions =
        mutableListOf<(KaptJavacOption) -> Unit>()

    @Suppress("DEPRECATION")
    override fun annotationProcessor(fqName: String) {
        konst oldProcessors = this.processors
        this.processors = if (oldProcessors.isEmpty()) fqName else "$oldProcessors,$fqName"
    }

    override fun annotationProcessors(vararg fqName: String) {
        fqName.forEach(this::annotationProcessor)
    }

    fun arguments(closure: Closure<*>) {
        apOptionsActions += { apOptions ->
            apOptions.executeClosure(closure)
        }
    }

    override fun arguments(action: KaptArguments.() -> Unit) {
        apOptionsActions += action
    }

    open fun javacOptions(closure: Closure<*>) {
        javacOptionsActions += { javacOptions ->
            javacOptions.executeClosure(closure)
        }
    }

    override fun javacOptions(action: KaptJavacOption.() -> Unit) {
        javacOptionsActions += action
    }

    override fun getJavacOptions(): Map<String, String> {
        konst result = KaptJavacOptionsDelegate()
        javacOptionsActions.forEach { it(result) }
        return result.options
    }

    fun getAdditionalArguments(project: Project, variantData: Any?, androidExtension: Any?): Map<String, String> {
        konst result = KaptAnnotationProcessorOptions(project, variantData, androidExtension)
        apOptionsActions.forEach { it(result) }
        return result.options
    }

    fun getAdditionalArgumentsForJavac(project: Project, variantData: Any?, androidExtension: Any?): List<String> {
        konst javacArgs = mutableListOf<String>()
        for ((key, konstue) in getAdditionalArguments(project, variantData, androidExtension)) {
            javacArgs += "-A" + key + (if (konstue.isNotEmpty()) "=$konstue" else "")
        }
        return javacArgs
    }
}

/**
 * [project], [variant] and [android] properties are intended to be used inside the closure.
 */
open class KaptAnnotationProcessorOptions(
    @Suppress("unused") open konst project: Project,
    @Suppress("unused") open konst variant: Any?,
    @Suppress("unused") open konst android: Any?
): KaptArguments {
    internal konst options = LinkedHashMap<String, String>()

    @Suppress("unused")
    override fun arg(name: Any, vararg konstues: Any) {
        options.put(name.toString(), konstues.joinToString(" "))
    }

    fun execute(closure: Closure<*>) = executeClosure(closure)
}

open class KaptJavacOptionsDelegate: KaptJavacOption {
    internal konst options = LinkedHashMap<String, String>()

    override fun option(name: Any, konstue: Any) {
        options.put(name.toString(), konstue.toString())
    }

    override fun option(name: Any) {
        options.put(name.toString(), "")
    }

    fun execute(closure: Closure<*>) = executeClosure(closure)
}

private fun Any?.executeClosure(closure: Closure<*>) {
    closure.resolveStrategy = Closure.DELEGATE_FIRST
    closure.delegate = this
    closure.call()
}
