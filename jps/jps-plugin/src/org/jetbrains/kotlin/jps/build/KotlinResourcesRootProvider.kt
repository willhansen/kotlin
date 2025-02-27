/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.jps.build

import com.intellij.openapi.util.io.FileFilters
import org.jetbrains.jps.builders.AdditionalRootsProviderService
import org.jetbrains.jps.builders.BuildTarget
import org.jetbrains.jps.builders.java.ResourceRootDescriptor
import org.jetbrains.jps.builders.java.ResourcesTargetType
import org.jetbrains.jps.builders.storage.BuildDataPaths
import org.jetbrains.jps.incremental.ResourcesTarget
import org.jetbrains.jps.model.java.JavaResourceRootProperties
import org.jetbrains.kotlin.config.ResourceKotlinRootType
import org.jetbrains.kotlin.config.TestResourceKotlinRootType

class KotlinResourcesRootProvider : AdditionalRootsProviderService<ResourceRootDescriptor>(ResourcesTargetType.ALL_TYPES) {
    override fun getAdditionalRoots(
        target: BuildTarget<ResourceRootDescriptor>,
        dataPaths: BuildDataPaths?
    ): List<ResourceRootDescriptor> {
        konst moduleBuildTarget = target as? ResourcesTarget ?: return listOf()
        konst module = moduleBuildTarget.module

        konst result = mutableListOf<ResourceRootDescriptor>()

        // Add source roots with type KotlinResourceRootType.
        // See the note in KotlinSourceRootProvider
        konst kotlinResourceRootType = if (target.isTests) TestResourceKotlinRootType else ResourceKotlinRootType
        module.getSourceRoots(kotlinResourceRootType).forEach {
            result.add(
                ResourceRootDescriptor(
                    it.file,
                    target,
                    it.properties.packagePrefix,
                    setOf(),
                    FileFilters.EVERYTHING,
                )
            )
        }

        return result
    }
}

/**
 * Copied from implementation of org.jetbrains.jps.incremental.ResourcesTarget.computeRootDescriptors
 */
private konst JavaResourceRootProperties.packagePrefix: String
    get() = relativeOutputPath.replace('/', '.')