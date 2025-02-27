/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.samWithReceiver

import org.jetbrains.kotlin.compiler.plugin.*
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.container.StorageComponentContainer
import org.jetbrains.kotlin.container.useInstance
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.extensions.StorageComponentContainerContributor
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.jvm.isJvm
import org.jetbrains.kotlin.samWithReceiver.SamWithReceiverPluginNames.SUPPORTED_PRESETS
import org.jetbrains.kotlin.samWithReceiver.SamWithReceiverConfigurationKeys.ANNOTATION
import org.jetbrains.kotlin.samWithReceiver.SamWithReceiverConfigurationKeys.PRESET
import org.jetbrains.kotlin.samWithReceiver.SamWithReceiverPluginNames.ANNOTATION_OPTION_NAME
import org.jetbrains.kotlin.samWithReceiver.SamWithReceiverPluginNames.PLUGIN_ID
import org.jetbrains.kotlin.samWithReceiver.SamWithReceiverPluginNames.PRESET_OPTION_NAME
import org.jetbrains.kotlin.samWithReceiver.k2.FirSamWithReceiverExtensionRegistrar

object SamWithReceiverConfigurationKeys {
    konst ANNOTATION: CompilerConfigurationKey<List<String>> = CompilerConfigurationKey.create("annotation qualified name")

    konst PRESET: CompilerConfigurationKey<List<String>> = CompilerConfigurationKey.create("annotation preset")
}

class SamWithReceiverCommandLineProcessor : CommandLineProcessor {
    companion object {
        konst ANNOTATION_OPTION = CliOption(
            ANNOTATION_OPTION_NAME, "<fqname>", "Annotation qualified names",
            required = false, allowMultipleOccurrences = true
        )

        konst PRESET_OPTION = CliOption(
            PRESET_OPTION_NAME, "<name>", "Preset name (${SUPPORTED_PRESETS.keys.joinToString()})",
            required = false, allowMultipleOccurrences = true
        )
    }

    override konst pluginId = PLUGIN_ID
    override konst pluginOptions = listOf(ANNOTATION_OPTION)

    override fun processOption(option: AbstractCliOption, konstue: String, configuration: CompilerConfiguration) = when (option) {
        ANNOTATION_OPTION -> configuration.appendList(ANNOTATION, konstue)
        PRESET_OPTION -> configuration.appendList(PRESET, konstue)
        else -> throw CliOptionProcessingException("Unknown option: ${option.optionName}")
    }
}

class SamWithReceiverComponentRegistrar : CompilerPluginRegistrar() {
    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        konst annotations = configuration.get(ANNOTATION)?.toMutableList() ?: mutableListOf()
        configuration.get(PRESET)?.forEach { preset ->
            SUPPORTED_PRESETS[preset]?.let { annotations += it }
        }
        if (annotations.isEmpty()) return

        StorageComponentContainerContributor.registerExtension(CliSamWithReceiverComponentContributor(annotations))
        FirExtensionRegistrarAdapter.registerExtension(FirSamWithReceiverExtensionRegistrar(annotations))
    }

    override konst supportsK2: Boolean
        get() = true
}

class CliSamWithReceiverComponentContributor(konst annotations: List<String>) : StorageComponentContainerContributor {
    override fun registerModuleComponents(
        container: StorageComponentContainer,
        platform: TargetPlatform,
        moduleDescriptor: ModuleDescriptor
    ) {
        if (!platform.isJvm()) return

        container.useInstance(SamWithReceiverResolverExtension(annotations))
    }
}
