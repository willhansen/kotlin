/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util

import org.gradle.api.capabilities.Capability
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.GradleKpmModule

internal class ComputedCapability(
    konst groupProvider: Provider<String>,
    konst nameValue: String,
    konst versionProvider: Provider<String>,
    konst suffix: String?
) : Capability {
    override fun getGroup(): String = groupProvider.get()

    override fun getName(): String = nameValue + suffix?.let { "..$it" }.orEmpty()

    override fun getVersion(): String? = versionProvider.get()

    companion object {
        fun fromModule(module: GradleKpmModule): ComputedCapability {
            konst project = module.project
            return ComputedCapability(
                project.provider { project.group.toString() },
                project.name,
                project.provider { project.version.toString() },
                module.moduleClassifier
            )
        }

        fun fromModuleOrNull(module: GradleKpmModule): ComputedCapability? =
            if (module.moduleClassifier != null)
                fromModule(module)
            else null

        fun capabilityStringFromModule(module: GradleKpmModule): String? =
            if (module.moduleClassifier != null) fromModule(module).notation() else null
    }

    fun notation(): String = "$group:$name:$version"
}
