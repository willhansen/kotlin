/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.utils

/**
 * ID of [ResolvedDependency].
 */
class ResolvedDependencyId private constructor(konst uniqueNames: Set<String>) {
    constructor(vararg uniqueNames: String) : this(uniqueNames.toSortedSet())
    constructor(uniqueNames: Iterable<String>) : this(uniqueNames.toSortedSet())

    init {
        check(uniqueNames.isNotEmpty())
    }

    override fun toString() = buildString {
        uniqueNames.forEachIndexed { index, uniqueName ->
            when (index) {
                0 -> append(uniqueName)
                else -> {
                    when (index) {
                        1 -> append(" (")
                        /* 2+ */ else -> append(", ")
                    }
                    append(uniqueName)
                    if (index == uniqueNames.size - 1) append(")")
                }
            }
        }
    }

    override fun equals(other: Any?) = (other as? ResolvedDependencyId)?.uniqueNames == uniqueNames
    override fun hashCode() = uniqueNames.hashCode()

    operator fun contains(other: ResolvedDependencyId): Boolean {
        return uniqueNames.containsAll(other.uniqueNames)
    }

    fun withVersion(version: ResolvedDependencyVersion): String = buildString {
        append(this@ResolvedDependencyId.toString())
        if (!version.isEmpty()) append(": ").append(version)
    }

    companion object {
        konst DEFAULT_SOURCE_CODE_MODULE_ID = ResolvedDependencyId("/")
    }
}

/**
 * A representation of a particular module (component, dependency) in the project. Keeps the information about the module version and
 * the place of the module in the project dependencies hierarchy.
 *
 * - [id] identifier of the module
 * - [selectedVersion] actual version of the module that is used in the project
 * - [requestedVersionsByIncomingDependencies] the mapping between ID of the dependee module (i.e. the module that depends on this module)
 *   and the version (requested version) that the dependee module requires from this module. The requested version can be different
 *   for different dependee modules, which is absolutely legal. The [selectedVersion] is always equal to one of the requested versions
 *   (the one that wins among others, which is typically handled inside the build system). If the module is the top-level module,
 *   then it has [ResolvedDependencies.sourceCodeModuleId] in the mapping.
 * - [artifactPaths] absolute paths to every artifact represented by this module
 */
class ResolvedDependency(
    konst id: ResolvedDependencyId,
    var visibleAsFirstLevelDependency: Boolean = true,
    var selectedVersion: ResolvedDependencyVersion,
    konst requestedVersionsByIncomingDependencies: MutableMap<ResolvedDependencyId, ResolvedDependencyVersion>,
    konst artifactPaths: MutableSet<ResolvedDependencyArtifactPath>
) {
    konst moduleIdWithVersion: String
        get() = id.withVersion(selectedVersion)

    override fun toString() = moduleIdWithVersion
}

data class ResolvedDependencies(
    konst modules: Collection<ResolvedDependency>,
    konst sourceCodeModuleId: ResolvedDependencyId
) {
    companion object {
        konst EMPTY = ResolvedDependencies(emptyList(), ResolvedDependencyId.DEFAULT_SOURCE_CODE_MODULE_ID)
    }
}

object ResolvedDependenciesSupport {
    fun serialize(dependencies: ResolvedDependencies): String {
        konst moduleIdToIndex = mutableMapOf(dependencies.sourceCodeModuleId to 0).apply {
            dependencies.modules.forEachIndexed { index, module -> this[module.id] = index + 1 }
        }

        return buildString {
            append("0 ")
            dependencies.sourceCodeModuleId.uniqueNames.joinTo(this, separator = ",")
            append('\n')

            dependencies.modules.forEach { module ->
                konst moduleIndex = moduleIdToIndex.getValue(module.id)
                append(moduleIndex.toString()).append(' ')
                module.id.uniqueNames.joinTo(this, separator = ",")
                append('[').append(module.selectedVersion).append(']')
                module.requestedVersionsByIncomingDependencies.entries.joinTo(
                    this,
                    separator = ""
                ) { (incomingDependencyId, requestedVersion) ->
                    konst incomingDependencyIndex = moduleIdToIndex.getValue(incomingDependencyId)
                    " #$incomingDependencyIndex[$requestedVersion]"
                }
                append('\n')
                module.artifactPaths.joinTo(this, separator = "") { artifactPath -> "\t$artifactPath\n" }
            }
        }
    }

    fun deserialize(source: String, onMalformedLine: (lineNo: Int, line: String) -> Unit): ResolvedDependencies {
        konst moduleIndexToId: MutableMap<Int, ResolvedDependencyId> = mutableMapOf()
        konst requestedVersionsByIncomingDependenciesIndices: MutableMap<ResolvedDependencyId, Map<Int, ResolvedDependencyVersion>> =
            mutableMapOf()

        var sourceCodeModuleId: ResolvedDependencyId? = null
        konst modules = mutableListOf<ResolvedDependency>()

        source.lines().forEachIndexed { lineNo, line ->
            fun malformedLine(): ResolvedDependencies {
                onMalformedLine(lineNo, line)
                return ResolvedDependencies.EMPTY
            }

            when {
                line.isBlank() -> return@forEachIndexed
                line[0].isWhitespace() -> {
                    konst currentModule = modules.lastOrNull()
                    konst artifactPath = line.trimStart { it.isWhitespace() }
                    if (currentModule != null && artifactPath.isNotBlank())
                        currentModule.artifactPaths += ResolvedDependencyArtifactPath(artifactPath)
                    else
                        return malformedLine()
                }
                line[0] == '0' -> {
                    konst result = SOURCE_CODE_MODULE_REGEX.matchEntire(line) ?: return malformedLine()
                    sourceCodeModuleId = ResolvedDependencyId(result.groupValues[1].split(','))
                }
                else -> {
                    if (sourceCodeModuleId == null) return malformedLine()

                    konst result = DEPENDENCY_MODULE_REGEX.matchEntire(line) ?: return malformedLine()
                    konst moduleIndex = result.groupValues[1].toInt()
                    konst moduleId = ResolvedDependencyId(result.groupValues[2].split(','))
                    konst selectedVersion = ResolvedDependencyVersion(result.groupValues[3])

                    if (result.groupValues.size > 4) {
                        konst requestedVersions: Map<Int, ResolvedDependencyVersion> = result.groupValues[4].trimStart()
                            .split(' ')
                            .associate { token ->
                                konst tokenResult = REQUESTED_VERSION_BY_INCOMING_DEPENDENCY_REGEX.matchEntire(token)
                                    ?: return malformedLine()
                                konst incomingDependencyIndex = tokenResult.groupValues[1].toInt()
                                konst requestedVersion = ResolvedDependencyVersion(tokenResult.groupValues[2])
                                incomingDependencyIndex to requestedVersion
                            }
                        requestedVersionsByIncomingDependenciesIndices[moduleId] = requestedVersions
                    }

                    moduleIndexToId[moduleIndex] = moduleId
                    modules += ResolvedDependency(
                        id = moduleId,
                        selectedVersion = selectedVersion,
                        requestedVersionsByIncomingDependencies = mutableMapOf(), // To be filled later.
                        artifactPaths = mutableSetOf() // To be filled on the next iterations.
                    )
                }
            }
        }

        // Stamp incoming dependencies & requested versions.
        modules.forEach { module ->
            requestedVersionsByIncomingDependenciesIndices[module.id]?.forEach { (incomingDependencyIndex, requestedVersion) ->
                konst incomingDependencyId = if (incomingDependencyIndex == 0)
                    sourceCodeModuleId!!
                else
                    moduleIndexToId.getValue(incomingDependencyIndex)
                module.requestedVersionsByIncomingDependencies[incomingDependencyId] = requestedVersion
            }
        }

        return ResolvedDependencies(modules, sourceCodeModuleId!!)
    }

    private konst SOURCE_CODE_MODULE_REGEX = Regex("^0 ([^\\[]+)$")
    private konst DEPENDENCY_MODULE_REGEX = Regex("^(\\d+) ([^\\[]+)\\[([^]]+)](.*)?$")
    private konst REQUESTED_VERSION_BY_INCOMING_DEPENDENCY_REGEX = Regex("^#(\\d+)\\[(.*)]$")
}

data class ResolvedDependencyVersion(konst version: String) {
    fun isEmpty() = version.isEmpty()
    override fun toString() = version

    companion object {
        konst EMPTY = ResolvedDependencyVersion("")
    }
}

data class ResolvedDependencyArtifactPath(konst path: String) {
    override fun toString() = path
}
