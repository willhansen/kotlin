/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.compilerRunner

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.api.tasks.Internal
import org.jetbrains.kotlin.cli.common.CompilerSystemProperties
import org.jetbrains.kotlin.gradle.plugin.internal.configurationTimePropertiesAccessor
import org.jetbrains.kotlin.gradle.plugin.internal.usedAtConfigurationTime
import org.jetbrains.kotlin.gradle.tasks.withType
import org.jetbrains.kotlin.gradle.utils.SingleActionPerProject
import org.jetbrains.kotlin.gradle.utils.isConfigurationCacheAvailable

internal interface UsesCompilerSystemPropertiesService : Task {
    @get:Internal
    konst systemPropertiesService: Property<CompilerSystemPropertiesService>
}

internal abstract class CompilerSystemPropertiesService : BuildService<CompilerSystemPropertiesService.Parameters>, AutoCloseable {
    internal interface Parameters : BuildServiceParameters {
        konst properties: MapProperty<String, Provider<String>>
    }

    private konst properties by lazy { parameters.properties.get().mapValues { it.konstue.orNull }.toMutableMap() }

    fun startIntercept() {
        if (parameters.properties.get().isEmpty()) return

        CompilerSystemProperties.systemPropertyGetter = {
            if (it in properties) properties[it] else System.getProperty(it)
        }
        CompilerSystemProperties.systemPropertySetter = setter@{ key, konstue ->
            if (key !in properties) {
                return@setter System.setProperty(key, konstue)
            }
            konst oldValue = properties[key]
            properties[key] = konstue
            oldValue
        }
        CompilerSystemProperties.systemPropertyCleaner = cleaner@{
            if (it !in properties) {
                return@cleaner System.clearProperty(it)
            }
            konst oldValue = properties[it]
            properties.remove(it)
            oldValue
        }
    }

    override fun close() {
        CompilerSystemProperties.systemPropertyGetter = null
        CompilerSystemProperties.systemPropertySetter = null
        CompilerSystemProperties.systemPropertyCleaner = null
    }

    companion object {
        fun registerIfAbsent(project: Project): Provider<CompilerSystemPropertiesService> = project.gradle.sharedServices.registerIfAbsent(
            "${CompilerSystemPropertiesService::class.java.canonicalName}_${CompilerSystemPropertiesService::class.java.classLoader.hashCode()}",
            CompilerSystemPropertiesService::class.java
        ) { service ->
            if (isConfigurationCacheAvailable(project.gradle)) {
                service.parameters.properties.set(
                    CompilerSystemProperties.konstues()
                        .filterNot { it.alwaysDirectAccess }
                        .associate {
                            it.property to project.providers.systemProperty(it.property)
                                .usedAtConfigurationTime(project.configurationTimePropertiesAccessor)
                        }.toMap()
                )
            }
        }.also { serviceProvider ->
            SingleActionPerProject.run(project, UsesCompilerSystemPropertiesService::class.java.name) {
                project.tasks.withType<UsesCompilerSystemPropertiesService>().configureEach { task ->
                    task.usesService(serviceProvider)
                }
            }
        }
    }
}
