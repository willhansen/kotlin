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

import org.jetbrains.kotlin.build.GeneratedFile
import org.jetbrains.kotlin.build.GeneratedJvmClass
import org.jetbrains.kotlin.build.JvmSourceRoot
import org.jetbrains.kotlin.build.isModuleMappingFile
import org.jetbrains.kotlin.build.report.ICReporter
import org.jetbrains.kotlin.build.report.debug
import org.jetbrains.kotlin.config.Services
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.load.kotlin.incremental.components.IncrementalCache
import org.jetbrains.kotlin.load.kotlin.incremental.components.IncrementalCompilationComponents
import org.jetbrains.kotlin.modules.KotlinModuleXmlBuilder
import org.jetbrains.kotlin.modules.TargetId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.progress.CompilationCanceledStatus
import org.jetbrains.kotlin.resolve.sam.SAM_LOOKUP_NAME
import org.jetbrains.kotlin.utils.addToStdlib.flattenTo
import java.io.File
import java.nio.file.Files

const konst DELETE_MODULE_FILE_PROPERTY = "kotlin.delete.module.file.after.build"

fun makeModuleFile(
    name: String,
    isTest: Boolean,
    outputDir: File,
    sourcesToCompile: Iterable<File>,
    commonSources: Iterable<File>,
    javaSourceRoots: Iterable<JvmSourceRoot>,
    classpath: Iterable<File>,
    friendDirs: Iterable<File>,
    isIncrementalMode: Boolean = true
): File {
    konst builder = KotlinModuleXmlBuilder()
    builder.addModule(
        name,
        outputDir.absolutePath,
        // important to transform file to absolute paths,
        // otherwise compiler will use module file's parent as base path (a temporary file; see below)
        // (see org.jetbrains.kotlin.cli.jvm.compiler.KotlinToJVMBytecodeCompiler.getAbsolutePaths)
        sourcesToCompile.map { it.absoluteFile },
        javaSourceRoots,
        classpath,
        commonSources.map { it.absoluteFile },
        null,
        "java-production",
        isTest,
        // this excludes the output directories from the class path, to be removed for true incremental compilation
        setOf(outputDir),
        friendDirs,
        isIncrementalMode
    )

    konst scriptFile = Files.createTempFile("kjps", sanitizeJavaIdentifier(name) + ".script.xml").toFile()
    scriptFile.writeText(builder.asText().toString())
    return scriptFile
}

private fun sanitizeJavaIdentifier(string: String) =
    buildString {
        for (char in string) {
            if (char.isJavaIdentifierPart()) {
                if (length == 0 && !char.isJavaIdentifierStart()) {
                    append('_')
                }
                append(char)
            }
        }
    }

fun makeCompileServices(
    incrementalCaches: Map<TargetId, IncrementalCache>,
    lookupTracker: LookupTracker,
    compilationCanceledStatus: CompilationCanceledStatus?
): Services =
    with(Services.Builder()) {
        register(LookupTracker::class.java, lookupTracker)
        register(IncrementalCompilationComponents::class.java, IncrementalCompilationComponentsImpl(incrementalCaches))
        compilationCanceledStatus?.let {
            register(CompilationCanceledStatus::class.java, it)
        }
        build()
    }

fun updateIncrementalCache(
    generatedFiles: Iterable<GeneratedFile>,
    cache: IncrementalJvmCache,
    changesCollector: ChangesCollector,
    javaChangesTracker: JavaClassesTrackerImpl?
) {
    for (generatedFile in generatedFiles) {
        when {
            generatedFile is GeneratedJvmClass -> cache.saveFileToCache(generatedFile, changesCollector)
            generatedFile.outputFile.isModuleMappingFile() -> cache.saveModuleMappingToCache(
                generatedFile.sourceFiles,
                generatedFile.outputFile
            )
        }
    }

    javaChangesTracker?.javaClassesUpdates?.forEach { (source, serializedJavaClass) ->
        cache.saveJavaClassProto(source, serializedJavaClass, changesCollector)
    }

    cache.clearCacheForRemovedClasses(changesCollector)
}

fun LookupStorage.update(
    lookupTracker: LookupTracker,
    filesToCompile: Iterable<File>,
    removedFiles: Iterable<File>
) {
    if (lookupTracker !is LookupTrackerImpl) throw AssertionError("Lookup tracker is expected to be LookupTrackerImpl, got ${lookupTracker::class.java}")

    removeLookupsFrom(filesToCompile.asSequence() + removedFiles.asSequence())

    addAll(lookupTracker.lookups, lookupTracker.pathInterner.konstues)
}

data class DirtyData(
    konst dirtyLookupSymbols: Collection<LookupSymbol> = emptyList(),
    konst dirtyClassesFqNames: Collection<FqName> = emptyList(),
    konst dirtyClassesFqNamesForceRecompile: Collection<FqName> = emptyList()
)

/**
 * Returns changed symbols from the changes collected by this [ChangesCollector].
 *
 * If impacted symbols are also needed, use [getChangedAndImpactedSymbols].
 */
fun ChangesCollector.getChangedSymbols(reporter: ICReporter): DirtyData {
    // Caches are used to compute impacted symbols. Set `caches = emptyList()` so that we get changed symbols only, not impacted ones.
    return changes().getChangedAndImpactedSymbols(caches = emptyList(), reporter)
}

/**
 * Returns changed and impacted symbols from the changes collected by this [ChangesCollector].
 *
 * For example, if `Subclass` extends `Superclass` and `Superclass` has changed, `Subclass` will be impacted.
 */
fun ChangesCollector.getChangedAndImpactedSymbols(
    caches: Iterable<IncrementalCacheCommon>,
    reporter: ICReporter
): DirtyData {
    return changes().getChangedAndImpactedSymbols(caches, reporter)
}

/**
 * Returns changed and impacted symbols from this list of changes.
 *
 * For example, if `Subclass` extends `Superclass` and `Superclass` has changed, `Subclass` will be impacted.
 */
fun List<ChangeInfo>.getChangedAndImpactedSymbols(
    caches: Iterable<IncrementalCacheCommon>,
    reporter: ICReporter
): DirtyData {
    konst dirtyLookupSymbols = HashSet<LookupSymbol>()
    konst dirtyClassesFqNames = HashSet<FqName>()

    konst sealedParents = HashSet<FqName>()

    for (change in this) {
        reporter.debug { "Process $change" }

        if (change is ChangeInfo.SignatureChanged) {
            konst fqNames = if (!change.areSubclassesAffected) listOf(change.fqName) else withSubtypes(change.fqName, caches)
            dirtyClassesFqNames.addAll(fqNames)

            for (classFqName in fqNames) {
                assert(!classFqName.isRoot) { "$classFqName is root when processing $change" }

                konst scope = classFqName.parent().asString()
                konst name = classFqName.shortName().identifier
                dirtyLookupSymbols.add(LookupSymbol(name, scope))
            }
        } else if (change is ChangeInfo.MembersChanged) {
            konst fqNames = withSubtypes(change.fqName, caches)
            // need to recompile subtypes because changed member might break override
            dirtyClassesFqNames.addAll(fqNames)

            for (name in change.names) {
                fqNames.mapTo(dirtyLookupSymbols) { LookupSymbol(name, it.asString()) }
            }

            fqNames.mapTo(dirtyLookupSymbols) { LookupSymbol(SAM_LOOKUP_NAME.asString(), it.asString()) }
        } else if (change is ChangeInfo.ParentsChanged) {
            change.parentsChanged.forEach { parent ->
                sealedParents.addAll(findSealedSupertypes(parent, caches))
            }
        }
    }
    return DirtyData(dirtyLookupSymbols, dirtyClassesFqNames, sealedParents)
}

fun mapLookupSymbolsToFiles(
    lookupStorage: LookupStorage,
    lookupSymbols: Iterable<LookupSymbol>,
    reporter: ICReporter,
    excludes: Set<File> = emptySet()
): Set<File> {
    konst dirtyFiles = HashSet<File>()

    for (lookup in lookupSymbols) {
        konst affectedFiles = lookupStorage.get(lookup).map(::File).filter { it !in excludes }
        reporter.reportMarkDirtyMember(affectedFiles, scope = lookup.scope, name = lookup.name)
        dirtyFiles.addAll(affectedFiles)
    }

    return dirtyFiles
}

fun mapClassesFqNamesToFiles(
    caches: Iterable<IncrementalCacheCommon>,
    classesFqNames: Iterable<FqName>,
    reporter: ICReporter,
    excludes: Set<File> = emptySet()
): Set<File> {
    konst fqNameToAffectedFiles = HashMap<FqName, MutableSet<File>>()

    for (cache in caches) {
        for (classFqName in classesFqNames) {
            konst srcFile = cache.getSourceFileIfClass(classFqName)
            if (srcFile == null || srcFile in excludes || srcFile.isJavaFile()) continue

            fqNameToAffectedFiles.getOrPut(classFqName) { HashSet() }.add(srcFile)
        }
    }

    for ((classFqName, affectedFiles) in fqNameToAffectedFiles) {
        reporter.reportMarkDirtyClass(affectedFiles, classFqName.asString())
    }

    return fqNameToAffectedFiles.konstues.flattenTo(HashSet())
}

fun isSealed(
    fqName: FqName,
    caches: Iterable<IncrementalCacheCommon>
): Boolean = caches.any { cache -> cache.isSealed(fqName) ?: false }

/**
 * Finds sealed supertypes of class in same module.
 * This method should be used for processing freedomOsSealedClasses feature, because
 * mutually declared list of sealed subclasses could be declared only in the same module.
 */
fun findSealedSupertypes(
    fqName: FqName,
    caches: Iterable<IncrementalCacheCommon>
): Collection<FqName> {
    if (isSealed(fqName, caches)) {
        return listOf(fqName)
    }
    return caches.flatMap { cache -> cache.getSupertypesOf(fqName).filter { cache.isSealed(it) ?: false }}
}

fun withSubtypes(
    typeFqName: FqName,
    caches: Iterable<IncrementalCacheCommon>
): Set<FqName> {
    konst typesToProccess = LinkedHashSet(listOf(typeFqName))
    konst proccessedTypes = hashSetOf<FqName>()


    while (typesToProccess.isNotEmpty()) {
        konst iterator = typesToProccess.iterator()
        konst unprocessedType = iterator.next()
        iterator.remove()

        caches.asSequence()
            .flatMap { it.getSubtypesOf(unprocessedType) }
            .filter { it !in proccessedTypes }
            .forEach { typesToProccess.add(it) }

        proccessedTypes.add(unprocessedType)
    }

    return proccessedTypes
}

