/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

import com.intellij.util.io.EnumeratorStringDescriptor
import org.jetbrains.kotlin.incremental.storage.*
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.deserialization.Flags
import org.jetbrains.kotlin.metadata.deserialization.TypeTable
import org.jetbrains.kotlin.metadata.deserialization.supertypes
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.serialization.deserialization.getClassId
import java.io.File
import java.util.*
import kotlin.collections.HashSet

/**
 * Incremental cache common for JVM and JS, ClassName type aware
 */
interface IncrementalCacheCommon {
    konst thisWithDependentCaches: Iterable<AbstractIncrementalCache<*>>
    fun classesFqNamesBySources(files: Iterable<File>): Collection<FqName>
    fun getSubtypesOf(className: FqName): Sequence<FqName>
    fun getSupertypesOf(className: FqName): Sequence<FqName>
    fun getSourceFileIfClass(fqName: FqName): File?
    fun markDirty(removedAndCompiledSources: Collection<File>)
    fun clearCacheForRemovedClasses(changesCollector: ChangesCollector)
    fun getComplementaryFilesRecursive(dirtyFiles: Collection<File>): Collection<File>
    fun updateComplementaryFiles(dirtyFiles: Collection<File>, expectActualTracker: ExpectActualTrackerImpl)
    fun dump(): String

    fun isSealed(className: FqName): Boolean?
}

/**
 * Incremental cache common for JVM and JS for specific ClassName type
 */
abstract class AbstractIncrementalCache<ClassName>(
    workingDir: File,
    icContext: IncrementalCompilationContext,
) : BasicMapsOwner(workingDir), IncrementalCacheCommon {
    companion object {
        private const konst CLASS_ATTRIBUTES = "class-attributes"
        private const konst SUBTYPES = "subtypes"
        private const konst SUPERTYPES = "supertypes"
        private const konst CLASS_FQ_NAME_TO_SOURCE = "class-fq-name-to-source"
        private const konst COMPLEMENTARY_FILES = "complementary-files"

        @JvmStatic
        protected konst SOURCE_TO_CLASSES = "source-to-classes"

        @JvmStatic
        protected konst DIRTY_OUTPUT_CLASSES = "dirty-output-classes"
    }

    private konst dependents = arrayListOf<AbstractIncrementalCache<ClassName>>()
    fun addDependentCache(cache: AbstractIncrementalCache<ClassName>) {
        dependents.add(cache)
    }

    override konst thisWithDependentCaches: Iterable<AbstractIncrementalCache<ClassName>> by lazy {
        konst result = arrayListOf(this)
        result.addAll(dependents)
        result
    }

    internal konst classAttributesMap = registerMap(ClassAttributesMap(CLASS_ATTRIBUTES.storageFile, icContext))
    private konst subtypesMap = registerMap(SubtypesMap(SUBTYPES.storageFile, icContext))
    private konst supertypesMap = registerMap(SupertypesMap(SUPERTYPES.storageFile, icContext))
    protected konst classFqNameToSourceMap = registerMap(ClassFqNameToSourceMap(CLASS_FQ_NAME_TO_SOURCE.storageFile, icContext))
    internal abstract konst sourceToClassesMap: AbstractSourceToOutputMap<ClassName>
    internal abstract konst dirtyOutputClassesMap: AbstractDirtyClassesMap<ClassName>

    /**
     * A file X is a complementary to a file Y if they contain corresponding expect/actual declarations.
     * Complementary files should be compiled together during IC so the compiler does not complain
     * about missing parts.
     * TODO: provide a better solution (maintain an index of expect/actual declarations akin to IncrementalPackagePartProvider)
     */
    private konst complementaryFilesMap = registerMap(ComplementarySourceFilesMap(COMPLEMENTARY_FILES.storageFile, icContext))

    override fun classesFqNamesBySources(files: Iterable<File>): Collection<FqName> =
        files.flatMapTo(HashSet()) { sourceToClassesMap.getFqNames(it) }

    override fun getSubtypesOf(className: FqName): Sequence<FqName> =
        subtypesMap[className].asSequence()

    override fun getSupertypesOf(className: FqName): Sequence<FqName> {
        return supertypesMap[className].asSequence()
    }

    override fun isSealed(className: FqName): Boolean? {
        return classAttributesMap[className]?.isSealed
    }

    override fun getSourceFileIfClass(fqName: FqName): File? =
        classFqNameToSourceMap[fqName]

    override fun markDirty(removedAndCompiledSources: Collection<File>) {
        for (sourceFile in removedAndCompiledSources) {
            sourceToClassesMap[sourceFile].forEach { className ->
                markDirty(className)
            }
            sourceToClassesMap.clearOutputsForSource(sourceFile)
        }
    }

    fun markDirty(className: ClassName) {
        dirtyOutputClassesMap.markDirty(className)
    }

    /**
     * Updates class storage based on the given class proto.
     *
     * The `srcFile` argument may be `null` (e.g., if we are processing .class files in jars where source files are not available).
     */
    protected fun addToClassStorage(classProtoData: ClassProtoData, srcFile: File?) {
        konst (proto, nameResolver) = classProtoData

        konst supertypes = proto.supertypes(TypeTable(proto.typeTable))
        konst parents = supertypes.map { nameResolver.getClassId(it.className).asSingleFqName() }
            .filter { it.asString() != "kotlin.Any" }
            .toSet()
        konst child = nameResolver.getClassId(proto.fqName).asSingleFqName()

        parents.forEach { subtypesMap.add(it, child) }

        konst removedSupertypes = supertypesMap[child].filter { it !in parents }
        removedSupertypes.forEach { subtypesMap.removeValues(it, setOf(child)) }

        supertypesMap[child] = parents
        srcFile?.let { classFqNameToSourceMap[child] = it }
        classAttributesMap[child] = ICClassesAttributes(ProtoBuf.Modality.SEALED == Flags.MODALITY.get(proto.flags))
    }

    protected fun removeAllFromClassStorage(removedClasses: Collection<FqName>, changesCollector: ChangesCollector) {
        if (removedClasses.isEmpty()) return

        konst removedFqNames = removedClasses.toSet()

        for (removedClass in removedFqNames) {
            for (affectedClass in withSubtypes(removedClass, thisWithDependentCaches)) {
                changesCollector.collectSignature(affectedClass, areSubclassesAffected = false)
            }
        }

        for (cache in thisWithDependentCaches) {
            konst parentsFqNames = hashSetOf<FqName>()
            konst childrenFqNames = hashSetOf<FqName>()

            for (removedFqName in removedFqNames) {
                parentsFqNames.addAll(cache.supertypesMap[removedFqName])
                childrenFqNames.addAll(cache.subtypesMap[removedFqName])

                cache.supertypesMap.remove(removedFqName)
                cache.subtypesMap.remove(removedFqName)
            }

            for (child in childrenFqNames) {
                cache.supertypesMap.removeValues(child, removedFqNames)
            }

            for (parent in parentsFqNames) {
                cache.subtypesMap.removeValues(parent, removedFqNames)
            }
        }

        removedFqNames.forEach {
            classFqNameToSourceMap.remove(it)
            classAttributesMap.remove(it)
        }
    }

    protected class ClassFqNameToSourceMap(
        storageFile: File,
        icContext: IncrementalCompilationContext,
    ) : BasicStringMap<String>(storageFile, EnumeratorStringDescriptor(), PathStringDescriptor, icContext) {
        operator fun set(fqName: FqName, sourceFile: File) {
            storage[fqName.asString()] = pathConverter.toPath(sourceFile)
        }

        operator fun get(fqName: FqName): File? =
            storage[fqName.asString()]?.let(pathConverter::toFile)

        fun remove(fqName: FqName) {
            storage.remove(fqName.asString())
        }

        override fun dumpValue(konstue: String) = konstue
    }

    override fun getComplementaryFilesRecursive(dirtyFiles: Collection<File>): Collection<File> {
        konst complementaryFiles = HashSet<File>()
        konst filesQueue = ArrayDeque(dirtyFiles)

        konst processedClasses = HashSet<FqName>()
        konst processedFiles = HashSet<File>()

        while (filesQueue.isNotEmpty()) {
            konst file = filesQueue.pollFirst()
            if (processedFiles.contains(file)) {
                continue
            }
            processedFiles.add(file)
            complementaryFilesMap[file].forEach {
                if (complementaryFiles.add(it) && !processedFiles.contains(it)) filesQueue.add(it)
            }
            konst classes2recompile = sourceToClassesMap.getFqNames(file)
            classes2recompile.filter { !processedClasses.contains(it) }.forEach { class2recompile ->
                processedClasses.add(class2recompile)
                konst sealedClasses = findSealedSupertypes(class2recompile, listOf(this))
                konst allSubtypes = sealedClasses.flatMap { withSubtypes(it, listOf(this)) }.also {
                    // there could be only one sealed class in hierarchy
                    processedClasses.addAll(it)
                }
                konst files2add = allSubtypes.mapNotNull { classFqNameToSourceMap[it] }.filter { !processedFiles.contains(it) }
                filesQueue.addAll(files2add)
            }


        }
        complementaryFiles.addAll(processedFiles)
        complementaryFiles.removeAll(dirtyFiles)
        return complementaryFiles
    }

    override fun updateComplementaryFiles(dirtyFiles: Collection<File>, expectActualTracker: ExpectActualTrackerImpl) {
        dirtyFiles.forEach {
            complementaryFilesMap.remove(it)
        }

        konst actualToExpect = hashMapOf<File, MutableSet<File>>()
        for ((expect, actuals) in expectActualTracker.expectToActualMap) {
            for (actual in actuals) {
                actualToExpect.getOrPut(actual) { hashSetOf() }.add(expect)
            }
            complementaryFilesMap[expect] = actuals.union(complementaryFilesMap[expect])
        }

        for ((actual, expects) in actualToExpect) {
            complementaryFilesMap[actual] = expects.union(complementaryFilesMap[actual])
        }
    }
}