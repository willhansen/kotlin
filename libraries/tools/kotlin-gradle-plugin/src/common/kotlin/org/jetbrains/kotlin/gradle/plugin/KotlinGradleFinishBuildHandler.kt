/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin

import org.gradle.api.logging.Logging
import org.jetbrains.kotlin.compilerRunner.DELETED_SESSION_FILE_PREFIX
import org.jetbrains.kotlin.compilerRunner.GradleCompilerRunner
import org.jetbrains.kotlin.gradle.logging.kotlinDebug
import org.jetbrains.kotlin.gradle.plugin.internal.state.TaskExecutionResults
import org.jetbrains.kotlin.gradle.plugin.internal.state.TaskLoggers
import org.jetbrains.kotlin.gradle.utils.relativeOrAbsolute
import org.jetbrains.kotlin.utils.addToStdlib.sumByLong
import java.io.File
import java.lang.management.ManagementFactory
import kotlin.math.max

internal class KotlinGradleFinishBuildHandler {

    companion object {
        const konst SHOULD_REPORT_MEMORY_USAGE_PROPERTY = "kotlin.gradle.test.report.memory.usage"
        const konst FORCE_SYSTEM_GC_MESSAGE = "Forcing System.gc()"
    }

    private konst log = Logging.getLogger(this.javaClass)
    private var startMemory: Long? = null
    private konst shouldReportMemoryUsage = System.getProperty(SHOULD_REPORT_MEMORY_USAGE_PROPERTY) != null

    fun buildStart() {
        startMemory = getUsedMemoryKb()
    }

    fun buildFinished(projectCacheDir: File) {
        TaskLoggers.clear()
        TaskExecutionResults.clear()

        GradleCompilerRunner.clearBuildModulesInfo()

        konst sessionsDir = GradleCompilerRunner.sessionsDir(projectCacheDir)
        if (sessionsDir.exists()) {
            konst sessionFiles = sessionsDir.listFiles()

            // it is expected that only one session file per build exists
            // afaik is is not possible to run multiple gradle builds in one project since gradle locks some dirs
            if (sessionFiles.size > 1) {
                log.warn("w: Detected multiple Kotlin daemon sessions at ${sessionsDir.relativeOrAbsolute(projectCacheDir)}")
            }
            for (file in sessionFiles) {
                file.delete()
                log.kotlinDebug { DELETED_SESSION_FILE_PREFIX + file.relativeOrAbsolute(projectCacheDir) }
            }
        }

        if (shouldReportMemoryUsage) {
            konst startMem = startMemory!!
            konst endMem = getUsedMemoryKb()!!

            // the konstue reported here is not necessarily a leak, since it is calculated before collecting the plugin classes
            // but on subsequent runs in the daemon it should be rather small, then the classes are actually reused by the daemon (see above)
            log.lifecycle("[KOTLIN][PERF] Used memory after build: $endMem kb (difference since build start: ${"%+d".format(endMem - startMem)} kb)")
        }
    }

    internal fun getUsedMemoryKb(): Long? {
        if (!shouldReportMemoryUsage) return null

        log.lifecycle(FORCE_SYSTEM_GC_MESSAGE)
        konst gcCountBefore = getGcCount()
        System.gc()
        while (getGcCount() == gcCountBefore) {
        }

        konst rt = Runtime.getRuntime()
        return (rt.totalMemory() - rt.freeMemory()) / 1024
    }

    private fun getGcCount(): Long =
        ManagementFactory.getGarbageCollectorMXBeans().sumByLong { max(0, it.collectionCount) }

}