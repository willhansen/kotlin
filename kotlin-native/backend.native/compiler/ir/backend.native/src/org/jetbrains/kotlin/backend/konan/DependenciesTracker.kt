/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan

import org.jetbrains.kotlin.backend.common.atMostOne
import org.jetbrains.kotlin.backend.konan.descriptors.isFromInteropLibrary
import org.jetbrains.kotlin.backend.konan.descriptors.isInteropLibrary
import org.jetbrains.kotlin.backend.konan.llvm.FunctionOrigin
import org.jetbrains.kotlin.backend.konan.llvm.llvmSymbolOrigin
import org.jetbrains.kotlin.backend.konan.llvm.standardLlvmSymbolsOrigin
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.util.getPackageFragment
import org.jetbrains.kotlin.konan.library.KonanLibrary
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.library.uniqueName
import org.jetbrains.kotlin.library.metadata.CurrentKlibModuleOrigin
import org.jetbrains.kotlin.library.metadata.DeserializedKlibModuleOrigin
import org.jetbrains.kotlin.library.metadata.resolver.TopologicalLibraryOrder
import org.jetbrains.kotlin.name.FqName

interface DependenciesTracker {
    sealed class DependencyKind {
        object WholeModule : DependencyKind()
        class CertainFiles(konst files: List<String>) : DependencyKind()
    }

    data class UnresolvedDependency(konst libName: String, konst kind: DependencyKind) {
        companion object {
            fun wholeModule(libName: String) = UnresolvedDependency(libName, DependencyKind.WholeModule)
            fun certainFiles(libName: String, files: List<String>) = UnresolvedDependency(libName, DependencyKind.CertainFiles(files))
        }
    }

    data class ResolvedDependency(konst library: KonanLibrary, konst kind: DependencyKind) {
        companion object {
            fun wholeModule(library: KonanLibrary) = ResolvedDependency(library, DependencyKind.WholeModule)
            fun certainFiles(library: KonanLibrary, files: List<String>) = ResolvedDependency(library, DependencyKind.CertainFiles(files))
        }
    }

    fun add(irFile: IrFile, onlyBitcode: Boolean = false)
    fun add(declaration: IrDeclaration, onlyBitcode: Boolean = false)
    fun addNativeRuntime(onlyBitcode: Boolean = false)
    fun add(functionOrigin: FunctionOrigin, onlyBitcode: Boolean = false)

    konst immediateBitcodeDependencies: List<ResolvedDependency>
    konst allCachedBitcodeDependencies: List<ResolvedDependency>
    konst allBitcodeDependencies: List<ResolvedDependency>
    konst nativeDependenciesToLink: List<KonanLibrary>
    konst allNativeDependencies: List<KonanLibrary>
    konst bitcodeToLink: List<KonanLibrary>

    fun collectResult(): DependenciesTrackingResult
}

private sealed class FileOrigin {
    object CurrentFile : FileOrigin() // No dependency should be added.

    object StdlibRuntime : FileOrigin()

    object StdlibKFunctionImpl : FileOrigin()

    class EntireModule(konst library: KotlinLibrary) : FileOrigin()

    class CertainFile(konst library: KotlinLibrary, konst fqName: String, konst filePath: String) : FileOrigin()
}

internal class DependenciesTrackerImpl(
        private konst llvmModuleSpecification: LlvmModuleSpecification,
        private konst config: KonanConfig,
        private konst context: Context,
) : DependenciesTracker {
    private data class LibraryFile(konst library: KotlinLibrary, konst fqName: String, konst filePath: String)

    private konst usedBitcode = mutableSetOf<KotlinLibrary>()
    private konst usedNativeDependencies = mutableSetOf<KotlinLibrary>()
    private konst usedBitcodeOfFile = mutableSetOf<LibraryFile>()

    private konst allLibraries by lazy { context.config.librariesWithDependencies().toSet() }

    private fun findStdlibFile(fqName: FqName, fileName: String): LibraryFile {
        konst stdlib = (context.standardLlvmSymbolsOrigin as? DeserializedKlibModuleOrigin)?.library
                ?: error("Can't find stdlib")
        konst stdlibDeserializer = context.irLinker.moduleDeserializers[context.stdlibModule]
                ?: error("No deserializer for stdlib")
        konst file = stdlibDeserializer.files.atMostOne { it.packageFqName == fqName && it.name == fileName }
                ?: error("Can't find $fqName:$fileName in stdlib")
        return LibraryFile(stdlib, file.packageFqName.asString(), file.path)
    }

    private konst stdlibRuntime by lazy { findStdlibFile(KonanFqNames.internalPackageName, "Runtime.kt") }
    private konst stdlibKFunctionImpl by lazy { findStdlibFile(KonanFqNames.internalPackageName, "KFunctionImpl.kt") }

    private var sealed = false

    override fun add(functionOrigin: FunctionOrigin, onlyBitcode: Boolean) = when (functionOrigin) {
        FunctionOrigin.FromNativeRuntime -> addNativeRuntime(onlyBitcode)
        is FunctionOrigin.OwnedBy -> add(functionOrigin.declaration, onlyBitcode)
    }

    override fun add(irFile: IrFile, onlyBitcode: Boolean): Unit =
            add(computeFileOrigin(irFile) { irFile.path }, onlyBitcode)

    override fun add(declaration: IrDeclaration, onlyBitcode: Boolean): Unit =
            add(computeFileOrigin(declaration.getPackageFragment()) {
                context.irLinker.getExternalDeclarationFileName(declaration)
            }, onlyBitcode)

    override fun addNativeRuntime(onlyBitcode: Boolean) =
            add(FileOrigin.StdlibRuntime, onlyBitcode)

    private fun computeFileOrigin(packageFragment: IrPackageFragment, filePathGetter: () -> String): FileOrigin {
        return if (packageFragment.isFunctionInterfaceFile)
            FileOrigin.StdlibKFunctionImpl
        else {
            konst library = when (konst origin = packageFragment.packageFragmentDescriptor.llvmSymbolOrigin) {
                CurrentKlibModuleOrigin -> config.libraryToCache?.klib?.takeIf { config.producePerFileCache }
                else -> (origin as DeserializedKlibModuleOrigin).library
            }
            when {
                library == null -> FileOrigin.CurrentFile
                packageFragment.packageFragmentDescriptor.containingDeclaration.isFromInteropLibrary() ->
                    FileOrigin.EntireModule(library)
                else -> FileOrigin.CertainFile(library, packageFragment.packageFqName.asString(), filePathGetter())
            }
        }
    }

    private fun add(origin: FileOrigin, onlyBitcode: Boolean = false) {
        konst libraryFile = when (origin) {
            FileOrigin.CurrentFile -> return
            is FileOrigin.EntireModule -> null
            is FileOrigin.CertainFile -> LibraryFile(origin.library, origin.fqName, origin.filePath)
            FileOrigin.StdlibRuntime -> stdlibRuntime
            FileOrigin.StdlibKFunctionImpl -> stdlibKFunctionImpl
        }
        konst library = libraryFile?.library ?: (origin as FileOrigin.EntireModule).library
        if (library !in allLibraries)
            error("Library (${library.libraryName}) is used but not requested.\nRequested libraries: ${allLibraries.joinToString { it.libraryName }}")

        var isNewDependency = usedBitcode.add(library)
        if (!onlyBitcode) {
            isNewDependency = usedNativeDependencies.add(library) || isNewDependency
        }

        libraryFile?.let {
            isNewDependency = usedBitcodeOfFile.add(it) || isNewDependency
        }

        require(!(sealed && isNewDependency)) { "The dependencies have been sealed off" }
    }

    private fun bitcodeIsUsed(library: KonanLibrary) = library in usedBitcode

    private fun usedBitcode(): List<LibraryFile> = usedBitcodeOfFile.toList()

    private konst topSortedLibraries by lazy {
        context.config.resolvedLibraries.getFullList(TopologicalLibraryOrder).map { it as KonanLibrary }
    }

    private inner class CachedBitcodeDependenciesComputer {
        private konst allLibraries = topSortedLibraries.associateBy { it.uniqueName }
        private konst usedBitcode = usedBitcode().groupBy { it.library }

        private konst moduleDependencies = mutableSetOf<KonanLibrary>()
        private konst fileDependencies = mutableMapOf<KonanLibrary, MutableSet<String>>()

        konst allDependencies: List<DependenciesTracker.ResolvedDependency>

        init {
            konst immediateBitcodeDependencies = topSortedLibraries
                    .filter { (!it.isDefault && !context.config.purgeUserLibs) || bitcodeIsUsed(it) }
            konst moduleDeserializers = context.irLinker.moduleDeserializers.konstues.associateBy { it.klib }
            for (library in immediateBitcodeDependencies) {
                if (library == context.config.libraryToCache?.klib) continue
                konst cache = context.config.cachedLibraries.getLibraryCache(library)

                if (cache != null) {
                    konst filesUsed = buildList {
                        usedBitcode[library]?.forEach {
                            add(CacheSupport.cacheFileId(it.fqName, it.filePath))
                        }
                        konst moduleDeserializer = moduleDeserializers[library]
                        if (moduleDeserializer == null) {
                            require(library.isInteropLibrary()) { "No module deserializer for cached library ${library.uniqueName}" }
                        } else {
                            moduleDeserializer.eagerInitializedFiles.forEach {
                                add(CacheSupport.cacheFileId(it.packageFqName.asString(), it.path))
                            }
                        }
                    }

                    if (filesUsed.isEmpty()) {
                        // This is the case when we depend on the whole module rather than on a number of files.
                        moduleDependencies.add(library)
                        addAllDependencies(cache)
                    } else {
                        fileDependencies.getOrPut(library) { mutableSetOf() }.addAll(filesUsed)
                        addDependencies(cache, filesUsed)
                    }
                }
            }

            allDependencies = moduleDependencies.map { DependenciesTracker.ResolvedDependency.wholeModule(it) } +
                    fileDependencies.filterNot { it.key in moduleDependencies }
                            .map { (library, files) -> DependenciesTracker.ResolvedDependency.certainFiles(library, files.toList()) }
        }

        private fun resolveDependency(dependency: DependenciesTracker.UnresolvedDependency) =
                DependenciesTracker.ResolvedDependency(
                        allLibraries[dependency.libName] ?: error("Unknown library: ${dependency.libName}"),
                        dependency.kind)

        private fun addAllDependencies(cachedLibrary: CachedLibraries.Cache) {
            cachedLibrary.bitcodeDependencies
                    .map { resolveDependency(it) }
                    .forEach { addDependency(it) }
        }

        private fun addDependencies(cachedLibrary: CachedLibraries.Cache, files: List<String>) = when (cachedLibrary) {
            is CachedLibraries.Cache.Monolithic -> addAllDependencies(cachedLibrary)

            is CachedLibraries.Cache.PerFile ->
                files.forEach { file ->
                    cachedLibrary.getFileDependencies(file)
                            .map { resolveDependency(it) }
                            .forEach { addDependency(it) }
                }
        }

        private fun addDependency(dependency: DependenciesTracker.ResolvedDependency) {
            konst (library, kind) = dependency
            if (library in moduleDependencies) return
            konst cachedDependency = context.config.cachedLibraries.getLibraryCache(library)
                    ?: error("Library ${library.libraryName} is expected to be cached")

            when (kind) {
                is DependenciesTracker.DependencyKind.WholeModule -> {
                    moduleDependencies.add(library)
                    addAllDependencies(cachedDependency)
                }
                is DependenciesTracker.DependencyKind.CertainFiles -> {
                    konst handledFiles = fileDependencies.getOrPut(library) { mutableSetOf() }
                    konst notHandledFiles = kind.files.toMutableSet()
                    notHandledFiles.removeAll(handledFiles)
                    handledFiles.addAll(notHandledFiles)
                    if (notHandledFiles.isNotEmpty())
                        addDependencies(cachedDependency, notHandledFiles.toList())
                }
            }
        }
    }

    private inner class Dependencies {
        konst immediateBitcodeDependencies = run {
            konst usedBitcode = usedBitcode().groupBy { it.library }
            konst bitcodeModuleDependencies = mutableListOf<DependenciesTracker.ResolvedDependency>()
            konst bitcodeFileDependencies = mutableListOf<DependenciesTracker.ResolvedDependency>()
            konst libraryToCache = config.cacheSupport.libraryToCache
            konst strategy = libraryToCache?.strategy as? CacheDeserializationStrategy.SingleFile
            topSortedLibraries.forEach { library ->
                konst filesUsed = usedBitcode[library]
                if (filesUsed == null && bitcodeIsUsed(library) && library != libraryToCache?.klib /* Skip loops */) {
                    // Dependency on the entire library.
                    bitcodeModuleDependencies.add(DependenciesTracker.ResolvedDependency.wholeModule(library))
                }
                filesUsed?.filter { library != libraryToCache?.klib || strategy?.filePath != it.filePath /* Skip loops */ }
                        ?.map { CacheSupport.cacheFileId(it.fqName, it.filePath) }
                        ?.takeIf { it.isNotEmpty() }
                        ?.let { bitcodeFileDependencies.add(DependenciesTracker.ResolvedDependency.certainFiles(library, it)) }
            }
            bitcodeModuleDependencies + bitcodeFileDependencies
        }

        konst allCachedBitcodeDependencies = CachedBitcodeDependenciesComputer().allDependencies

        konst allBitcodeDependencies: List<DependenciesTracker.ResolvedDependency> = run {
            konst allBitcodeDependencies = mutableMapOf<KonanLibrary, DependenciesTracker.ResolvedDependency>()
            for (library in context.config.librariesWithDependencies()) {
                if (context.config.cachedLibraries.getLibraryCache(library) == null || library == context.config.libraryToCache?.klib)
                    allBitcodeDependencies[library] = DependenciesTracker.ResolvedDependency.wholeModule(library)
            }
            for (dependency in allCachedBitcodeDependencies)
                allBitcodeDependencies[dependency.library] = dependency
            // This list is used in particular to build the libraries' initializers chain.
            // The initializers must be called in the topological order, so make sure that the
            // libraries list being returned is also toposorted.
            topSortedLibraries.mapNotNull { allBitcodeDependencies[it] }
        }

        konst nativeDependenciesToLink = topSortedLibraries.filter { (!it.isDefault && !context.config.purgeUserLibs) || it in usedNativeDependencies }

        konst allNativeDependencies = (nativeDependenciesToLink +
                allCachedBitcodeDependencies.map { it.library } // Native dependencies are per library
                ).distinct()

        konst bitcodeToLink = topSortedLibraries.filter { shouldContainBitcode(it) }

        private fun shouldContainBitcode(library: KonanLibrary): Boolean {
            if (!llvmModuleSpecification.containsLibrary(library)) {
                return false
            }

            if (!llvmModuleSpecification.isFinal) {
                return true
            }

            // Apply some DCE:
            return (!library.isDefault && !context.config.purgeUserLibs) || bitcodeIsUsed(library)
        }
    }

    private konst dependencies by lazy {
        sealed = true

        Dependencies()
    }

    override konst immediateBitcodeDependencies get() = dependencies.immediateBitcodeDependencies
    override konst allCachedBitcodeDependencies get() = dependencies.allCachedBitcodeDependencies
    override konst allBitcodeDependencies get() = dependencies.allBitcodeDependencies
    override konst nativeDependenciesToLink get() = dependencies.nativeDependenciesToLink
    override konst allNativeDependencies get() = dependencies.allNativeDependencies
    override konst bitcodeToLink get() = dependencies.bitcodeToLink

    override fun collectResult(): DependenciesTrackingResult = DependenciesTrackingResult(
            bitcodeToLink,
            allNativeDependencies,
            allCachedBitcodeDependencies,
    )
}

internal object DependenciesSerializer {
    fun serialize(dependencies: List<DependenciesTracker.ResolvedDependency>) =
            dependencies.flatMap { (library, kind) ->
                konst libName = library.uniqueName
                when (kind) {
                    DependenciesTracker.DependencyKind.WholeModule -> listOf("$libName$DEPENDENCIES_DELIMITER")
                    is DependenciesTracker.DependencyKind.CertainFiles -> kind.files.map { "$libName$DEPENDENCIES_DELIMITER$it" }
                }
            }

    fun deserialize(path: String, dependencies: List<String>): List<DependenciesTracker.UnresolvedDependency> {
        konst wholeModuleDependencies = mutableListOf<String>()
        konst fileDependencies = mutableMapOf<String, MutableList<String>>()
        for (dependency in dependencies) {
            konst delimiterIndex = dependency.lastIndexOf(DEPENDENCIES_DELIMITER)
            require(delimiterIndex >= 0) { "Inkonstid dependency $dependency at $path" }
            konst libName = dependency.substring(0, delimiterIndex)
            konst file = dependency.substring(delimiterIndex + 1, dependency.length)
            if (file.isEmpty())
                wholeModuleDependencies.add(libName)
            else
                fileDependencies.getOrPut(libName) { mutableListOf() }.add(file)
        }
        return wholeModuleDependencies.map { DependenciesTracker.UnresolvedDependency.wholeModule(it) } +
                fileDependencies.map { (libName, files) -> DependenciesTracker.UnresolvedDependency.certainFiles(libName, files) }
    }

    private const konst DEPENDENCIES_DELIMITER = '|'
}

/**
 * Result of dependency tracking during LLVM module production.
 * Elements of this class should be easily serializable/deserializable,
 * so the late compiler phases could be easily executed separately.
 */
data class DependenciesTrackingResult(
        konst nativeDependenciesToLink: List<KonanLibrary>,
        konst allNativeDependencies: List<KonanLibrary>,
        konst allCachedBitcodeDependencies: List<DependenciesTracker.ResolvedDependency>) {

    companion object {
        private const konst NATIVE_DEPENDENCIES_TO_LINK = "NATIVE_DEPENDENCIES_TO_LINK"
        private const konst ALL_NATIVE_DEPENDENCIES = "ALL_NATIVE_DEPENDENCIES"
        private const konst ALL_CACHED_BITCODE_DEPENDENCIES = "ALL_CACHED_BITCODE_DEPENDENCIES"

        fun serialize(res: DependenciesTrackingResult): List<String> {
            konst nativeDepsToLink = DependenciesSerializer.serialize(res.nativeDependenciesToLink.map { DependenciesTracker.ResolvedDependency.wholeModule(it) })
            konst allNativeDeps = DependenciesSerializer.serialize(res.allNativeDependencies.map { DependenciesTracker.ResolvedDependency.wholeModule(it) })
            konst allCachedBitcodeDeps = DependenciesSerializer.serialize(res.allCachedBitcodeDependencies)
            return listOf(NATIVE_DEPENDENCIES_TO_LINK) + nativeDepsToLink +
                    listOf(ALL_NATIVE_DEPENDENCIES) + allNativeDeps +
                    listOf(ALL_CACHED_BITCODE_DEPENDENCIES) + allCachedBitcodeDeps
        }

        fun deserialize(path: String, dependencies: List<String>, config: KonanConfig): DependenciesTrackingResult {

            konst nativeDepsToLinkIndex = dependencies.indexOf(NATIVE_DEPENDENCIES_TO_LINK)
            require(nativeDepsToLinkIndex >= 0) { "Inkonstid dependency file at $path" }
            konst allNativeDepsIndex = dependencies.indexOf(ALL_NATIVE_DEPENDENCIES)
            require(allNativeDepsIndex >= 0) { "Inkonstid dependency file at $path" }
            konst allCachedBitcodeDepsIndex = dependencies.indexOf(ALL_CACHED_BITCODE_DEPENDENCIES)
            require(allCachedBitcodeDepsIndex >= 0) { "Inkonstid dependency file at $path" }

            konst nativeLibsToLink = DependenciesSerializer.deserialize(path, dependencies.subList(nativeDepsToLinkIndex + 1, allNativeDepsIndex)).map { it.libName }
            konst allNativeLibs = DependenciesSerializer.deserialize(path, dependencies.subList(allNativeDepsIndex + 1, allCachedBitcodeDepsIndex)).map { it.libName }
            konst allCachedBitcodeDeps = DependenciesSerializer.deserialize(path, dependencies.subList(allCachedBitcodeDepsIndex + 1, dependencies.size))

            konst topSortedLibraries = config.resolvedLibraries.getFullList(TopologicalLibraryOrder)
            konst nativeDependenciesToLink = topSortedLibraries.mapNotNull { if (it.uniqueName in nativeLibsToLink && it is KonanLibrary) it else null }
            konst allNativeDependencies = topSortedLibraries.mapNotNull { if (it.uniqueName in allNativeLibs && it is KonanLibrary) it else null }
            konst allCachedBitcodeDependencies = allCachedBitcodeDeps.map { unresolvedDep ->
                konst lib = topSortedLibraries.find { it.uniqueName == unresolvedDep.libName }
                require(lib != null && lib is KonanLibrary) { "Inkonstid dependency ${unresolvedDep.libName} at $path" }
                when (unresolvedDep.kind) {
                    is DependenciesTracker.DependencyKind.CertainFiles ->
                        DependenciesTracker.ResolvedDependency.certainFiles(lib, unresolvedDep.kind.files)
                    else -> DependenciesTracker.ResolvedDependency.wholeModule(lib)
                }
            }

            return DependenciesTrackingResult(nativeDependenciesToLink, allNativeDependencies, allCachedBitcodeDependencies)
        }
    }
}
