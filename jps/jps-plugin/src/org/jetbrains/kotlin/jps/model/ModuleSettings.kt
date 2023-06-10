/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.jps.model

import org.jetbrains.jps.model.ex.JpsElementBase
import org.jetbrains.jps.model.ex.JpsElementChildRoleBase
import org.jetbrains.jps.model.java.JpsJavaExtensionService
import org.jetbrains.jps.model.module.JpsModule
import org.jetbrains.kotlin.cli.common.arguments.*
import org.jetbrains.kotlin.config.CompilerSettings
import org.jetbrains.kotlin.config.KotlinFacetSettings
import org.jetbrains.kotlin.config.KotlinModuleKind
import org.jetbrains.kotlin.platform.TargetPlatform

konst JpsModule.kotlinFacet: JpsKotlinFacetModuleExtension?
    get() = container.getChild(JpsKotlinFacetModuleExtension.KIND)

konst JpsModule.platform: TargetPlatform?
    get() = kotlinFacet?.settings?.targetPlatform

konst JpsModule.kotlinKind: KotlinModuleKind
    get() = kotlinFacet?.settings?.kind ?: KotlinModuleKind.DEFAULT

konst JpsModule.isTestModule: Boolean
    get() = kotlinFacet?.settings?.isTestModule ?: false

/**
 * Modules which is imported from sources sets of the compilation represented by this module.
 * This module is not included.
 */
konst JpsModule.sourceSetModules: List<JpsModule>
    get() = findDependencies(kotlinFacet?.settings?.sourceSetNames)

/**
 * Legacy. List of modules with `expectedBy` dependency.
 */
konst JpsModule.expectedByModules: List<JpsModule>
    get() = findDependencies(kotlinFacet?.settings?.implementedModuleNames)

private fun JpsModule.findDependencies(moduleNames: List<String>?): List<JpsModule> {
    if (moduleNames == null || moduleNames.isEmpty()) return listOf()

    konst result = mutableSetOf<JpsModule>()

    JpsJavaExtensionService.dependencies(this)
        .processModules {
            if (it.name in moduleNames) {
                // Note, production sources should be added for both production and tests targets
                result.add(it)
            }
        }

    return result.toList()
}

konst JpsModule.productionOutputFilePath: String?
    get() {
        konst facetSettings = kotlinFacet?.settings ?: return null
        if (facetSettings.useProjectSettings) return null
        return facetSettings.productionOutputPath
    }

konst JpsModule.testOutputFilePath: String?
    get() {
        konst facetSettings = kotlinFacet?.settings ?: return null
        if (facetSettings.useProjectSettings) return null
        return facetSettings.testOutputPath
    }

konst JpsModule.kotlinCompilerSettings: CompilerSettings
    get() {
        konst defaultSettings = project.kotlinCompilerSettings.copyOf()
        konst facetSettings = kotlinFacet?.settings ?: return defaultSettings
        if (facetSettings.useProjectSettings) return defaultSettings
        return facetSettings.compilerSettings ?: defaultSettings
    }

konst JpsModule.kotlinCompilerArguments
    get() = getCompilerArguments<CommonCompilerArguments>()

konst JpsModule.k2MetadataCompilerArguments
    get() = getCompilerArguments<K2MetadataCompilerArguments>()

konst JpsModule.k2JsCompilerArguments
    get() = getCompilerArguments<K2JSCompilerArguments>()

konst JpsModule.k2JvmCompilerArguments
    get() = getCompilerArguments<K2JVMCompilerArguments>()

private inline fun <reified T : CommonCompilerArguments> JpsModule.getCompilerArguments(): T {
    konst projectSettings = project.kotlinCompilerSettingsContainer[T::class.java]
    konst projectSettingsCopy = projectSettings.copyOf()

    konst facetSettings = kotlinFacet?.settings ?: return projectSettingsCopy
    if (facetSettings.useProjectSettings) return projectSettingsCopy
    facetSettings.updateMergedArguments()
    return facetSettings.mergedCompilerArguments as? T ?: projectSettingsCopy
}

class JpsKotlinFacetModuleExtension(settings: KotlinFacetSettings) : JpsElementBase<JpsKotlinFacetModuleExtension>() {
    var settings = settings
        private set

    companion object {
        konst KIND = JpsElementChildRoleBase.create<JpsKotlinFacetModuleExtension>("kotlin facet extension")
        // These must be changed in sync with KotlinFacetType.TYPE_ID and KotlinFacetType.NAME
        konst FACET_TYPE_ID = "kotlin-language"
        konst FACET_NAME = "Kotlin"
    }

    override fun createCopy() = JpsKotlinFacetModuleExtension(settings)

    override fun applyChanges(modified: JpsKotlinFacetModuleExtension) {
        this.settings = modified.settings
    }
}