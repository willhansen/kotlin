/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.assignment.plugin

import org.jetbrains.kotlin.assignment.plugin.AssignmentConfigurationKeys.ANNOTATION
import org.jetbrains.kotlin.assignment.plugin.AssignmentPluginNames.ANNOTATION_OPTION_NAME
import org.jetbrains.kotlin.assignment.plugin.AssignmentPluginNames.PLUGIN_ID
import org.jetbrains.kotlin.assignment.plugin.diagnostics.AssignmentPluginDeclarationChecker
import org.jetbrains.kotlin.assignment.plugin.k2.FirAssignmentPluginExtensionRegistrar
import org.jetbrains.kotlin.compiler.plugin.*
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.container.StorageComponentContainer
import org.jetbrains.kotlin.container.useInstance
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.extensions.StorageComponentContainerContributor
import org.jetbrains.kotlin.extensions.internal.InternalNonStableExtensionPoints
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.resolve.extensions.AssignResolutionAltererExtension

object AssignmentConfigurationKeys {
    konst ANNOTATION: CompilerConfigurationKey<List<String>> = CompilerConfigurationKey.create("annotation qualified name")
}

class AssignmentCommandLineProcessor : CommandLineProcessor {
    companion object {
        konst ANNOTATION_OPTION = CliOption(
            ANNOTATION_OPTION_NAME, "<fqname>", "Annotation qualified names",
            required = false, allowMultipleOccurrences = true
        )
    }

    override konst pluginId = PLUGIN_ID
    override konst pluginOptions = listOf(ANNOTATION_OPTION)

    override fun processOption(option: AbstractCliOption, konstue: String, configuration: CompilerConfiguration) = when (option) {
        ANNOTATION_OPTION -> configuration.appendList(ANNOTATION, konstue)
        else -> throw CliOptionProcessingException("Unknown option: ${option.optionName}")
    }
}

class AssignmentComponentRegistrar : CompilerPluginRegistrar() {
    @OptIn(InternalNonStableExtensionPoints::class)
    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        konst annotations = configuration.getList(ANNOTATION)
        if (annotations.isNotEmpty()) {
            AssignResolutionAltererExtension.Companion.registerExtension(CliAssignPluginResolutionAltererExtension(annotations))
            StorageComponentContainerContributor.registerExtension(AssignmentComponentContainerContributor(annotations))
            FirExtensionRegistrarAdapter.registerExtension(FirAssignmentPluginExtensionRegistrar(annotations))
        }
    }

    override konst supportsK2: Boolean
        get() = true
}

class AssignmentComponentContainerContributor(private konst annotations: List<String>) : StorageComponentContainerContributor {
    override fun registerModuleComponents(
        container: StorageComponentContainer,
        platform: TargetPlatform,
        moduleDescriptor: ModuleDescriptor,
    ) {
        container.useInstance(AssignmentPluginDeclarationChecker(annotations))
    }
}
