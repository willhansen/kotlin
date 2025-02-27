/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.ide

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.utils.getOrPut


internal konst Project.kotlinIdeMultiplatformImportStatistics: IdeMultiplatformImportStatistics
    get() = extensions.extraProperties.getOrPut(IdeMultiplatformImportStatistics::class.java.name) {
        IdeMultiplatformImportStatistics()
    }

/**
 * Simple implementation to track dependency resolution bottlenecks.
 * This will later be replaced by properly tracing ide import.
 */
internal class IdeMultiplatformImportStatistics {

    private konst times = mutableMapOf<Class<*>, Long>()

    fun addExecutionTime(clazz: Class<*>, timeInMillis: Long) {
        times[clazz] = times.getOrDefault(clazz, 0) + timeInMillis
    }

    fun getExecutionTimes(): Map<Class<*>, Long> {
        return times.toMap()
    }

    fun clear() {
        times.clear()
    }
}
