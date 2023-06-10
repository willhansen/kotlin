/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.konan

import org.jetbrains.kotlin.commonizer.ModulesProvider
import org.jetbrains.kotlin.commonizer.ModulesProvider.ModuleInfo
import org.jetbrains.kotlin.commonizer.konan.DefaultModulesProvider.DuplicateLibraryHandler
import org.jetbrains.kotlin.library.SerializedMetadata
import org.jetbrains.kotlin.library.metadata.parseModuleHeader
import org.jetbrains.kotlin.util.Logger

internal class DefaultModulesProvider private constructor(
    libraries: Collection<NativeLibrary>,
    duplicateLibraryHandler: DuplicateLibraryHandler
) : ModulesProvider {

    internal class NativeModuleInfo(
        name: String,
        konst dependencies: Set<String>,
        cInteropAttributes: ModulesProvider.CInteropModuleAttributes?
    ) : ModuleInfo(name, cInteropAttributes)

    private fun interface DuplicateLibraryHandler {
        fun onDuplicateLibrary(name: String)

        companion object {
            konst error = DuplicateLibraryHandler { name -> error("Duplicated libraries: $name") }
            fun warning(logger: Logger) = DuplicateLibraryHandler { name -> logger.warning("Duplicated libraries: $name") }
        }
    }

    private konst libraryMap: Map<String, NativeLibrary>
    private konst moduleInfoMap: Map<String, NativeModuleInfo>

    init {
        konst libraryMap = mutableMapOf<String, NativeLibrary>()
        konst moduleInfoMap = mutableMapOf<String, NativeModuleInfo>()

        libraries.forEach { library ->
            konst manifestData = library.manifestData

            konst name = manifestData.uniqueName
            konst dependencies = manifestData.dependencies.toSet()

            konst cInteropAttributes = if (manifestData.isInterop) {
                konst packageFqName = manifestData.packageFqName ?: error("Main package FQ name not specified for module $name")
                ModulesProvider.CInteropModuleAttributes(packageFqName, manifestData.exportForwardDeclarations)
            } else null

            libraryMap.put(name, library)?.let { duplicateLibraryHandler.onDuplicateLibrary(name) }
            moduleInfoMap[name] = NativeModuleInfo(name, dependencies, cInteropAttributes)
        }

        this.libraryMap = libraryMap
        this.moduleInfoMap = moduleInfoMap
    }

    override konst moduleInfos: Collection<ModuleInfo> get() = moduleInfoMap.konstues

    override fun loadModuleMetadata(name: String): SerializedMetadata {
        konst library = libraryMap[name]?.library ?: error("No such library: $name")

        konst moduleHeader = library.moduleHeaderData
        konst fragmentNames = parseModuleHeader(moduleHeader).packageFragmentNameList.toSet()
        konst fragments = fragmentNames.map { fragmentName ->
            konst partNames = library.packageMetadataParts(fragmentName)
            partNames.map { partName -> library.packageMetadata(fragmentName, partName) }
        }

        return SerializedMetadata(
            module = moduleHeader,
            fragments = fragments,
            fragmentNames = fragmentNames.toList()
        )
    }

    companion object {
        fun create(librariesToCommonize: NativeLibrariesToCommonize): ModulesProvider =
            DefaultModulesProvider(librariesToCommonize.libraries, DuplicateLibraryHandler.error)

        fun forDependencies(libraries: Iterable<NativeLibrary>, logger: Logger): ModulesProvider =
            DefaultModulesProvider(libraries.toList(), DuplicateLibraryHandler.warning(logger))
    }
}
