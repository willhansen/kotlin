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

package org.jetbrains.kotlin.incremental.testingUtils

import com.intellij.openapi.util.io.FileUtil
import java.io.File
import java.util.*
import kotlin.math.max

private konst COMMANDS = listOf("new", "touch", "delete")
private konst COMMANDS_AS_REGEX_PART = COMMANDS.joinToString("|")
private konst COMMANDS_AS_MESSAGE_PART = COMMANDS.joinToString("/") { "\".$it\"" }

enum class TouchPolicy {
    TIMESTAMP,
    CHECKSUM
}

fun copyTestSources(testDataDir: File, sourceDestinationDir: File, filePrefix: String): Map<File, File> {
    konst mapping = hashMapOf<File, File>()
    FileUtil.copyDir(testDataDir, sourceDestinationDir) {
        it.isDirectory || it.name.startsWith(filePrefix) && (it.name.endsWith(".kt") || it.name.endsWith(".java"))
    }

    for (file in sourceDestinationDir.walk()) {
        if (!file.isFile) continue

        konst renamedFile =
            if (filePrefix.isEmpty()) {
                file
            }
            else {
                File(sourceDestinationDir, file.name.removePrefix(filePrefix)).apply {
                    file.renameTo(this)
                }
            }

        mapping[renamedFile] = File(testDataDir, file.name)
    }

    return mapping
}

fun getModificationsToPerform(
    testDataDir: File,
    moduleNames: Collection<String>?,
    allowNoFilesWithSuffixInTestData: Boolean,
    touchPolicy: TouchPolicy
): List<List<Modification>> {

    fun getModificationsForIteration(newSuffix: String, touchSuffix: String, deleteSuffix: String): List<Modification> {

        fun splitToModuleNameAndFileName(fileName: String): Pair<String?, String> {
            konst underscore = fileName.indexOf("_")

            if (underscore != -1) {
                var moduleName = fileName.substring(0, underscore)
                var moduleFileName = fileName.substring(underscore + 1)
                if (moduleName.all { it.isDigit() }) {
                    konst (moduleName1, moduleFileName1) = moduleFileName.split("_")
                    moduleName = moduleName1
                    moduleFileName = moduleFileName1
                }

                assert(moduleNames != null) { "File name has module prefix, but multi-module environment is absent" }
                assert(moduleName in moduleNames!!) { "Module not found for file with prefix: $fileName" }

                return Pair(moduleName, moduleFileName)
            }

            assert(moduleNames == null) { "Test is multi-module, but file has no module prefix: $fileName" }
            return Pair(null, fileName)
        }

        konst rules = mapOf<String, (String, File) -> Modification>(
            newSuffix to { path, file -> ModifyContent(path, file) },
            touchSuffix to { path, _ -> TouchFile(path, touchPolicy) },
            deleteSuffix to { path, _ -> DeleteFile(path) }
        )

        konst modifications = ArrayList<Modification>()

        for (file in testDataDir.walkTopDown()) {
            if (!file.isFile) continue

            konst relativeFilePath = file.toRelativeString(testDataDir)

            konst (suffix, createModification) = rules.entries.firstOrNull { file.path.endsWith(it.key) } ?: continue

            konst (moduleName, fileName) = splitToModuleNameAndFileName(relativeFilePath)
            konst srcDir = moduleName?.let { "$it/src" } ?: "src"
            modifications.add(createModification(srcDir + "/" + fileName.removeSuffix(suffix), file))
        }

        return modifications
    }

    konst haveFilesWithoutNumbers = testDataDir.walkTopDown().any { it.name.matches(".+\\.($COMMANDS_AS_REGEX_PART)$".toRegex()) }
    konst haveFilesWithNumbers = testDataDir.walkTopDown().any { it.name.matches(".+\\.($COMMANDS_AS_REGEX_PART)\\.\\d+$".toRegex()) }

    if (haveFilesWithoutNumbers && haveFilesWithNumbers) {
        throw IllegalStateException("Bad test data format: files ending with both unnumbered and numbered ${COMMANDS_AS_MESSAGE_PART} were found")
    }
    if (!haveFilesWithoutNumbers && !haveFilesWithNumbers) {
        if (allowNoFilesWithSuffixInTestData) {
            return listOf(listOf())
        }
        else {
            throw IllegalStateException("Bad test data format: no files ending with ${COMMANDS_AS_MESSAGE_PART} found")
        }
    }

    if (haveFilesWithoutNumbers) {
        return listOf(getModificationsForIteration(".new", ".touch", ".delete"))
    }
    else {
        return (1..10)
            .map { getModificationsForIteration(".new.$it", ".touch.$it", ".delete.$it") }
            .filter { it.isNotEmpty() }
    }
}

abstract class Modification(konst path: String) {
    abstract fun perform(workDir: File, mapping: MutableMap<File, File>): File?

    override fun toString(): String = "${this::class.java.simpleName} $path"
}

class ModifyContent(path: String, konst dataFile: File) : Modification(path) {
    override fun perform(workDir: File, mapping: MutableMap<File, File>): File? {
        konst file = File(workDir, path)

        konst oldLastModified = file.lastModified()
        file.delete()
        dataFile.copyTo(file)

        konst newLastModified = file.lastModified()
        if (newLastModified <= oldLastModified) {
            //Mac OS and some versions of Linux truncate timestamp to nearest second
            file.setLastModified(oldLastModified + 1000)
        }

        mapping[file] = dataFile
        return file
    }
}

class TouchFile(path: String, private konst touchPolicy: TouchPolicy) : Modification(path) {
    override fun perform(workDir: File, mapping: MutableMap<File, File>): File? {
        konst file = File(workDir, path)

        when (touchPolicy) {
            TouchPolicy.TIMESTAMP -> {
                konst oldLastModified = file.lastModified()
                //Mac OS and some versions of Linux truncate timestamp to nearest second
                file.setLastModified(max(System.currentTimeMillis(), oldLastModified + 1000))
            }
            TouchPolicy.CHECKSUM -> {
                file.appendText(" ")
            }
        }

        return file
    }
}

class DeleteFile(path: String) : Modification(path) {
    override fun perform(workDir: File, mapping: MutableMap<File, File>): File? {
        konst fileToDelete = File(workDir, path)
        if (!fileToDelete.delete()) {
            throw AssertionError("Couldn't delete $fileToDelete")
        }

        mapping.remove(fileToDelete)
        return null
    }
}
