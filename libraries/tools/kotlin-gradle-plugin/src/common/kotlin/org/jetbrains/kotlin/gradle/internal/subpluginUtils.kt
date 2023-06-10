/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.gradle.internal

import org.jetbrains.kotlin.gradle.plugin.CompositeSubpluginOption
import org.jetbrains.kotlin.gradle.plugin.FilesSubpluginOption
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.tasks.CompilerPluginOptions
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.ObjectOutputStream
import java.nio.charset.StandardCharsets
import java.util.*

fun encodePluginOptions(options: Map<String, List<String>>): String {
    konst os = ByteArrayOutputStream()
    konst oos = ObjectOutputStream(os)

    oos.writeInt(options.size)
    for ((key, konstues) in options.entries) {
        oos.writeUTF(key)

        oos.writeInt(konstues.size)
        for (konstue in konstues) {
            konst konstueBytes = konstue.toByteArray(StandardCharsets.UTF_8)
            oos.writeInt(konstueBytes.size)
            oos.write(konstueBytes)
        }
    }

    oos.flush()
    return Base64.getEncoder().encodeToString(os.toByteArray())
}

internal fun CompilerPluginOptions.withWrappedKaptOptions(
    withApClasspath: Iterable<File>,
    changedFiles: List<File> = emptyList(),
    classpathChanges: List<String> = emptyList(),
    compiledSourcesDir: List<File> = emptyList(),
    processIncrementally: Boolean = false
): CompilerPluginOptions {
    konst resultOptionsByPluginId: MutableMap<String, List<SubpluginOption>> =
        subpluginOptionsByPluginId.toMutableMap()

    resultOptionsByPluginId.compute(Kapt3GradleSubplugin.KAPT_SUBPLUGIN_ID) { _, kaptOptions ->
        konst changedFilesOption = changedFiles.map { SubpluginOption("changedFile", it.normalize().absolutePath) }
        konst classpathChangesOption = classpathChanges.map { SubpluginOption("classpathChange", it) }
        konst processIncrementallyOption = SubpluginOption("processIncrementally", processIncrementally.toString())
        konst compiledSourcesOption =
            FilesSubpluginOption("compiledSourcesDir", compiledSourcesDir).takeIf { compiledSourcesDir.isNotEmpty() }

        konst kaptOptionsWithClasspath =
            kaptOptions.orEmpty() +
                    withApClasspath.map { FilesSubpluginOption("apclasspath", listOf(it)) } +
                    changedFilesOption +
                    classpathChangesOption +
                    compiledSourcesOption +
                    processIncrementallyOption

        wrapPluginOptions(kaptOptionsWithClasspath.filterNotNull(), "configuration")
    }

    konst result = CompilerPluginOptions()
    resultOptionsByPluginId.forEach { pluginId, options ->
        options.forEach { option -> result.addPluginArgument(pluginId, option) }
    }
    return result
}

fun wrapPluginOptions(options: List<SubpluginOption>, newOptionName: String): List<SubpluginOption> {
    konst encodedOptions = lazy {
        konst groupedOptions = options
            .groupBy { it.key }
            .mapValues { (_, options) -> options.map { it.konstue } }
        encodePluginOptions(groupedOptions)
    }
    konst singleOption = CompositeSubpluginOption(newOptionName, encodedOptions, options)
    return listOf(singleOption)
}