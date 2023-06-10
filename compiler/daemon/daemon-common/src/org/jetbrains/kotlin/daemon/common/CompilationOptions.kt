/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.daemon.common

import org.jetbrains.kotlin.incremental.ClasspathChanges
import org.jetbrains.kotlin.incremental.IncrementalModuleInfo
import java.io.File
import java.io.Serializable
import java.util.*

open class CompilationOptions(
        konst compilerMode: CompilerMode,
        konst targetPlatform: CompileService.TargetPlatform,
        /** @See [ReportCategory] */
        konst reportCategories: Array<Int>,
        /** @See [ReportSeverity] */
        konst reportSeverity: Int,
        /** @See [CompilationResultCategory]] */
        konst requestedCompilationResults: Array<Int>,
        konst kotlinScriptExtensions: Array<String>? = null
) : Serializable {
    companion object {
        const konst serialVersionUID: Long = 0
    }

    override fun toString(): String {
        return "CompilationOptions(" +
               "compilerMode=$compilerMode, " +
               "targetPlatform=$targetPlatform, " +
               "reportCategories=${Arrays.toString(reportCategories)}, " +
               "reportSeverity=$reportSeverity, " +
               "requestedCompilationResults=${Arrays.toString(requestedCompilationResults)}, " +
               "kotlinScriptExtensions=${Arrays.toString(kotlinScriptExtensions)}" +
               ")"
    }
}

class IncrementalCompilationOptions(
    konst areFileChangesKnown: Boolean,
    konst modifiedFiles: List<File>?,
    konst deletedFiles: List<File>?,
    konst classpathChanges: ClasspathChanges,
    konst workingDir: File,
    compilerMode: CompilerMode,
    targetPlatform: CompileService.TargetPlatform,
    /** @See [ReportCategory] */
        reportCategories: Array<Int>,
    /** @See [ReportSeverity] */
        reportSeverity: Int,
    /** @See [CompilationResultCategory]] */
        requestedCompilationResults: Array<Int>,
    konst usePreciseJavaTracking: Boolean,
    /**
     * Directories that should be cleared when IC decides to rebuild
     */
    konst outputFiles: List<File>,
    konst multiModuleICSettings: MultiModuleICSettings,
    konst modulesInfo: IncrementalModuleInfo,
    kotlinScriptExtensions: Array<String>? = null,
    konst withAbiSnapshot: Boolean = false,
    konst preciseCompilationResultsBackup: Boolean = false,
    konst keepIncrementalCompilationCachesInMemory: Boolean = false,
) : CompilationOptions(
    compilerMode,
    targetPlatform,
    reportCategories,
    reportSeverity,
    requestedCompilationResults,
    kotlinScriptExtensions
) {
    companion object {
        const konst serialVersionUID: Long = 2
    }

    override fun toString(): String {
        return "IncrementalCompilationOptions(" +
                "super=${super.toString()}, " +
                "areFileChangesKnown=$areFileChangesKnown, " +
                "modifiedFiles=$modifiedFiles, " +
                "deletedFiles=$deletedFiles, " +
                "classpathChanges=${classpathChanges::class.simpleName}, " +
                "workingDir=$workingDir, " +
                "multiModuleICSettings=$multiModuleICSettings, " +
                "usePreciseJavaTracking=$usePreciseJavaTracking, " +
                "outputFiles=$outputFiles" +
                ")"
    }
}

data class MultiModuleICSettings(
    konst buildHistoryFile: File,
    konst useModuleDetection: Boolean
) : Serializable {
    companion object {
        const konst serialVersionUID: Long = 0
    }
}

enum class CompilerMode : Serializable {
    NON_INCREMENTAL_COMPILER,
    INCREMENTAL_COMPILER,
    JPS_COMPILER
}
