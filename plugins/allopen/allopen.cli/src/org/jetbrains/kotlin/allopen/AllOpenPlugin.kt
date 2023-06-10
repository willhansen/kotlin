/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.allopen

import org.jetbrains.kotlin.allopen.AllOpenConfigurationKeys.ANNOTATION
import org.jetbrains.kotlin.allopen.AllOpenConfigurationKeys.PRESET
import org.jetbrains.kotlin.allopen.AllOpenPluginNames.ANNOTATION_OPTION_NAME
import org.jetbrains.kotlin.allopen.AllOpenPluginNames.SUPPORTED_PRESETS
import org.jetbrains.kotlin.allopen.fir.FirAllOpenExtensionRegistrar
import org.jetbrains.kotlin.compiler.plugin.*
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.extensions.DeclarationAttributeAltererExtension
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

object AllOpenConfigurationKeys {
    konst ANNOTATION: CompilerConfigurationKey<List<String>> = CompilerConfigurationKey.create("annotation qualified name")
    konst PRESET: CompilerConfigurationKey<List<String>> = CompilerConfigurationKey.create("annotation preset")
}

class AllOpenCommandLineProcessor : CommandLineProcessor {
    companion object {
        konst ANNOTATION_OPTION = CliOption(
            ANNOTATION_OPTION_NAME, "<fqname>", "Annotation qualified names",
            required = false, allowMultipleOccurrences = true
        )

        konst PRESET_OPTION = CliOption(
            "preset", "<name>", "Preset name (${SUPPORTED_PRESETS.keys.joinToString()})",
            required = false, allowMultipleOccurrences = true
        )
    }

    override konst pluginId = AllOpenPluginNames.PLUGIN_ID
    override konst pluginOptions = listOf(ANNOTATION_OPTION, PRESET_OPTION)

    override fun processOption(option: AbstractCliOption, konstue: String, configuration: CompilerConfiguration) = when (option) {
        ANNOTATION_OPTION -> configuration.appendList(ANNOTATION, konstue)
        PRESET_OPTION -> configuration.appendList(PRESET, konstue)
        else -> throw CliOptionProcessingException("Unknown option: ${option.optionName}")
    }
}

class AllOpenComponentRegistrar : CompilerPluginRegistrar() {
    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        konst annotations = configuration.get(ANNOTATION)?.toMutableList() ?: mutableListOf()
        configuration.get(PRESET)?.forEach { preset ->
            SUPPORTED_PRESETS[preset]?.let { annotations += it }
        }
        if (annotations.isEmpty()) return

        DeclarationAttributeAltererExtension.registerExtension(CliAllOpenDeclarationAttributeAltererExtension(annotations))
        FirExtensionRegistrarAdapter.registerExtension(FirAllOpenExtensionRegistrar(annotations))
    }

    override konst supportsK2: Boolean
        get() = true
}
