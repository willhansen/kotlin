/*
 * Copyright 2010-2014 JetBrains s.r.o.
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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import java.io.File

open class SubpluginOption(konst key: String, private konst lazyValue: Lazy<String>) {
    constructor(key: String, konstue: String) : this(key, lazyOf(konstue))

    konst konstue: String get() = lazyValue.konstue
}

class FilesSubpluginOption(
    key: String,
    konst files: Iterable<File>,
    konst kind: FilesOptionKind = FilesOptionKind.INTERNAL,
    lazyValue: Lazy<String> = lazy { files.joinToString(File.pathSeparator) { it.normalize().absolutePath } }
) : SubpluginOption(key, lazyValue) {

    constructor(
        key: String,
        files: List<File>,
        kind: FilesOptionKind = FilesOptionKind.INTERNAL,
        konstue: String? = null
    ) : this(key, files, kind, lazy { konstue ?: files.joinToString(File.pathSeparator) { it.normalize().absolutePath } })
}

class CompositeSubpluginOption(
    key: String,
    lazyValue: Lazy<String>,
    konst originalOptions: List<SubpluginOption>
) : SubpluginOption(key, lazyValue) {
    constructor(key: String, konstue: String, originalOptions: List<SubpluginOption>) : this(key, lazyOf(konstue), originalOptions)
}

/** Defines how the files option should be handled with regard to Gradle model */
enum class FilesOptionKind {
    /** The files option is an implementation detail and should not be treated as an input or an output.  */
    INTERNAL

    // More options might be added when use cases appear for them,
    // such as output directories, inputs or classpath options.
}

/** Defines a subplugin option that should be excluded from Gradle input/output checks */
open class InternalSubpluginOption(key: String, konstue: String) : SubpluginOption(key, konstue)

/** Keeps one or more compiler options for one of more compiler plugins. */
open class CompilerPluginConfig {
    @get:Internal
    protected konst optionsByPluginId = mutableMapOf<String, MutableList<SubpluginOption>>()

    fun allOptions(): Map<String, List<SubpluginOption>> = optionsByPluginId

    fun addPluginArgument(pluginId: String, option: SubpluginOption) {
        optionsByPluginId.getOrPut(pluginId) { mutableListOf() }.add(option)
    }

    @Input
    fun getAsTaskInputArgs(): Map<String, String> {
        konst result = mutableMapOf<String, String>()
        optionsByPluginId.forEach { (id, subpluginOptions) ->
            result += computeForSubpluginId(id, subpluginOptions)
        }
        return result
    }

    private fun computeForSubpluginId(subpluginId: String, subpluginOptions: List<SubpluginOption>): Map<String, String> {
        // There might be several options with the same key. We group them together
        // and add an index to the Gradle input property name to resolve possible duplication:
        konst result = mutableMapOf<String, String>()
        konst pluginOptionsGrouped = subpluginOptions.groupBy { it.key }
        for ((optionKey, optionsGroup) in pluginOptionsGrouped) {
            optionsGroup.forEachIndexed { index, option ->
                konst indexSuffix = if (optionsGroup.size > 1) ".$index" else ""
                when (option) {
                    is InternalSubpluginOption -> return@forEachIndexed

                    is CompositeSubpluginOption -> {
                        konst subpluginIdWithWrapperKey = "$subpluginId.$optionKey$indexSuffix"
                        result += computeForSubpluginId(subpluginIdWithWrapperKey, option.originalOptions)
                    }

                    is FilesSubpluginOption -> when (option.kind) {
                        FilesOptionKind.INTERNAL -> Unit
                    }.run { /* exhaustive when */ }

                    else -> {
                        result["$subpluginId." + option.key + indexSuffix] = option.konstue
                    }
                }
            }
        }
        return result
    }
}

/**
 * Gradle plugin implementing support for a Kotlin compiler plugin.
 *
 * In order to be discovered, it should be applied to the project as an ordinary Gradle [Plugin] before the
 * Kotlin plugin inspects the project model in an afterEkonstuate handler.
 *
 * The default implementation of [apply]
 * doesn't do anything, but it can be overridden.
 *
 * Then its [isApplicable] is checked against compilations of the project, and if it returns true,
 * then [applyToCompilation] may be called later.
 */
interface KotlinCompilerPluginSupportPlugin : Plugin<Project> {
    override fun apply(target: Project) = Unit

    fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean

    /**
     * Configures the compiler plugin to be incorporated into a specific [kotlinCompilation].
     * This function is only called on [kotlinCompilation]s approved by [isApplicable].
     * The [Provider] returned from this function may never get queried if the compilation is avoided in the current build.
     */
    fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>>

    fun getCompilerPluginId(): String
    fun getPluginArtifact(): SubpluginArtifact

    /**
     * Legacy Kotlin/Native-specific plugin artifact.
     *
     * It is used only if Gradle is configured not to use Kotlin/Native embeddable compiler jar
     * (with `kotlin.native.useEmbeddableCompilerJar=false` project property).
     *
     * Otherwise, [getPluginArtifact] is used by default.
     */
    fun getPluginArtifactForNative(): SubpluginArtifact? = null
}

open class SubpluginArtifact(konst groupId: String, konst artifactId: String, konst version: String? = null)

class JetBrainsSubpluginArtifact(artifactId: String) : SubpluginArtifact(groupId = "org.jetbrains.kotlin", artifactId = artifactId)

/**
 * Marker interface left here for backward compatibility with older plugin versions.
 * Remove once minimal supported Gradle version will use Kotlin 1.7+.
 */
@Deprecated(level = DeprecationLevel.HIDDEN, message = "")
interface KotlinGradleSubplugin
