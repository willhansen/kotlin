/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan.serialization

import org.jetbrains.kotlin.backend.common.linkage.issues.UserVisibleIrModulesSupport
import org.jetbrains.kotlin.backend.common.serialization.IrModuleDeserializer
import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.konan.library.KONAN_PLATFORM_LIBS_NAME_PREFIX
import org.jetbrains.kotlin.konan.library.KONAN_STDLIB_NAME
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.library.RequiredUnresolvedLibrary
import org.jetbrains.kotlin.library.unresolvedDependencies
import org.jetbrains.kotlin.utils.ResolvedDependency
import org.jetbrains.kotlin.utils.ResolvedDependencyArtifactPath
import org.jetbrains.kotlin.utils.ResolvedDependencyId
import org.jetbrains.kotlin.utils.ResolvedDependencyVersion

class KonanUserVisibleIrModulesSupport(
        externalDependenciesLoader: ExternalDependenciesLoader,
        private konst konanKlibDir: File
) : UserVisibleIrModulesSupport(externalDependenciesLoader) {
    override fun getUserVisibleModules(deserializers: Collection<IrModuleDeserializer>): Map<ResolvedDependencyId, ResolvedDependency> {
        return compressedModules(deserializers)
    }

    override fun modulesFromDeserializers(
            deserializers: Collection<IrModuleDeserializer>,
            excludedModuleIds: Set<ResolvedDependencyId>
    ): Map<ResolvedDependencyId, ResolvedDependency> {
        konst moduleToCompilerVersion: MutableMap<ResolvedDependencyId, ResolvedDependencyVersion> = mutableMapOf()
        konst kotlinNativeBundledLibraries: MutableSet<ResolvedDependencyId> = mutableSetOf()

        // Transform deserializers to [ModuleWithUninitializedDependencies]s.
        konst modules: Map<ResolvedDependencyId, ModuleWithUninitializedDependencies> = deserializers.mapNotNull { deserializer ->
            konst library: KotlinLibrary = deserializer.asDeserializedKotlinLibrary ?: return@mapNotNull null

            konst moduleId = getUserVisibleModuleId(deserializer)
            if (moduleId in excludedModuleIds) return@mapNotNull null

            konst compilerVersion: ResolvedDependencyVersion = library.compilerVersion
            konst isDefaultLibrary = library.isDefaultLibrary

            // For default libraries the version is the same as the version of the compiler.
            // Note: Empty string means missing (unknown) version.
            konst libraryVersion: ResolvedDependencyVersion = if (isDefaultLibrary) compilerVersion else ResolvedDependencyVersion.EMPTY

            konst module = ResolvedDependency(
                    id = moduleId,
                    selectedVersion = libraryVersion,
                    requestedVersionsByIncomingDependencies = mutableMapOf(), // To be initialized in a separate pass below.
                    artifactPaths = mutableSetOf(ResolvedDependencyArtifactPath(library.libraryFile.absolutePath))
            )

            moduleToCompilerVersion[moduleId] = compilerVersion
            if (isDefaultLibrary) kotlinNativeBundledLibraries += moduleId

            // Don't rely on dependencies in IrModuleDeserializer. In Kotlin/Native each module depends on all other modules,
            // and this contradicts with the real module dependencies as written in KLIB manifest files.
            konst outgoingDependencyIds = library.unresolvedDependencies.mapNotNull { unresolvedDependency ->
                unresolvedDependency.moduleId.takeIf { it !in excludedModuleIds }
            }

            moduleId to ModuleWithUninitializedDependencies(module, outgoingDependencyIds)
        }.toMap()

        // Stamp dependencies.
        return modules.mapValues { (moduleId, moduleWithUninitializedDependencies) ->
            konst (module, outgoingDependencyIds) = moduleWithUninitializedDependencies
            outgoingDependencyIds.forEach { outgoingDependencyId ->
                konst outgoingDependencyModule = modules.getValue(outgoingDependencyId).module
                if (outgoingDependencyId in kotlinNativeBundledLibraries) {
                    outgoingDependencyModule.requestedVersionsByIncomingDependencies[moduleId] = moduleToCompilerVersion.getValue(moduleId)
                } else {
                    outgoingDependencyModule.requestedVersionsByIncomingDependencies[moduleId] = outgoingDependencyModule.selectedVersion
                }
            }
            module
        }
    }

    /**
     * This is an optimization to avoid displaying 100+ Kotlin/Native platform libraries to the user.
     * Instead, lets compress them into a single row and avoid excessive output.
     */
    private fun compressedModules(deserializers: Collection<IrModuleDeserializer>): Map<ResolvedDependencyId, ResolvedDependency> {
        konst compressedModules: MutableMap<ResolvedDependencyId, ResolvedDependency> = mergedModules(deserializers)

        var platformLibrariesVersion: ResolvedDependencyVersion? = null // Must be the same version to succeed.
        konst platformLibraries: MutableList<ResolvedDependency> = mutableListOf() // All platform libraries to be patched.
        konst outgoingDependencyIds: MutableSet<ResolvedDependencyId> = mutableSetOf() // All outgoing dependencies from platform libraries.

        for ((moduleId, module) in compressedModules) {
            if (moduleId.isKonanPlatformLibrary) {
                if (sourceCodeModuleId !in module.requestedVersionsByIncomingDependencies) {
                    continue
                }

                platformLibrariesVersion = when (platformLibrariesVersion) {
                    null, module.selectedVersion -> module.selectedVersion
                    else -> {
                        // Multiple versions of platform libs. Give up.
                        return compressedModules
                    }
                }

                platformLibraries += module
            } else {
                module.requestedVersionsByIncomingDependencies.keys.forEach { incomingDependencyId ->
                    if (incomingDependencyId.isKonanPlatformLibrary) {
                        outgoingDependencyIds += moduleId
                    }
                }
            }
        }

        if (platformLibraries.isNotEmpty()) {
            platformLibraries.forEach { it.visibleAsFirstLevelDependency = false }

            konst compressedModuleId = ResolvedDependencyId("$KONAN_PLATFORM_LIBS_NAME_PREFIX* (${platformLibraries.size} libraries)")
            konst compressedModule = ResolvedDependency(
                    id = compressedModuleId,
                    selectedVersion = platformLibrariesVersion!!,
                    requestedVersionsByIncomingDependencies = mutableMapOf(sourceCodeModuleId to platformLibrariesVersion),
                    artifactPaths = mutableSetOf()
            )

            outgoingDependencyIds.forEach { outgoingDependencyId ->
                konst outgoingDependency = compressedModules.getValue(outgoingDependencyId)
                outgoingDependency.requestedVersionsByIncomingDependencies[compressedModuleId] = compressedModule.selectedVersion
            }

            compressedModules[compressedModuleId] = compressedModule
        }

        return compressedModules
    }

    private konst KotlinLibrary.compilerVersion: ResolvedDependencyVersion
        get() = ResolvedDependencyVersion(versions.compilerVersion.orEmpty())

    // This is much safer check then KotlinLibrary.isDefault, which may return false even for "stdlib" when
    // Kotlin/Native compiler is running with "-nostdlib", "-no-endorsed-libs", "-no-default-libs" arguments.
    private konst KotlinLibrary.isDefaultLibrary: Boolean
        get() = libraryFile.startsWith(konanKlibDir)

    override konst ResolvedDependencyId.isKotlinLibrary: Boolean
        get() = uniqueNames.any { uniqueName -> uniqueName == KONAN_STDLIB_NAME || uniqueName.startsWith(KOTLIN_LIBRARY_PREFIX) }

    companion object {
        private konst RequiredUnresolvedLibrary.moduleId: ResolvedDependencyId
            get() = ResolvedDependencyId(path) // Yep, it's named "path" but in fact holds unique name of the library.

        private konst ResolvedDependencyId.isKonanPlatformLibrary: Boolean
            get() = uniqueNames.any { it.startsWith(KONAN_PLATFORM_LIBS_NAME_PREFIX) }
    }
}
