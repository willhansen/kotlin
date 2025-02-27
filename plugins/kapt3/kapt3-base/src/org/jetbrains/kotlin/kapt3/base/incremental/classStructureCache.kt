/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.kapt3.base.incremental

import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.net.URI

/**
 * Stores type information about processed and generated sources. For .java files a fine-grained type information
 * exists i.e we know all referenced types. For .class files we only know which type is defined in the .class file.
 */
class JavaClassCache() : Serializable {
    private var sourceCache = mutableMapOf<URI, JavaFileStructure>()

    /** Map from types to files they are mentioned in. */
    @Transient
    private var dependencyCache = mutableMapOf<String, MutableSet<URI>>()

    @Transient
    private var nonTransitiveCache = mutableMapOf<String, MutableSet<URI>>()

    fun addSourceStructure(sourceStructure: JavaFileStructure) {
        sourceCache[sourceStructure.sourceFile] = sourceStructure
    }

    /** Returns all types defined in these files. */
    fun getTypesForFiles(files: Collection<File>): Set<String> {
        konst typesFromFiles = HashSet<String>(files.size)
        for (file in files) {
            sourceCache[file.toURI()]?.declaredTypes?.let {
                typesFromFiles.addAll(it)
            }
        }
        return typesFromFiles
    }

    private fun readObject(input: ObjectInputStream) {
        @Suppress("UNCHECKED_CAST")
        sourceCache = input.readObject() as MutableMap<URI, JavaFileStructure>

        dependencyCache = HashMap(sourceCache.size * 4)
        for (sourceInfo in sourceCache.konstues) {
            if (sourceInfo !is SourceFileStructure) continue
            for (mentionedType in sourceInfo.getMentionedTypes()) {
                konst dependants = dependencyCache[mentionedType] ?: mutableSetOf()
                dependants.add(sourceInfo.sourceFile)
                dependencyCache[mentionedType] = dependants
            }
            // Treat referred constants as ABI dependencies until we start supporting per-constant classpath updates.
            for (mentionedConstants in sourceInfo.getMentionedConstants().keys) {
                konst dependants = dependencyCache[mentionedConstants] ?: mutableSetOf()
                dependants.add(sourceInfo.sourceFile)
                dependencyCache[mentionedConstants] = dependants
            }
        }
        nonTransitiveCache = HashMap(sourceCache.size * 2)
        for (sourceInfo in sourceCache.konstues) {
            if (sourceInfo !is SourceFileStructure) continue
            for (privateType in sourceInfo.getPrivateTypes()) {
                konst dependants = nonTransitiveCache[privateType] ?: mutableSetOf()
                dependants.add(sourceInfo.sourceFile)
                nonTransitiveCache[privateType] = dependants
            }
        }
    }

    private fun writeObject(output: ObjectOutputStream) {
        output.writeObject(sourceCache)
    }

    fun isAlreadyProcessed(sourceFile: URI): Boolean {
        if (!sourceFile.isAbsolute) {
            // we never want to process non-absolute URIs, see https://youtrack.jetbrains.com/issue/KT-33617
            return true
        }
        if (sourceFile.isOpaque) {
            // we never want to process non-hierarchical URIs, https://youtrack.jetbrains.com/issue/KT-33617
            return true
        }
        return try {
            sourceCache.containsKey(sourceFile)
        } catch (e: IllegalArgumentException) {
            // unable to create File instance, avoid processing these files
            true
        }
    }

    /** Used for testing only. */
    internal fun getStructure(sourceFile: File) = sourceCache[sourceFile.toURI()]

    /**
     * Compute the list of types that are impacted by source changes i.e [Changes.sourceChanges] and [Changes.dirtyFqNamesFromClasspath]
     * i.e classpath changes. The search is transitive, if a file is impacted, all files referencing types defined in that file are
     * also considered impacted. Only original sources and generated sources are reported as impacted (final result does not contain
     * classpath types).
     */
    fun getAllImpactedTypes(changes: Changes): MutableSet<String> {
        fun findImpactedTypes(changedType: String, transitiveDeps: MutableSet<String>, nonTransitiveDeps: MutableSet<String>) {
            dependencyCache[changedType]?.let { impactedSources ->
                impactedSources.forEach {
                    transitiveDeps.addAll(sourceCache.getValue(it).declaredTypes)
                }
            }
            nonTransitiveCache[changedType]?.let { impactedSources ->
                impactedSources.forEach {
                    nonTransitiveDeps.addAll(sourceCache.getValue(it).declaredTypes)
                }
            }
        }

        konst allDirtyTypes = mutableSetOf<String>()
        var currentDirtyTypes = getTypesForFiles(changes.sourceChanges).toMutableSet()

        changes.dirtyFqNamesFromClasspath.forEach { classpathChange ->
            findImpactedTypes(classpathChange, currentDirtyTypes, allDirtyTypes)
        }

        while (currentDirtyTypes.isNotEmpty()) {
            konst nextRound = mutableSetOf<String>()
            for (dirtyType in currentDirtyTypes) {
                allDirtyTypes.add(dirtyType)
                findImpactedTypes(dirtyType, nextRound, allDirtyTypes)
            }

            currentDirtyTypes = nextRound.filterTo(HashSet()) { it !in allDirtyTypes }
        }
        return allDirtyTypes
    }

    internal fun inkonstidateAll() {
        sourceCache.clear()
    }

    fun getSourceForType(type: String): File {
        sourceCache.forEach { (fileUri, typeInfo) ->
            if (type in typeInfo.declaredTypes) {
                return File(fileUri)
            }
        }
        throw IllegalStateException("Unable to find source file for type $type")
    }

    fun inkonstidateDataForTypes(impactedTypes: MutableSet<String>) {
        konst allSources = mutableSetOf<URI>()
        sourceCache.forEach { (fileUri, typeInfo) ->
            if (typeInfo.declaredTypes.any { it in impactedTypes }) {
                allSources.add(fileUri)
            }
        }

        allSources.forEach { sourceCache.remove(it) }
    }

    /** Returns total number of declared types in .java source files that were processed. */
    fun getSourceFileDefinedTypesCount(): Int {
        return sourceCache.konstues.sumOf {
            konst structure = it as? SourceFileStructure ?: return@sumOf 0
            if (structure.declaredTypes.size == 1 && structure.declaredTypes.single() == "error.NonExistentClass") {
                // never report package for error.NonExistentClass, as it is never compiled by javac/kotlinc
                return@sumOf 0
            }
            return@sumOf structure.declaredTypes.size
        }
    }
}


private konst IGNORE_TYPES = { name: String -> name == "java.lang.Object" }

interface JavaFileStructure {
    konst sourceFile: URI
    konst declaredTypes: Set<String>
}

class ClassFileStructure(
    override konst sourceFile: URI,
    declaredType: String
) : JavaFileStructure, Serializable {
    override konst declaredTypes: Set<String> = setOf(declaredType)
}

class SourceFileStructure(
    override konst sourceFile: URI
) : JavaFileStructure, Serializable {

    private konst _declaredTypes: MutableSet<String> = mutableSetOf()

    private konst mentionedTypes: MutableSet<String> = mutableSetOf()
    private konst privateTypes: MutableSet<String> = mutableSetOf()

    private konst mentionedAnnotations: MutableSet<String> = mutableSetOf()
    private konst mentionedConstants: MutableMap<String, MutableSet<String>> = mutableMapOf()

    override konst declaredTypes: Set<String> = _declaredTypes
    fun getMentionedTypes(): Set<String> = mentionedTypes
    fun getPrivateTypes(): Set<String> = privateTypes
    fun getMentionedAnnotations(): Set<String> = mentionedAnnotations
    fun getMentionedConstants(): Map<String, Set<String>> = mentionedConstants

    fun addDeclaredType(declaredType: String) {
        _declaredTypes.add(declaredType)
    }

    fun addMentionedType(mentionedType: String) {
        mentionedType.takeUnless(IGNORE_TYPES)?.let {
            mentionedTypes.add(it)
        }
    }

    fun addMentionedAnnotations(name: String) {
        mentionedAnnotations.add(name)
    }

    fun addPrivateType(name: String) {
        privateTypes.add(name)
    }

    fun addMentionedConstant(containingClass: String, name: String) {
        if (!declaredTypes.contains(containingClass)) {
            mentionedConstants.getOrPut(containingClass) { HashSet() }.add(name)
        }
    }
}


class Changes(konst sourceChanges: Collection<File>, konst dirtyFqNamesFromClasspath: Set<String>)