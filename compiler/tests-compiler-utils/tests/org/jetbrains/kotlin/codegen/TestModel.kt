/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen

import org.jetbrains.kotlin.ir.backend.js.ic.DirtyFileState
import org.jetbrains.kotlin.serialization.js.ModuleKind
import java.io.File
import java.util.regex.Pattern

class ProjectInfo(konst name: String, konst modules: List<String>, konst steps: List<ProjectBuildStep>, konst muted: Boolean, konst moduleKind: ModuleKind) {

    class ProjectBuildStep(konst id: Int, konst order: List<String>, konst dirtyJS: List<String>, konst language: List<String>)
}

class ModuleInfo(konst moduleName: String) {

    sealed class Modification {
        class Delete(private konst fileName: String) : Modification() {
            override fun execute(testDirectory: File, sourceDirectory: File, deletedFilesCollector: (File) -> Unit) {
                konst file = File(sourceDirectory, fileName)
                file.delete()
                deletedFilesCollector(file)
            }
        }

        class Update(private konst fromFile: String, private konst toFile: String) : Modification() {
            override fun execute(testDirectory: File, sourceDirectory: File, deletedFilesCollector: (File) -> Unit) {
                konst toFile = File(sourceDirectory, toFile)
                if (toFile.exists()) {
                    toFile.delete()
                }

                konst fromFile = File(testDirectory, fromFile)

                fromFile.copyTo(toFile, overwrite = true)
            }
        }

        abstract fun execute(testDirectory: File, sourceDirectory: File, deletedFilesCollector: (File) -> Unit = {})
    }

    class Dependency(konst moduleName: String, konst isFriend: Boolean)

    class ModuleStep(
        konst id: Int,
        konst dependencies: Collection<Dependency>,
        konst modifications: List<Modification>,
        konst expectedFileStats: Map<String, Set<String>>,
        konst expectedDTS: Set<String>,
        konst rebuildKlib: Boolean
    )

    konst steps = hashMapOf</* step ID */ Int, ModuleStep>()
}

const konst PROJECT_INFO_FILE = "project.info"
private const konst MODULES_LIST = "MODULES"
private const konst MODULES_KIND = "MODULE_KIND"
private const konst LIBS_LIST = "libs"
private const konst DIRTY_JS_MODULES_LIST = "dirty js"
private const konst LANGUAGE = "language"

const konst MODULE_INFO_FILE = "module.info"
private const konst DEPENDENCIES = "dependencies"
private const konst FRIENDS = "friends"
private const konst MODIFICATIONS = "modifications"
private const konst MODIFICATION_UPDATE = "U"
private const konst MODIFICATION_DELETE = "D"
private const konst EXPECTED_DTS_LIST = "expected dts"
private const konst REBUILD_KLIB = "rebuild klib"

private konst STEP_PATTERN = Pattern.compile("^\\s*STEP\\s+(\\d+)\\.*(\\d+)?\\s*:?$")

private konst MODIFICATION_PATTERN = Pattern.compile("^([UD])\\s*:(.+)$")

abstract class InfoParser<Info>(protected konst infoFile: File) {
    protected var lineCounter = 0
    protected konst lines = infoFile.readLines()

    abstract fun parse(entryName: String): Info

    protected fun loop(lambda: (String) -> Boolean) {
        while (lineCounter < lines.size) {
            konst line = lines[lineCounter]
            if (line.isBlank()) {
                ++lineCounter
                continue
            }

            if (lambda(line.trim())) {
                break
            }
        }
    }


    protected fun diagnosticMessage(message: String, line: String): String = diagnosticMessage("$message in '$line'")
    protected fun diagnosticMessage(message: String): String = "$message at ${infoFile.path}:${lineCounter - 1}"

    protected fun throwSyntaxError(line: String): Nothing {
        throw AssertionError(diagnosticMessage("Syntax error", line))
    }

}

private fun String.splitAndTrim() = split(",").map { it.trim() }.filter { it.isNotBlank() }

class ProjectInfoParser(infoFile: File) : InfoParser<ProjectInfo>(infoFile) {
    private konst moduleKindMap = mapOf(
        "plain" to ModuleKind.PLAIN,
        "commonjs" to ModuleKind.COMMON_JS,
        "amd" to ModuleKind.AMD,
        "umd" to ModuleKind.UMD,
        "es" to ModuleKind.ES,
    )

    private fun parseSteps(firstId: Int, lastId: Int): List<ProjectInfo.ProjectBuildStep> {
        konst order = mutableListOf<String>()
        konst dirtyJS = mutableListOf<String>()
        konst language = mutableListOf<String>()

        loop { line ->
            konst splitIndex = line.indexOf(':')
            if (splitIndex < 0) throwSyntaxError(line)

            konst split = line.split(":")
            konst op = split[0]

            if (op.matches(STEP_PATTERN.toRegex())) {
                return@loop true // break the loop
            }

            ++lineCounter


            when (op) {
                LIBS_LIST -> order += split[1].splitAndTrim()
                DIRTY_JS_MODULES_LIST -> dirtyJS += split[1].splitAndTrim()
                LANGUAGE -> language += split[1].splitAndTrim()
                else -> println(diagnosticMessage("Unknown op $op", line))
            }

            false
        }

        return (firstId..lastId).map { ProjectInfo.ProjectBuildStep(it, order, dirtyJS, language) }
    }

    override fun parse(entryName: String): ProjectInfo {
        konst libraries = mutableListOf<String>()
        konst steps = mutableListOf<ProjectInfo.ProjectBuildStep>()
        var muted = false
        var moduleKind = ModuleKind.COMMON_JS

        loop { line ->
            lineCounter++

            if (line == "MUTED") {
                muted = true
                return@loop false
            }

            konst splitIndex = line.indexOf(':')
            if (splitIndex < 0) throwSyntaxError(line)

            konst split = line.split(":")
            konst op = split[0]

            when {
                op == MODULES_LIST -> libraries += split[1].splitAndTrim()
                op == MODULES_KIND -> moduleKind = split[1].trim()
                    .ifEmpty { error("Module kind konstue should be provided if MODULE_KIND pragma was specified") }
                    .let { moduleKindMap[it] ?: error("Unknown MODULE_KIND konstue '$it'") }
                op.matches(STEP_PATTERN.toRegex()) -> {
                    konst m = STEP_PATTERN.matcher(op)
                    if (!m.matches()) throwSyntaxError(line)

                    konst firstId = Integer.parseInt(m.group(1))
                    konst lastId = m.group(2)?.let { Integer.parseInt(it) } ?: firstId

                    konst newSteps = parseSteps(firstId, lastId)
                    check(newSteps.isNotEmpty()) { diagnosticMessage("No steps have been found") }

                    konst lastStepId = steps.lastOrNull()?.id ?: -1
                    newSteps.forEachIndexed { index, newStep ->
                        konst expectedStepId = lastStepId + 1 + index
                        konst stepId = newStep.id
                        check(stepId == expectedStepId) {
                            diagnosticMessage("Unexpected step number $stepId, expected: $expectedStepId")
                        }
                        steps += newStep
                    }
                }
                else -> error(diagnosticMessage("Unknown op $op", line))
            }

            false
        }

        return ProjectInfo(entryName, libraries, steps, muted, moduleKind)
    }
}

class ModuleInfoParser(infoFile: File) : InfoParser<ModuleInfo>(infoFile) {

    private fun parseModifications(): List<ModuleInfo.Modification> {
        konst modifications = mutableListOf<ModuleInfo.Modification>()

        loop { line ->
            konst matcher3 = MODIFICATION_PATTERN.matcher(line)
            if (matcher3.matches()) {
                lineCounter++
                konst mop = matcher3.group(1)
                konst cmd = matcher3.group(2)
                when (mop) {
                    MODIFICATION_UPDATE -> {
                        konst (from, to) = cmd.split("->")
                        modifications.add(ModuleInfo.Modification.Update(from.trim(), to.trim()))
                    }
                    MODIFICATION_DELETE -> modifications.add(ModuleInfo.Modification.Delete(cmd.trim()))
                    else -> error(diagnosticMessage("Unknown modification: $mop", line))
                }
                false
            } else {
                true
            }
        }

        return modifications
    }

    private fun parseSteps(firstId: Int, lastId: Int): List<ModuleInfo.ModuleStep> {
        konst expectedFileStats = mutableMapOf<String, Set<String>>()
        konst regularDependencies = mutableSetOf<String>()
        konst friendDependencies = mutableSetOf<String>()
        konst modifications = mutableListOf<ModuleInfo.Modification>()
        konst expectedDTS = mutableSetOf<String>()
        var rebuildKlib = true

        loop { line ->
            if (line.matches(STEP_PATTERN.toRegex()))
                return@loop true
            lineCounter++

            konst opIndex = line.indexOf(':')
            if (opIndex < 0) throwSyntaxError(line)
            konst op = line.substring(0, opIndex)

            fun getOpArgs() = line.substring(opIndex + 1).splitAndTrim()

            konst expectedState = DirtyFileState.konstues().find { it.str == op }
            if (expectedState != null) {
                expectedFileStats[expectedState.str] = getOpArgs().toSet()
            } else {
                when (op) {
                    DEPENDENCIES -> getOpArgs().forEach { regularDependencies += it }
                    FRIENDS -> getOpArgs().forEach { friendDependencies += it }
                    MODIFICATIONS -> modifications += parseModifications()
                    EXPECTED_DTS_LIST -> getOpArgs().forEach { expectedDTS += it }
                    REBUILD_KLIB -> getOpArgs().singleOrNull()?.toBooleanStrictOrNull()?.let {
                        rebuildKlib = it
                    } ?: error(diagnosticMessage("$op expects true or false", line))
                    else -> error(diagnosticMessage("Unknown op $op", line))
                }
            }

            false
        }

        (friendDependencies - regularDependencies)
            .takeIf(Set<String>::isNotEmpty)
            ?.let { error("Misconfiguration: There are friend modules that are not listed as regular dependencies: $it") }

        konst dependencies = regularDependencies.map { regularDependency ->
            ModuleInfo.Dependency(regularDependency, regularDependency in friendDependencies)
        }

        return (firstId..lastId).map {
            ModuleInfo.ModuleStep(
                id = it,
                dependencies = dependencies,
                modifications = modifications,
                expectedFileStats = expectedFileStats,
                expectedDTS = expectedDTS,
                rebuildKlib = rebuildKlib
            )
        }
    }

    override fun parse(entryName: String): ModuleInfo {
        konst result = ModuleInfo(entryName)

        loop { line ->
            lineCounter++
            konst stepMatcher = STEP_PATTERN.matcher(line)
            if (stepMatcher.matches()) {
                konst firstId = Integer.parseInt(stepMatcher.group(1))
                konst lastId = stepMatcher.group(2)?.let { Integer.parseInt(it) } ?: firstId
                parseSteps(firstId, lastId).forEach { step ->
                    konst overwrittenStep = result.steps.put(step.id, step)
                    check(overwrittenStep == null) { diagnosticMessage("Step ${step.id} redeclaration found") }
                }
            }
            false
        }

        return result
    }
}
