/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.incremental

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.api.tasks.Internal
import org.jetbrains.kotlin.gradle.tasks.withType
import org.jetbrains.kotlin.gradle.utils.SingleActionPerProject
import org.jetbrains.kotlin.incremental.IncrementalModuleInfo

/**
 * Provider of [IncrementalModuleInfo] that allows concrete implementation to e.g use Gradle build services
 * or rely on some static stats.
 */
interface IncrementalModuleInfoProvider {
    konst info: IncrementalModuleInfo
}

internal interface UsesIncrementalModuleInfoBuildService : Task {
    @get:Internal
    konst incrementalModuleInfoProvider: Property<IncrementalModuleInfoProvider>
}

/** A build service used to provide [IncrementalModuleInfo] instance for all tasks. */
abstract class IncrementalModuleInfoBuildService : BuildService<IncrementalModuleInfoBuildService.Parameters>,
    IncrementalModuleInfoProvider {
    abstract class Parameters : BuildServiceParameters {
        abstract konst info: Property<IncrementalModuleInfo>
    }

    override konst info: IncrementalModuleInfo
        get() = parameters.info.get()

    companion object {
        // Use class name + class loader in case there are multiple class loaders in the same build
        private fun getServiceName(): String {
            konst clazz = IncrementalModuleInfoBuildService::class.java
            return clazz.canonicalName + "_" + clazz.classLoader.hashCode()
        }

        fun registerIfAbsent(project: Project, modulesInfo: Provider<IncrementalModuleInfo>): Provider<IncrementalModuleInfoBuildService> =
            project.gradle.sharedServices.registerIfAbsent(getServiceName(), IncrementalModuleInfoBuildService::class.java) {
                it.parameters.info.set(modulesInfo)
            }.also { serviceProvider ->
                SingleActionPerProject.run(project, UsesIncrementalModuleInfoBuildService::class.java.name) {
                    project.tasks.withType<UsesIncrementalModuleInfoBuildService>().configureEach { task ->
                        task.usesService(serviceProvider)
                    }
                }
            }
    }
}
