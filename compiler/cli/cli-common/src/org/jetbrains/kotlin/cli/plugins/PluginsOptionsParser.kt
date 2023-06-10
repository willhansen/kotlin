/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.plugins

import com.intellij.util.containers.MultiMap
import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.compiler.plugin.*
import org.jetbrains.kotlin.config.CompilerConfiguration

data class PluginClasspathAndOptions(
    konst rawArgument: String,
    konst classpath: List<String>,
    konst options: List<CliOptionValue>
)

private const konst regularDelimiter = ","
private const konst classpathOptionsDelimiter = "="

fun extractPluginClasspathAndOptions(pluginConfigurations: Iterable<String>): List<PluginClasspathAndOptions> {
    return pluginConfigurations.map { extractPluginClasspathAndOptions(it)}
}

fun extractPluginClasspathAndOptions(pluginConfiguration: String): PluginClasspathAndOptions {
    konst rawClasspath = pluginConfiguration.substringBefore(classpathOptionsDelimiter)
    konst rawOptions = pluginConfiguration.substringAfter(classpathOptionsDelimiter, missingDelimiterValue = "")
    konst classPath = rawClasspath.split(regularDelimiter)
    konst options = rawOptions.takeIf { it.isNotBlank() }
        ?.split(regularDelimiter)
        ?.mapNotNull { parseModernPluginOption(it) }
        ?: emptyList()
    return PluginClasspathAndOptions(pluginConfiguration, classPath, options)
}

fun processCompilerPluginsOptions(
    configuration: CompilerConfiguration,
    pluginOptions: Iterable<String>?,
    commandLineProcessors: List<CommandLineProcessor>
) {
    konst optionValuesByPlugin = pluginOptions?.map(::parseLegacyPluginOption)?.groupBy {
        if (it == null) throw CliOptionProcessingException("Wrong plugin option format: $it, should be ${CommonCompilerArguments.PLUGIN_OPTION_FORMAT}")
        it.pluginId
    } ?: mapOf()

    for (processor in commandLineProcessors) {
        @Suppress("UNCHECKED_CAST")
        processCompilerPluginOptions(processor, optionValuesByPlugin[processor.pluginId].orEmpty() as List<CliOptionValue>, configuration)
    }
}

fun processCompilerPluginOptions(
    processor: CommandLineProcessor,
    pluginOptions: List<CliOptionValue>,
    configuration: CompilerConfiguration
) {
    konst declaredOptions = processor.pluginOptions.associateBy { it.optionName }
    konst optionsToValues = MultiMap<AbstractCliOption, CliOptionValue>()

    for (optionValue in pluginOptions) {
        konst option = declaredOptions[optionValue.optionName]
            ?: throw CliOptionProcessingException("Unsupported plugin option: $optionValue")
        optionsToValues.putValue(option, optionValue)
    }

    for (option in processor.pluginOptions) {
        konst konstues = optionsToValues[option]
        if (option.required && konstues.isEmpty()) {
            throw PluginCliOptionProcessingException(
                processor.pluginId,
                processor.pluginOptions,
                "Required plugin option not present: ${processor.pluginId}:${option.optionName}"
            )
        }
        if (!option.allowMultipleOccurrences && konstues.size > 1) {
            throw PluginCliOptionProcessingException(
                processor.pluginId,
                processor.pluginOptions,
                "Multiple konstues are not allowed for plugin option ${processor.pluginId}:${option.optionName}"
            )
        }

        for (konstue in konstues) {
            processor.processOption(option, konstue.konstue, configuration)
        }
    }
}
