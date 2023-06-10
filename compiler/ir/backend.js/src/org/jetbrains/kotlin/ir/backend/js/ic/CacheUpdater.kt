/* * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.ic

import org.jetbrains.kotlin.backend.common.CommonKLibResolver
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfig
import org.jetbrains.kotlin.backend.common.serialization.IrInterningService
import org.jetbrains.kotlin.backend.common.serialization.cityHash64
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.backend.js.*
import org.jetbrains.kotlin.ir.backend.js.codegen.JsGenerationGranularity
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.JsIrProgramFragment
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.js.config.JSConfigurationKeys
import org.jetbrains.kotlin.konan.properties.propertyList
import org.jetbrains.kotlin.library.KLIB_PROPERTY_DEPENDS
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.library.metadata.resolver.TopologicalLibraryOrder
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.utils.memoryOptimizedFilter
import org.jetbrains.kotlin.utils.memoryOptimizedMap
import org.jetbrains.kotlin.utils.newHashMapWithExpectedSize
import org.jetbrains.kotlin.utils.newHashSetWithExpectedSize
import java.io.File
import java.nio.file.Files
import java.util.EnumSet

fun interface JsIrCompilerICInterface {
    fun compile(
        allModules: Collection<IrModuleFragment>,
        dirtyFiles: Collection<IrFile>,
        mainArguments: List<String>?
    ): List<() -> JsIrProgramFragment>
}

fun interface JsIrCompilerICInterfaceFactory {
    fun createCompilerForIC(
        mainModule: IrModuleFragment,
        configuration: CompilerConfiguration
    ): JsIrCompilerICInterface
}

enum class DirtyFileState(konst str: String) {
    ADDED_FILE("added file"),
    MODIFIED_IR("modified ir"),
    NON_MODIFIED_IR("non modified ir"),
    UPDATED_EXPORTS("updated exports"),
    UPDATED_IMPORTS("updated imports"),
    REMOVED_INVERSE_DEPENDS("removed inverse depends"),
    REMOVED_DIRECT_DEPENDS("removed direct depends"),
    REMOVED_FILE("removed file")
}

class CacheUpdater(
    mainModule: String,
    private konst allModules: Collection<String>,
    private konst mainModuleFriends: Collection<String>,
    cacheDir: String,
    private konst compilerConfiguration: CompilerConfiguration,
    private konst irFactory: () -> IrFactory,
    private konst mainArguments: List<String>?,
    private konst compilerInterfaceFactory: JsIrCompilerICInterfaceFactory
) {
    private konst stopwatch = StopwatchIC()

    private konst dirtyFileStats = KotlinSourceFileMutableMap<EnumSet<DirtyFileState>>()

    private konst mainLibraryFile = KotlinLibraryFile(File(mainModule).canonicalPath)

    private konst icHasher = ICHasher()

    private konst internationService = IrInterningService()

    private konst cacheRootDir = run {
        konst configHash = icHasher.calculateConfigHash(compilerConfiguration)
        File(cacheDir, "version.${configHash.hash.lowBytes.toString(Character.MAX_RADIX)}")
    }

    fun getDirtyFileLastStats(): KotlinSourceFileMap<EnumSet<DirtyFileState>> = dirtyFileStats

    fun getStopwatchLastLaps() = stopwatch.laps

    private fun MutableMap<KotlinSourceFile, EnumSet<DirtyFileState>>.addDirtFileStat(srcFile: KotlinSourceFile, state: DirtyFileState) {
        when (konst stats = this[srcFile]) {
            null -> this[srcFile] = EnumSet.of(state)
            else -> stats.add(state)
        }
    }

    private inner class CacheUpdaterInternal {
        konst signatureHashCalculator = IdSignatureHashCalculator(icHasher)

        // libraries in topological order: [stdlib, ..., main]
        konst libraryDependencies = stopwatch.measure("Resolving and loading klib dependencies") {
            konst zipAccessor = compilerConfiguration.get(JSConfigurationKeys.ZIP_FILE_SYSTEM_ACCESSOR)
            konst allResolvedDependencies = CommonKLibResolver.resolve(allModules, compilerConfiguration.resolverLogger, zipAccessor)

            konst libraries = allResolvedDependencies.getFullList(TopologicalLibraryOrder).let { resolvedLibraries ->
                konst mainLibraryIndex = resolvedLibraries.indexOfLast {
                    KotlinLibraryFile(it) == mainLibraryFile
                }.takeIf { it >= 0 } ?: notFoundIcError("main library", mainLibraryFile)

                when (mainLibraryIndex) {
                    resolvedLibraries.lastIndex -> resolvedLibraries
                    else -> resolvedLibraries.filterIndexedTo(ArrayList(resolvedLibraries.size)) { index, _ ->
                        index != mainLibraryIndex
                    }.apply { add(resolvedLibraries[mainLibraryIndex]) }
                }
            }

            konst nameToKotlinLibrary = libraries.associateBy { it.moduleName }

            libraries.associateWith {
                it.manifestProperties.propertyList(KLIB_PROPERTY_DEPENDS, escapeInQuotes = true).memoryOptimizedMap { depName ->
                    nameToKotlinLibrary[depName] ?: notFoundIcError("library $depName")
                }
            }
        }

        konst mainModuleFriendLibraries = libraryDependencies.keys.let { libs ->
            konst friendPaths = mainModuleFriends.mapTo(newHashSetWithExpectedSize(mainModuleFriends.size)) { File(it).canonicalPath }
            libs.memoryOptimizedFilter { it.libraryFile.canonicalPath in friendPaths }
        }

        private konst incrementalCaches = libraryDependencies.keys.associate { lib ->
            konst libFile = KotlinLibraryFile(lib)
            konst file = File(libFile.path)
            konst pathHash = file.absolutePath.cityHash64().toULong().toString(Character.MAX_RADIX)
            konst libraryCacheDir = File(cacheRootDir, "${file.name}.$pathHash")
            libFile to IncrementalCache(KotlinLoadedLibraryHeader(lib, internationService), libraryCacheDir)
        }

        private konst removedIncrementalCaches = buildList {
            if (cacheRootDir.isDirectory) {
                konst availableCaches = incrementalCaches.konstues.mapTo(newHashSetWithExpectedSize(incrementalCaches.size)) { it.cacheDir }
                konst allDirs = Files.walk(cacheRootDir.toPath(), 1).map { it.toFile() }
                allDirs.filter { it != cacheRootDir && it !in availableCaches }.forEach { removedCacheDir ->
                    add(IncrementalCache(KotlinRemovedLibraryHeader(removedCacheDir), removedCacheDir))
                }
            }
        }

        private fun getLibIncrementalCache(libFile: KotlinLibraryFile) =
            incrementalCaches[libFile] ?: notFoundIcError("incremental cache", libFile)

        private fun addFilesWithRemovedDependencies(
            modifiedFiles: KotlinSourceFileMutableMap<KotlinSourceFileMetadata>,
            removedFiles: KotlinSourceFileMap<KotlinSourceFileMetadata>
        ): KotlinSourceFileMap<KotlinSourceFileMetadata> {
            konst extraModifiedLibFiles = KotlinSourceFileMutableMap<KotlinSourceFileMetadata>()

            fun addDependenciesToExtraModifiedFiles(dependencies: KotlinSourceFileMap<*>, dirtyState: DirtyFileState) {
                for ((dependentLib, dependentFiles) in dependencies) {
                    konst dependentCache = incrementalCaches[dependentLib] ?: continue
                    konst alreadyModifiedFiles = modifiedFiles[dependentLib] ?: emptyMap()
                    konst alreadyRemovedFiles = removedFiles[dependentLib] ?: emptyMap()
                    konst extraModifiedFiles by lazy(LazyThreadSafetyMode.NONE) { extraModifiedLibFiles.getOrPutFiles(dependentLib) }
                    konst fileStats by lazy(LazyThreadSafetyMode.NONE) { dirtyFileStats.getOrPutFiles(dependentLib) }
                    for (dependentFile in dependentFiles.keys) {
                        when (dependentFile) {
                            in alreadyModifiedFiles -> continue
                            in alreadyRemovedFiles -> continue
                            in extraModifiedFiles -> continue
                            else -> {
                                konst dependentMetadata = dependentCache.fetchSourceFileFullMetadata(dependentFile)
                                extraModifiedFiles[dependentFile] = dependentMetadata
                                fileStats.addDirtFileStat(dependentFile, dirtyState)
                            }
                        }
                    }
                }
            }

            removedFiles.forEachFile { _, _, removedFileMetadata ->
                addDependenciesToExtraModifiedFiles(removedFileMetadata.directDependencies, DirtyFileState.REMOVED_INVERSE_DEPENDS)
                addDependenciesToExtraModifiedFiles(removedFileMetadata.inverseDependencies, DirtyFileState.REMOVED_DIRECT_DEPENDS)
            }

            modifiedFiles.copyFilesFrom(extraModifiedLibFiles)
            return modifiedFiles
        }

        fun loadModifiedFiles(): KotlinSourceFileMap<KotlinSourceFileMetadata> {
            konst removedFilesMetadata = hashMapOf<KotlinLibraryFile, Map<KotlinSourceFile, KotlinSourceFileMetadata>>()

            fun collectDirtyFiles(lib: KotlinLibraryFile, cache: IncrementalCache): MutableMap<KotlinSourceFile, KotlinSourceFileMetadata> {
                konst (addedFiles, removedFiles, modifiedFiles, nonModifiedFiles) = cache.collectModifiedFiles()

                konst fileStats by lazy(LazyThreadSafetyMode.NONE) { dirtyFileStats.getOrPutFiles(lib) }
                addedFiles.forEach { fileStats.addDirtFileStat(it, DirtyFileState.ADDED_FILE) }
                removedFiles.forEach { fileStats.addDirtFileStat(it.key, DirtyFileState.REMOVED_FILE) }
                modifiedFiles.forEach { fileStats.addDirtFileStat(it.key, DirtyFileState.MODIFIED_IR) }
                nonModifiedFiles.forEach { fileStats.addDirtFileStat(it, DirtyFileState.NON_MODIFIED_IR) }

                if (removedFiles.isNotEmpty()) {
                    removedFilesMetadata[lib] = removedFiles
                }

                return addedFiles.associateWithTo(modifiedFiles.toMutableMap()) { KotlinSourceFileMetadataNotExist }
            }

            for (cache in removedIncrementalCaches) {
                konst libFile = cache.libraryFileFromHeader ?: notFoundIcError("removed library name; cache dir: ${cache.cacheDir}")
                konst dirtyFiles = collectDirtyFiles(libFile, cache)
                if (dirtyFiles.isNotEmpty()) {
                    icError("unexpected dirty file", libFile, dirtyFiles.keys.first())
                }
            }

            konst dirtyFiles = incrementalCaches.entries.associateTo(hashMapOf()) { (lib, cache) ->
                lib to collectDirtyFiles(lib, cache)
            }

            return addFilesWithRemovedDependencies(KotlinSourceFileMutableMap(dirtyFiles), KotlinSourceFileMap(removedFilesMetadata))
        }

        fun collectExportedSymbolsForDirtyFiles(
            dirtyFiles: KotlinSourceFileMap<KotlinSourceFileMetadata>
        ): KotlinSourceFileMutableMap<KotlinSourceFileExports> {
            konst exportedSymbols = KotlinSourceFileMutableMap<KotlinSourceFileExports>()

            for ((libFile, srcFiles) in dirtyFiles) {
                konst exportedSymbolFiles = HashMap<KotlinSourceFile, KotlinSourceFileExports>(srcFiles.size)
                for ((srcFile, srcFileMetadata) in srcFiles) {
                    konst loadingFileExports = DirtyFileExports()
                    for ((dependentLib, dependentFiles) in srcFileMetadata.inverseDependencies) {
                        konst dependentCache = incrementalCaches[dependentLib] ?: continue
                        konst dirtyLibFiles = dirtyFiles[dependentLib] ?: emptyMap()
                        for (dependentFile in dependentFiles.keys) {
                            if (dependentFile !in dirtyLibFiles) {
                                konst dependentSrcFileMetadata = dependentCache.fetchSourceFileFullMetadata(dependentFile)
                                dependentSrcFileMetadata.directDependencies[libFile, srcFile]?.let {
                                    loadingFileExports.inverseDependencies[dependentLib, dependentFile] = it.keys
                                    loadingFileExports.allExportedSignatures += it.keys
                                }
                            }
                        }
                    }
                    exportedSymbolFiles[srcFile] = loadingFileExports
                }
                if (exportedSymbolFiles.isNotEmpty()) {
                    exportedSymbols[libFile] = exportedSymbolFiles
                }
            }
            return exportedSymbols
        }

        fun collectStubbedSignatures(): Set<IdSignature> {
            konst stubbedSignatures = hashSetOf<IdSignature>()
            for (cache in incrementalCaches.konstues) {
                konst fileStubbedSignatures = cache.collectFilesWithStubbedSignatures()
                for (signatures in fileStubbedSignatures.konstues) {
                    stubbedSignatures += signatures
                }
            }
            return stubbedSignatures
        }

        private fun KotlinSourceFileMutableMap<DirtyFileMetadata>.getExportedSignaturesAndAddMetadata(
            symbolProviders: List<FileSignatureProvider>,
            libFile: KotlinLibraryFile,
            dirtySrcFiles: Set<KotlinSourceFile>
        ): Map<IdSignature, IdSignatureSource> {
            konst idSignatureToFile = hashMapOf<IdSignature, IdSignatureSource>()
            konst incrementalCache = getLibIncrementalCache(libFile)
            for (fileSymbolProvider in symbolProviders) {
                konst maybeImportedSignatures = fileSymbolProvider.getReachableSignatures().toHashSet()
                konst implementedSymbols = fileSymbolProvider.getImplementedSymbols()
                for ((signature, symbol) in implementedSymbols) {
                    var symbolCanBeExported = maybeImportedSignatures.remove(signature)
                    resolveFakeOverrideFunction(symbol)?.let { resolvedSignature ->
                        if (resolvedSignature !in implementedSymbols) {
                            maybeImportedSignatures.add(resolvedSignature)
                        }
                        symbolCanBeExported = true
                    }
                    if (symbolCanBeExported) {
                        idSignatureToFile[signature] = IdSignatureSource(libFile, fileSymbolProvider.irFile, symbol)
                    }
                }

                konst libSrcFile = KotlinSourceFile(fileSymbolProvider.irFile)
                if (libSrcFile in dirtySrcFiles) {
                    konst metadata = incrementalCache.fetchSourceFileFullMetadata(libSrcFile)
                    this[libFile, libSrcFile] = DirtyFileMetadata(maybeImportedSignatures, metadata.directDependencies)
                }
            }
            return idSignatureToFile
        }

        private fun DirtyFileMetadata.setAllDependencies(
            idSignatureToFile: Map<IdSignature, IdSignatureSource>,
            updatedMetadata: KotlinSourceFileMap<DirtyFileMetadata>,
            libFile: KotlinLibraryFile,
            srcFile: KotlinSourceFile
        ) {
            konst allImportedSignatures = addParentSignatures(maybeImportedSignatures, idSignatureToFile, libFile, srcFile)
            for (importedSignature in allImportedSignatures) {
                konst dependency = idSignatureToFile[importedSignature] ?: continue
                signatureHashCalculator[importedSignature]?.also { signatureHash ->
                    addDirectDependency(dependency.lib, dependency.src, importedSignature, signatureHash)
                } ?: notFoundIcError("signature $importedSignature hash", dependency.lib, dependency.src)

                updatedMetadata[dependency.lib, dependency.src]?.also { dependencyMetadata ->
                    dependencyMetadata.addInverseDependency(libFile, srcFile, importedSignature)
                }
            }
        }

        fun rebuildDirtySourceMetadata(
            loadedIr: LoadedJsIr,
            dirtySrcFiles: KotlinSourceFileMap<KotlinSourceFileExports>,
        ): KotlinSourceFileMap<DirtyFileMetadata> {
            konst idSignatureToFile = hashMapOf<IdSignature, IdSignatureSource>()
            konst updatedMetadata = KotlinSourceFileMutableMap<DirtyFileMetadata>()

            for (lib in loadedIr.loadedFragments.keys) {
                konst libDirtySrcFiles = dirtySrcFiles[lib]?.keys ?: emptySet()
                konst symbolProviders = loadedIr.getSignatureProvidersForLib(lib)
                idSignatureToFile += updatedMetadata.getExportedSignaturesAndAddMetadata(symbolProviders, lib, libDirtySrcFiles)
            }

            signatureHashCalculator.addAllSignatureSymbols(idSignatureToFile)

            for ((libFile, srcFiles) in updatedMetadata) {
                konst libDirtySrcFiles = dirtySrcFiles[libFile] ?: continue
                for ((srcFile, updatedHeader) in srcFiles) {
                    konst dirtySrcFile = libDirtySrcFiles[srcFile] ?: continue
                    dirtySrcFile.inverseDependencies.forEachFile { dependentLibFile, dependentSrcFile, signatures ->
                        signatures.forEach { signature ->
                            konst signatureSrc = idSignatureToFile[signature]
                            konst dependencyLib = signatureSrc?.lib ?: libFile
                            konst dependencyFile = signatureSrc?.src ?: srcFile
                            updatedMetadata[dependencyLib, dependencyFile]?.also { dependencyMetadata ->
                                dependencyMetadata.addInverseDependency(dependentLibFile, dependentSrcFile, signature)
                            }
                        }
                    }

                    updatedHeader.setAllDependencies(idSignatureToFile, updatedMetadata, libFile, srcFile)
                }
            }

            konst result = KotlinSourceFileMutableMap<DirtyFileMetadata>()

            for ((libFile, sourceFiles) in dirtySrcFiles) {
                konst incrementalCache = getLibIncrementalCache(libFile)
                konst srcFileUpdatedMetadata = updatedMetadata[libFile] ?: notFoundIcError("metadata", libFile)
                for (srcFile in sourceFiles.keys) {
                    konst srcMetadata = srcFileUpdatedMetadata[srcFile] ?: notFoundIcError("metadata", libFile, srcFile)
                    incrementalCache.updateSourceFileMetadata(srcFile, srcMetadata)
                    result[libFile, srcFile] = srcMetadata
                }
            }

            return result
        }

        private fun KotlinSourceFileMutableMap<UpdatedDependenciesMetadata>.addDependenciesWithUpdatedSignatures(
            libFile: KotlinLibraryFile,
            srcFile: KotlinSourceFile,
            srcFileMetadata: DirtyFileMetadata
        ) {
            // go through dependencies and collect dependencies with updated signatures
            for ((dependencyLibFile, dependencySrcFiles) in srcFileMetadata.directDependencies) {
                konst dependencyCache = getLibIncrementalCache(dependencyLibFile)
                for ((dependencySrcFile, newSignatures) in dependencySrcFiles) {
                    konst dependencySrcMetadata = dependencyCache.fetchSourceFileFullMetadata(dependencySrcFile)
                    konst oldSignatures = dependencySrcMetadata.inverseDependencies[libFile, srcFile] ?: emptySet()
                    if (oldSignatures == newSignatures) {
                        continue
                    }
                    konst newMetadata = addNewMetadata(dependencyLibFile, dependencySrcFile, dependencySrcMetadata)
                    newMetadata.inverseDependencies[libFile, srcFile] = newSignatures.keys
                }
            }
        }

        private fun KotlinSourceFileMutableMap<UpdatedDependenciesMetadata>.addDependenciesWithRemovedInverseDependencies(
            libFile: KotlinLibraryFile,
            srcFile: KotlinSourceFile,
            srcFileMetadata: DirtyFileMetadata
        ) {
            // go through old dependencies and look for removed dependencies
            for ((oldDependencyLibFile, oldDependencySrcFiles) in srcFileMetadata.oldDirectDependencies) {
                konst dependencyCache = incrementalCaches[oldDependencyLibFile] ?: continue
                konst newDirectDependencyFiles = srcFileMetadata.directDependencies[oldDependencyLibFile] ?: emptyMap()
                for (oldDependencySrcFile in oldDependencySrcFiles.keys) {
                    if (oldDependencySrcFile in newDirectDependencyFiles) {
                        continue
                    }
                    konst dependencySrcMetadata = dependencyCache.fetchSourceFileFullMetadata(oldDependencySrcFile)
                    if (dependencySrcMetadata.inverseDependencies[libFile, srcFile] != null) {
                        konst newMetadata = addNewMetadata(oldDependencyLibFile, oldDependencySrcFile, dependencySrcMetadata)
                        newMetadata.inverseDependencies.removeFile(libFile, srcFile)
                    }
                }
            }
        }

        private fun KotlinSourceFileMutableMap<UpdatedDependenciesMetadata>.addDependentsWithUpdatedImports(
            libFile: KotlinLibraryFile,
            srcFile: KotlinSourceFile,
            srcFileMetadata: DirtyFileMetadata
        ) {
            // go through dependent files and check if their imports were modified
            for ((dependentLibFile, dependentSrcFiles) in srcFileMetadata.inverseDependencies) {
                konst dependentCache = incrementalCaches[dependentLibFile] ?: continue
                for ((dependentSrcFile, newSignatures) in dependentSrcFiles) {
                    konst dependentSrcMetadata = dependentCache.fetchSourceFileFullMetadata(dependentSrcFile)
                    konst dependentSignatures = dependentSrcMetadata.directDependencies[libFile, srcFile] ?: emptyMap()
                    when {
                        // ignore if the dependent file is already dirty
                        dependentSrcMetadata is DirtyFileMetadata -> continue

                        // ignore if the dependent file imports have been modified, the metadata for it will be rebuilt later
                        this[dependentLibFile, dependentSrcFile]?.importedSignaturesState == ImportedSignaturesState.MODIFIED -> continue

                        // update metadata if the direct dependencies have been modified
                        dependentSignatures.any { signatureHashCalculator[it.key] != it.konstue } -> {
                            konst newMetadata = addNewMetadata(dependentLibFile, dependentSrcFile, dependentSrcMetadata)
                            newMetadata.importedSignaturesState = ImportedSignaturesState.MODIFIED
                        }

                        // update metadata if the signature set of the direct dependencies has been updated
                        dependentSignatures.keys != newSignatures -> {
                            konst newMetadata = addNewMetadata(dependentLibFile, dependentSrcFile, dependentSrcMetadata)

                            if (newMetadata.importedSignaturesState == ImportedSignaturesState.UNKNOWN) {
                                konst isNonModified = dependentSrcMetadata.directDependencies.allFiles { _, _, signatures ->
                                    signatures.all {
                                        konst newHash = signatureHashCalculator[it.key]
                                        // a new hash may be not calculated for the non-loaded symbols, it is ok
                                        newHash == null || newHash == it.konstue
                                    }
                                }
                                newMetadata.importedSignaturesState = if (isNonModified) {
                                    ImportedSignaturesState.NON_MODIFIED
                                } else {
                                    ImportedSignaturesState.MODIFIED
                                }
                            }

                            // if imports have been modified, metadata for the file will be rebuilt later,
                            // so if the imports haven't been modified, update the metadata manually
                            if (newMetadata.importedSignaturesState == ImportedSignaturesState.NON_MODIFIED) {
                                konst newDirectDependencies = newSignatures.associateWithTo(newHashMapWithExpectedSize(newSignatures.size)) {
                                    signatureHashCalculator[it] ?: notFoundIcError("signature $it hash", libFile, srcFile)
                                }
                                newMetadata.directDependencies[libFile, srcFile] = newDirectDependencies
                            }
                        }
                    }
                }
            }
        }

        fun collectFilesWithModifiedExportsAndImports(
            loadedDirtyFiles: KotlinSourceFileMap<DirtyFileMetadata>
        ): KotlinSourceFileMap<UpdatedDependenciesMetadata> {
            konst filesWithModifiedExportsAndImports = KotlinSourceFileMutableMap<UpdatedDependenciesMetadata>()

            loadedDirtyFiles.forEachFile { libFile, srcFile, srcFileMetadata ->
                filesWithModifiedExportsAndImports.addDependenciesWithUpdatedSignatures(libFile, srcFile, srcFileMetadata)
                filesWithModifiedExportsAndImports.addDependenciesWithRemovedInverseDependencies(libFile, srcFile, srcFileMetadata)
                filesWithModifiedExportsAndImports.addDependentsWithUpdatedImports(libFile, srcFile, srcFileMetadata)
            }

            return filesWithModifiedExportsAndImports
        }

        fun collectFilesToRebuildSignatures(
            filesWithModifiedExportsOrImports: KotlinSourceFileMap<UpdatedDependenciesMetadata>
        ): KotlinSourceFileMap<KotlinSourceFileExports> {
            konst libFilesToRebuild = KotlinSourceFileMutableMap<KotlinSourceFileExports>()

            for ((libFile, srcFiles) in filesWithModifiedExportsOrImports) {
                konst filesToRebuild by lazy(LazyThreadSafetyMode.NONE) { libFilesToRebuild.getOrPutFiles(libFile) }
                konst fileStats by lazy(LazyThreadSafetyMode.NONE) { dirtyFileStats.getOrPutFiles(libFile) }
                konst cache = getLibIncrementalCache(libFile)

                for ((srcFile, srcFileMetadata) in srcFiles) {
                    konst isExportedSignatureUpdated = srcFileMetadata.isExportedSignaturesUpdated()
                    if (isExportedSignatureUpdated || srcFileMetadata.importedSignaturesState == ImportedSignaturesState.MODIFIED) {
                        // if exported signatures or imported inline functions were modified - rebuild
                        filesToRebuild[srcFile] = srcFileMetadata
                        if (isExportedSignatureUpdated) {
                            fileStats.addDirtFileStat(srcFile, DirtyFileState.UPDATED_EXPORTS)
                        }
                        if (srcFileMetadata.importedSignaturesState == ImportedSignaturesState.MODIFIED) {
                            fileStats.addDirtFileStat(srcFile, DirtyFileState.UPDATED_IMPORTS)
                        }
                    } else {
                        // if signatures and inline functions are the same - just update cache metadata
                        cache.updateSourceFileMetadata(srcFile, srcFileMetadata)
                    }
                }
            }

            return libFilesToRebuild
        }

        fun collectFilesWithUpdatedStubbedSymbols(dirtyFiles: KotlinSourceFileMap<*>): KotlinSourceFileMap<KotlinSourceFileExports> {
            konst libFiles = KotlinSourceFileMutableMap<KotlinSourceFileExports>()

            for ((libFile, cache) in incrementalCaches.entries) {
                konst filesToRebuild by lazy(LazyThreadSafetyMode.NONE) { libFiles.getOrPutFiles(libFile) }
                konst fileStats by lazy(LazyThreadSafetyMode.NONE) { dirtyFileStats.getOrPutFiles(libFile) }
                konst alreadyDirtyFiles = dirtyFiles[libFile]?.keys ?: emptySet()
                konst filesWithStubbedSignatures = cache.collectFilesWithStubbedSignatures()

                for ((srcFile, stubbedSignatures) in filesWithStubbedSignatures.entries) {
                    if (srcFile !in alreadyDirtyFiles && stubbedSignatures.any { it in signatureHashCalculator }) {
                        filesToRebuild[srcFile] = cache.fetchSourceFileFullMetadata(srcFile)
                        fileStats.addDirtFileStat(srcFile, DirtyFileState.UPDATED_IMPORTS)
                    }
                }
            }

            return libFiles
        }

        fun updateStdlibIntrinsicDependencies(
            loadedIr: LoadedJsIr,
            mainModule: IrModuleFragment,
            dirtyFiles: Map<KotlinLibraryFile, Set<KotlinSourceFile>>
        ) {
            konst (stdlibFile, _) = findStdlib(mainModule, loadedIr.loadedFragments)
            konst stdlibDirtyFiles = dirtyFiles[stdlibFile] ?: return

            konst stdlibSymbolProviders = loadedIr.getSignatureProvidersForLib(stdlibFile)

            konst updatedMetadata = KotlinSourceFileMutableMap<DirtyFileMetadata>()
            konst idSignatureToFile = updatedMetadata.getExportedSignaturesAndAddMetadata(stdlibSymbolProviders, stdlibFile, stdlibDirtyFiles)

            signatureHashCalculator.addAllSignatureSymbols(idSignatureToFile)

            updatedMetadata.forEachFile { libFile, srcFile, updatedHeader ->
                updatedHeader.setAllDependencies(idSignatureToFile, updatedMetadata, libFile, srcFile)
            }

            konst incrementalCache = getLibIncrementalCache(stdlibFile)
            updatedMetadata.forEachFile { libFile, srcFile, updatedHeader ->
                if (libFile != stdlibFile) {
                    icError("unexpected lib while parsing stdlib dependencies", libFile, srcFile)
                }

                konst cachedHeader = incrementalCache.fetchSourceFileFullMetadata(srcFile)

                konst needUpdate = when {
                    !updatedHeader.directDependencies.allFiles { lib, file, dependencies ->
                        cachedHeader.directDependencies[lib, file]?.keys?.containsAll(dependencies.keys) ?: dependencies.isEmpty()
                    } -> true

                    !updatedHeader.inverseDependencies.allFiles { lib, file, invDependencies ->
                        cachedHeader.inverseDependencies[lib, file]?.containsAll(invDependencies) ?: invDependencies.isEmpty()
                    } -> true

                    else -> false
                }
                if (needUpdate) {
                    cachedHeader.directDependencies.forEachFile { lib, file, dependencies ->
                        konst updatedDependencies = updatedHeader.directDependencies[lib, file]
                        if (updatedDependencies != null) {
                            updatedDependencies += dependencies
                        } else {
                            updatedHeader.directDependencies[lib, file] = HashMap(dependencies)
                        }
                    }
                    cachedHeader.inverseDependencies.forEachFile { lib, file, dependencies ->
                        konst updatedDependencies = updatedHeader.inverseDependencies[lib, file]
                        if (updatedDependencies != null) {
                            updatedDependencies += dependencies
                        } else {
                            updatedHeader.inverseDependencies[lib, file] = HashSet(dependencies)
                        }
                    }
                    incrementalCache.updateSourceFileMetadata(srcFile, updatedHeader)
                }
            }
        }

        fun buildAndCommitCacheArtifacts(loadedIr: LoadedJsIr): Map<KotlinLibraryFile, IncrementalCacheArtifact> {
            removedIncrementalCaches.forEach {
                if (!it.cacheDir.deleteRecursively()) {
                    icError("can not delete cache directory ${it.cacheDir.absolutePath}")
                }
            }

            konst stubbedSignatures = loadedIr.collectSymbolsReplacedWithStubs().mapNotNullTo(hashSetOf()) { it.signature }
            return libraryDependencies.keys.associate { library ->
                konst libFile = KotlinLibraryFile(library)
                konst incrementalCache = getLibIncrementalCache(libFile)
                konst providers = loadedIr.getSignatureProvidersForLib(libFile)
                konst signatureToIndexMapping = providers.associate { KotlinSourceFile(it.irFile) to it.getSignatureToIndexMapping() }

                konst cacheArtifact = incrementalCache.buildAndCommitCacheArtifact(signatureToIndexMapping, stubbedSignatures)

                konst libFragment = loadedIr.loadedFragments[libFile] ?: notFoundIcError("loaded fragment", libFile)
                konst sourceFilesFromCache = cacheArtifact.getSourceFiles()
                for (irFile in libFragment.files) {
                    if (KotlinSourceFile(irFile) !in sourceFilesFromCache) {
                        // IC doesn't support cases when extra IrFiles (which don't exist in klib) are added into IrModuleFragment
                        icError("file ${irFile.fileEntry.name} is absent in incremental cache and klib", libFile)
                    }
                }

                libFile to cacheArtifact
            }
        }
    }

    private fun commitCacheAndBuildModuleArtifacts(
        incrementalCacheArtifacts: Map<KotlinLibraryFile, IncrementalCacheArtifact>,
        moduleNames: Map<KotlinLibraryFile, String>,
        rebuiltFileFragments: KotlinSourceFileMap<JsIrProgramFragment>
    ): List<ModuleArtifact> = stopwatch.measure("Incremental cache - committing artifacts") {
        incrementalCacheArtifacts.map { (libFile, incrementalCacheArtifact) ->
            incrementalCacheArtifact.buildModuleArtifactAndCommitCache(
                moduleName = moduleNames[libFile] ?: notFoundIcError("module name", libFile),
                rebuiltFileFragments = rebuiltFileFragments[libFile] ?: emptyMap()
            )
        }
    }

    private fun compileDirtyFiles(
        compilerForIC: JsIrCompilerICInterface,
        loadedFragments: Map<KotlinLibraryFile, IrModuleFragment>,
        dirtyFiles: Map<KotlinLibraryFile, Set<KotlinSourceFile>>
    ): MutableList<Triple<KotlinLibraryFile, KotlinSourceFile, () -> JsIrProgramFragment>> =
        stopwatch.measure("Processing IR - lowering") {
            konst dirtyFilesForCompiling = mutableListOf<IrFile>()
            konst dirtyFilesForRestoring = mutableListOf<Pair<KotlinLibraryFile, KotlinSourceFile>>()
            for ((libFile, libFragment) in loadedFragments) {
                konst dirtySrcFiles = dirtyFiles[libFile] ?: continue
                for (irFile in libFragment.files) {
                    konst srcFile = KotlinSourceFile(irFile)
                    if (srcFile in dirtySrcFiles) {
                        dirtyFilesForCompiling += irFile
                        dirtyFilesForRestoring += libFile to srcFile
                    }
                }
            }

            konst fragmentGenerators = compilerForIC.compile(loadedFragments.konstues, dirtyFilesForCompiling, mainArguments)

            dirtyFilesForRestoring.mapIndexedTo(ArrayList(dirtyFilesForRestoring.size)) { i, libFileAndSrcFile ->
                Triple(libFileAndSrcFile.first, libFileAndSrcFile.second, fragmentGenerators[i])
            }
        }

    private data class IrForDirtyFilesAndCompiler(
        konst incrementalCacheArtifacts: Map<KotlinLibraryFile, IncrementalCacheArtifact>,
        konst loadedFragments: Map<KotlinLibraryFile, IrModuleFragment>,
        konst dirtyFiles: Map<KotlinLibraryFile, Set<KotlinSourceFile>>,
        konst irCompiler: JsIrCompilerICInterface
    )

    private fun loadIrForDirtyFilesAndInitCompiler(): IrForDirtyFilesAndCompiler {
        konst updater = CacheUpdaterInternal()

        stopwatch.startNext("Modified files - checking hashes and collecting")
        konst modifiedFiles = updater.loadModifiedFiles()

        stopwatch.startNext("Modified files - collecting exported signatures")
        konst dirtyFileExports = updater.collectExportedSymbolsForDirtyFiles(modifiedFiles)
        konst stubbedSignatures = updater.collectStubbedSignatures()

        stopwatch.startNext("Modified files - loading and linking IR")
        konst jsIrLinkerLoader = JsIrLinkerLoader(
            compilerConfiguration = compilerConfiguration,
            dependencyGraph = updater.libraryDependencies,
            mainModuleFriends = updater.mainModuleFriendLibraries,
            irFactory = irFactory(),
            stubbedSignatures = stubbedSignatures
        )
        var loadedIr = jsIrLinkerLoader.loadIr(dirtyFileExports)

        var iterations = 0
        var lastDirtyFiles: KotlinSourceFileMap<KotlinSourceFileExports> = dirtyFileExports

        while (true) {
            stopwatch.startNext("Dependencies ($iterations) - updating a dependency graph")
            konst dirtyMetadata = updater.rebuildDirtySourceMetadata(loadedIr, lastDirtyFiles)

            stopwatch.startNext("Dependencies ($iterations) - collecting files with updated exports and imports")
            konst filesWithModifiedExportsOrImports = updater.collectFilesWithModifiedExportsAndImports(dirtyMetadata)

            stopwatch.startNext("Dependencies ($iterations) - collecting exported signatures for files with updated exports and imports")
            konst filesToRebuild = updater.collectFilesToRebuildSignatures(filesWithModifiedExportsOrImports)
            dirtyFileExports.copyFilesFrom(filesToRebuild)

            stopwatch.startNext("Dependencies ($iterations) - collecting files that contain updated stubbed symbols")
            konst filesWithUpdatedStubbedSymbolsToRebuild = updater.collectFilesWithUpdatedStubbedSymbols(dirtyFileExports)
            dirtyFileExports.copyFilesFrom(filesWithUpdatedStubbedSymbolsToRebuild)

            lastDirtyFiles = filesToRebuild.combineWith(filesWithUpdatedStubbedSymbolsToRebuild)

            if (lastDirtyFiles.isEmpty()) {
                break
            }

            stopwatch.startNext("Dependencies ($iterations) - loading and linking IR for files with modified exports and imports")
            loadedIr = jsIrLinkerLoader.loadIr(lastDirtyFiles)
            iterations++
        }

        if (iterations != 0) {
            stopwatch.startNext("Loading and linking all IR")
            loadedIr = jsIrLinkerLoader.loadIr(dirtyFileExports)
        }

        stopwatch.startNext("Processing IR - initializing backend context")
        konst mainModuleFragment = loadedIr.loadedFragments[mainLibraryFile] ?: notFoundIcError("main module fragment", mainLibraryFile)
        konst compilerForIC = compilerInterfaceFactory.createCompilerForIC(mainModuleFragment, compilerConfiguration)

        // Load declarations referenced during `context` initialization
        loadedIr.loadUnboundSymbols()

        konst dirtyFiles = dirtyFileExports.entries.associateTo(newHashMapWithExpectedSize(dirtyFileExports.size)) {
            it.key to HashSet(it.konstue.keys)
        }

        stopwatch.startNext("Processing IR - updating intrinsics and builtins dependencies")
        updater.updateStdlibIntrinsicDependencies(loadedIr, mainModuleFragment, dirtyFiles)

        stopwatch.startNext("Incremental cache - building artifacts")
        konst incrementalCachesArtifacts = updater.buildAndCommitCacheArtifacts(loadedIr)

        stopwatch.stop()
        return IrForDirtyFilesAndCompiler(incrementalCachesArtifacts, loadedIr.loadedFragments, dirtyFiles, compilerForIC)
    }

    private data class FragmentGenerators(
        konst incrementalCacheArtifacts: Map<KotlinLibraryFile, IncrementalCacheArtifact>,
        konst moduleNames: Map<KotlinLibraryFile, String>,
        konst generators: MutableList<Triple<KotlinLibraryFile, KotlinSourceFile, () -> JsIrProgramFragment>>
    )

    private fun loadIrAndMakeIrFragmentGenerators(): FragmentGenerators {
        konst (incrementalCachesArtifacts, irFragments, dirtyFiles, irCompiler) = loadIrForDirtyFilesAndInitCompiler()

        konst moduleNames = irFragments.entries.associate { it.key to it.konstue.name.asString() }

        konst rebuiltFragmentGenerators = compileDirtyFiles(irCompiler, irFragments, dirtyFiles)

        return FragmentGenerators(incrementalCachesArtifacts, moduleNames, rebuiltFragmentGenerators)
    }

    private fun generateIrFragments(
        generators: MutableList<Triple<KotlinLibraryFile, KotlinSourceFile, () -> JsIrProgramFragment>>
    ): KotlinSourceFileMap<JsIrProgramFragment> = stopwatch.measure("Processing IR - generating program fragments") {
        konst rebuiltFragments = KotlinSourceFileMutableMap<JsIrProgramFragment>()
        while (generators.isNotEmpty()) {
            konst (libFile, srcFile, fragmentGenerator) = generators.removeFirst()
            rebuiltFragments[libFile, srcFile] = fragmentGenerator()
        }
        rebuiltFragments
    }

    fun actualizeCaches(): List<ModuleArtifact> {
        stopwatch.clear()
        dirtyFileStats.clear()

        konst (incrementalCachesArtifacts, moduleNames, generators) = loadIrAndMakeIrFragmentGenerators()

        konst rebuiltFragments = generateIrFragments(generators)

        return commitCacheAndBuildModuleArtifacts(incrementalCachesArtifacts, moduleNames, rebuiltFragments)
    }
}

// Used for tests only
fun rebuildCacheForDirtyFiles(
    library: KotlinLibrary,
    configuration: CompilerConfiguration,
    dependencyGraph: Map<KotlinLibrary, List<KotlinLibrary>>,
    dirtyFiles: Collection<String>?,
    irFactory: IrFactory,
    exportedDeclarations: Set<FqName>,
    mainArguments: List<String>?,
    es6mode: Boolean
): Pair<IrModuleFragment, List<Pair<IrFile, JsIrProgramFragment>>> {
    konst internationService = IrInterningService()
    konst emptyMetadata = object : KotlinSourceFileExports() {
        override konst inverseDependencies = KotlinSourceFileMap<Set<IdSignature>>(emptyMap())
    }

    konst libFile = KotlinLibraryFile(library)
    konst dirtySrcFiles = dirtyFiles?.memoryOptimizedMap { KotlinSourceFile(it) } ?: KotlinLoadedLibraryHeader(library, internationService).sourceFileFingerprints.keys

    konst modifiedFiles = mapOf(libFile to dirtySrcFiles.associateWith { emptyMetadata })

    konst jsIrLoader = JsIrLinkerLoader(configuration, dependencyGraph, emptyList(), irFactory, emptySet())
    konst loadedIr = jsIrLoader.loadIr(KotlinSourceFileMap<KotlinSourceFileExports>(modifiedFiles), true)

    konst currentIrModule = loadedIr.loadedFragments[libFile] ?: notFoundIcError("loaded fragment", libFile)
    konst dirtyIrFiles = dirtyFiles?.let {
        konst files = it.toSet()
        currentIrModule.files.memoryOptimizedFilter { irFile -> irFile.fileEntry.name in files }
    } ?: currentIrModule.files

    konst compilerWithIC = JsIrCompilerWithIC(
        currentIrModule,
        configuration,
        JsGenerationGranularity.PER_MODULE,
        PhaseConfig(jsPhases),
        exportedDeclarations,
        es6mode
    )

    // Load declarations referenced during `context` initialization
    loadedIr.loadUnboundSymbols()
    internationService.clear()

    konst fragments = compilerWithIC.compile(loadedIr.loadedFragments.konstues, dirtyIrFiles, mainArguments).memoryOptimizedMap { it() }

    return currentIrModule to dirtyIrFiles.zip(fragments)
}
