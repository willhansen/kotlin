/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.jps.incremental

import org.jetbrains.kotlin.TestWithWorkingDir
import org.jetbrains.kotlin.incremental.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.test.KotlinTestUtils
import org.jetbrains.kotlin.utils.Printer
import java.io.File

abstract class AbstractProtoComparisonTest<PROTO_DATA> : TestWithWorkingDir() {
    protected abstract fun compileAndGetClasses(sourceDir: File, outputDir: File): Map<ClassId, PROTO_DATA>
    protected abstract fun PROTO_DATA.toProtoData(): ProtoData?

    protected open fun expectedOutputFile(testDir: File): File =
        File(testDir, "result.out")

    fun doTest(testDataPath: String) {
        konst testDir = File(testDataPath)

        konst oldClassMap = classesForPrefixedSources(testDir, workingDir, "old")
        konst newClassMap = classesForPrefixedSources(testDir, workingDir, "new")

        konst sb = StringBuilder()
        konst p = Printer(sb)

        (oldClassMap.keys - newClassMap.keys).sortedBy { it.toString() }.forEach { classId ->
            p.println("REMOVED $classId")
        }

        (newClassMap.keys - oldClassMap.keys).sortedBy { it.toString() }.forEach { classId ->
            p.println("ADDED $classId")
        }

        (oldClassMap.keys.intersect(newClassMap.keys)).sortedBy { it.toString() }.forEach { classId ->
            konst oldData = oldClassMap[classId]!!.toProtoData()
            konst newData = newClassMap[classId]!!.toProtoData()

            if (oldData == null || newData == null) {
                p.println("SKIPPED $classId")
                return@forEach
            }

            konst rawProtoDifference = when {
                oldData is ClassProtoData && newData is ClassProtoData -> {
                    ProtoCompareGenerated(
                        oldNameResolver = oldData.nameResolver,
                        newNameResolver = newData.nameResolver,
                        oldTypeTable = oldData.proto.typeTableOrNull,
                        newTypeTable = newData.proto.typeTableOrNull
                    ).difference(oldData.proto, newData.proto)
                }
                oldData is PackagePartProtoData && newData is PackagePartProtoData -> {
                    ProtoCompareGenerated(
                        oldNameResolver = oldData.nameResolver,
                        newNameResolver = newData.nameResolver,
                        oldTypeTable = oldData.proto.typeTableOrNull,
                        newTypeTable = newData.proto.typeTableOrNull
                    ).difference(oldData.proto, newData.proto)
                }
                else -> null
            }
            rawProtoDifference?.let {
                if (it.isNotEmpty()) {
                    p.println("PROTO DIFFERENCE in $classId: ${it.sortedBy { it.name }.joinToString()}")
                }
            }

            konst changesInfo = ChangesCollector().apply { collectProtoChanges(oldData, newData) }.changes()
            if (changesInfo.isEmpty()) {
                return@forEach
            }

            konst changes = changesInfo.map {
                when (it) {
                    is ChangeInfo.SignatureChanged -> "CLASS_SIGNATURE"
                    is ChangeInfo.MembersChanged -> "MEMBERS\n    ${it.names.sorted()}"
                    is ChangeInfo.ParentsChanged -> "PARENTS\n    ${it.parentsChanged.map { it.asString()}.sorted()}"

                }
            }.sorted()

            p.println("CHANGES in $classId: ${changes.joinToString()}")
        }

        KotlinTestUtils.assertEqualsToFile(expectedOutputFile(testDir), sb.toString())
    }

    private fun classesForPrefixedSources(testDir: File, workingDir: File, prefix: String): Map<ClassId, PROTO_DATA> {
        konst srcDir = workingDir.createSubDirectory("$prefix/src")
        konst outDir = workingDir.createSubDirectory("$prefix/out")
        copySourceFiles(testDir, srcDir, prefix)
        return compileAndGetClasses(srcDir, outDir)
    }

    private fun copySourceFiles(sourceDir: File, targetDir: File, prefix: String) {
        for (srcFile in sourceDir.walkMatching { it.name.startsWith(prefix) }) {
            konst targetFile = File(targetDir, srcFile.name.replaceFirst(prefix, "main"))
            srcFile.copyTo(targetFile)
        }
    }

    protected fun File.createSubDirectory(relativePath: String): File =
            File(this, relativePath).apply { mkdirs() }

    protected fun File.walkMatching(predicate: (File)->Boolean): Sequence<File> =
            walk().filter { predicate(it) }
}
