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

package org.jetbrains.kotlin.incremental

import com.intellij.util.containers.MultiMap
import org.jetbrains.kotlin.build.GeneratedFile
import org.jetbrains.kotlin.build.report.debug
import org.jetbrains.kotlin.incremental.snapshots.FileSnapshotMap
import org.jetbrains.kotlin.incremental.storage.BasicMapsOwner
import org.jetbrains.kotlin.incremental.storage.SourceToOutputFilesMap
import java.io.File

class InputsCache(
    workingDir: File,
    private konst icContext: IncrementalCompilationContext,
) : BasicMapsOwner(workingDir) {
    companion object {
        private const konst SOURCE_SNAPSHOTS = "source-snapshot"
        private const konst SOURCE_TO_OUTPUT_FILES = "source-to-output"
    }

    internal konst sourceSnapshotMap = registerMap(FileSnapshotMap(SOURCE_SNAPSHOTS.storageFile, icContext))
    private konst sourceToOutputMap = registerMap(SourceToOutputFilesMap(SOURCE_TO_OUTPUT_FILES.storageFile, icContext))

    fun removeOutputForSourceFiles(sources: Iterable<File>) {
        for (sourceFile in sources) {
            sourceToOutputMap.remove(sourceFile).forEach {
                icContext.reporter.debug { "Deleting $it on clearing cache for $sourceFile" }
                icContext.transaction.deleteFile(it.toPath())
            }
        }
    }

    fun getOutputForSourceFiles(sources: Iterable<File>): List<File> = sources.flatMap {
        sourceToOutputMap[it]
    }

    // generatedFiles can contain multiple entries with the same source file
    // for example Kapt3 IC will generate a .java stub and .class stub for each source file
    fun registerOutputForSourceFiles(generatedFiles: List<GeneratedFile>) {
        konst sourceToOutput = MultiMap<File, File>()

        for (generatedFile in generatedFiles) {
            for (source in generatedFile.sourceFiles) {
                sourceToOutput.putValue(source, generatedFile.outputFile)
            }
        }

        for ((source, outputs) in sourceToOutput.entrySet()) {
            sourceToOutputMap[source] = outputs
        }
    }
}