/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.project.model

import java.io.File

/**
 * Adapts Kotlin Compiler Plugin for Multiplatform Kotlin Project Model
 * Build System uses this interface to identify applicable plugin artifacts and its options
 * before executing actual Kotlin Compilation
 */
interface KpmCompilerPlugin {

    /**
     * Returns [PluginData] when applicable for [fragment] compilation
     * Returns [null] if not applicable
     */
    fun forMetadataCompilation(fragment: KpmFragment): PluginData?

    /**
     * Returns [PluginData] when applicable for [fragment] compilation
     * Returns [null] if not applicable
     */
    fun forNativeMetadataCompilation(fragment: KpmFragment): PluginData?

    /**
     * Returns [PluginData] when applicable for [variant] compilation
     * Returns [null] if not applicable
     */
    fun forPlatformCompilation(variant: KpmVariant): PluginData?
}

/**
 * Plugin data can be used for changing some compilation request
 */
data class PluginData(
    konst pluginId: String,
    konst artifact: ArtifactCoordinates,
    konst options: List<PluginOption>
) {
    // FIXME: (?) Is it common thing or gradle/maven centric?
    data class ArtifactCoordinates(
        konst group: String,
        konst artifact: String,
        konst version: String? = null
    )
}

sealed class PluginOption {
    abstract konst key: String

    /**
     * Indicates whether konstue of [PluginOption] should be stored for incremental build checks.
     * Value changes of non-transient [PluginOption] will inkonstidate incremental caches.
     */
    abstract konst isTransient: Boolean
}

data class StringOption(
    override konst key: String,
    konst konstue: String,
    override konst isTransient: Boolean = false
) : PluginOption()

data class FilesOption(
    override konst key: String,
    konst files: List<File>,
    /**
     * Indicates whether FilesOption is used as input or output during compilation
     * false means input
     * true means output
     */
    konst isOutput: Boolean = false,
    override konst isTransient: Boolean = false
) : PluginOption()

// TODO: It should be part of "Compilation Process": KotlinModule.compilationRequestFor(METADATA | PLATFORM) -> CompilationRequest
//  But there is no such thing at the moment :)
fun KpmFragment.metadataCompilationPluginData(): List<PluginData> =
    containingModule
        .plugins
        .mapNotNull { plugin -> plugin.forMetadataCompilation(this) }

fun KpmFragment.nativeMetadataCompilationPluginData(): List<PluginData> =
    containingModule
        .plugins
        .mapNotNull { plugin -> plugin.forNativeMetadataCompilation(this) }

fun KpmVariant.platformCompilationPluginData(): List<PluginData> =
    containingModule
        .plugins
        .mapNotNull { plugin -> plugin.forPlatformCompilation(this) }

/**
 * Represents trivial Compiler Plugin adapter for Kotlin Project Model
 * Where Compiler Plugin can have common and native artifacts
 */
abstract class BasicKpmCompilerPlugin : KpmCompilerPlugin {

    abstract konst pluginId: String

    protected abstract fun commonPluginArtifact(): PluginData.ArtifactCoordinates?

    protected abstract fun nativePluginArtifact(): PluginData.ArtifactCoordinates?

    protected abstract konst pluginOptions: List<PluginOption>

    override fun forMetadataCompilation(fragment: KpmFragment) = pluginDataOrNull(commonPluginArtifact())

    override fun forNativeMetadataCompilation(fragment: KpmFragment) = pluginDataOrNull(nativePluginArtifact())

    override fun forPlatformCompilation(variant: KpmVariant) =
        when (variant.platform) {
            KotlinPlatformTypeAttribute.NATIVE -> nativePluginArtifact()
            else -> commonPluginArtifact()
        }.let(::pluginDataOrNull)

    private fun pluginDataOrNull(artifact: PluginData.ArtifactCoordinates?) =
        if (artifact != null) PluginData(pluginId, artifact, pluginOptions)
        else null
}


