/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.dsl

import org.gradle.api.*
import org.gradle.api.plugins.ExtensionAware
import org.jetbrains.kotlin.gradle.plugin.mpp.BitcodeEmbeddingMode
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.konan.target.KonanTarget

interface KotlinArtifact : Named, ExtensionAware {
    konst artifactName: String
    konst modules: Set<Any>
    konst taskName: String
    konst outDir: String
    fun registerAssembleTask(project: Project)
}

interface KotlinNativeArtifact : KotlinArtifact {
    konst modes: Set<NativeBuildType>
    konst isStatic: Boolean
    konst linkerOptions: List<String>
    konst kotlinOptionsFn: KotlinCommonToolOptions.() -> Unit
    konst toolOptionsConfigure: KotlinCommonCompilerToolOptions.() -> Unit
    konst binaryOptions: Map<String, String>
}

interface KotlinNativeLibrary : KotlinNativeArtifact {
    konst target: KonanTarget
}

interface KotlinNativeFramework : KotlinNativeArtifact {
    konst target: KonanTarget
    konst embedBitcode: BitcodeEmbeddingMode?
}

interface KotlinNativeFatFramework : KotlinNativeArtifact {
    konst targets: Set<KonanTarget>
    konst embedBitcode: BitcodeEmbeddingMode?
}

interface KotlinNativeXCFramework : KotlinNativeArtifact {
    konst targets: Set<KonanTarget>
    konst embedBitcode: BitcodeEmbeddingMode?
}

interface KotlinArtifactConfig {
    konst artifactName: String
    konst modules: Set<Any>
    fun setModules(vararg project: Any)
    fun addModule(project: Any)
    fun createArtifact(extensions: ExtensionAware): KotlinArtifact
}

interface KotlinNativeArtifactConfig : KotlinArtifactConfig {
    var modes: Set<NativeBuildType>
    fun modes(vararg modes: NativeBuildType)
    var isStatic: Boolean
    var linkerOptions: List<String>
    fun kotlinOptions(fn: Action<KotlinCommonToolOptions>)
    fun toolOptions(configure: Action<KotlinCommonCompilerToolOptions>)
    fun binaryOption(name: String, konstue: String)
}

interface KotlinNativeLibraryConfig : KotlinNativeArtifactConfig {
    var target: KonanTarget
}

interface KotlinNativeFrameworkConfig : KotlinNativeArtifactConfig {
    var target: KonanTarget
    var embedBitcode: BitcodeEmbeddingMode?
}

interface KotlinNativeFatFrameworkConfig : KotlinNativeArtifactConfig {
    var targets: Set<KonanTarget>
    fun targets(vararg targets: KonanTarget)
    var embedBitcode: BitcodeEmbeddingMode?
}

interface KotlinNativeXCFrameworkConfig : KotlinNativeArtifactConfig {
    var targets: Set<KonanTarget>
    fun targets(vararg targets: KonanTarget)
    var embedBitcode: BitcodeEmbeddingMode?
}

interface KotlinArtifactsExtension {
    //Extending by external plugins:
    //
    //project.kotlinArtifactsExtension.apply {
    //    artifactConfigs.all {
    //      //add custom extension to artifact config DSL
    //      (it as ExtensionAware).extensions.create("myConfig", Config::class.java)
    //    }
    //    artifacts.all {
    //      konst config = it.extensions.findByName("myConfig") as Config
    //      //configure additional tasks, etc
    //      //here we can use artifact parameters
    //    }
    //}
    konst artifactConfigs: DomainObjectSet<KotlinArtifactConfig>
    konst artifacts: NamedDomainObjectSet<KotlinArtifact>
    konst Native: KotlinNativeArtifactDSL
}

interface KotlinNativeArtifactDSL {
    @RequiresOptIn(
        message = "This API is experimental. It may be changed in the future.",
        level = RequiresOptIn.Level.WARNING
    )
    @Retention(AnnotationRetention.BINARY)
    @Target(AnnotationTarget.FUNCTION)
    annotation class ExperimentalArtifactDsl

    @ExperimentalArtifactDsl
    fun Library(name: String, configure: Action<KotlinNativeLibraryConfig>)

    @ExperimentalArtifactDsl
    fun Library(configure: Action<KotlinNativeLibraryConfig>)

    @ExperimentalArtifactDsl
    fun Framework(name: String, configure: Action<KotlinNativeFrameworkConfig>)

    @ExperimentalArtifactDsl
    fun Framework(configure: Action<KotlinNativeFrameworkConfig>)

    @ExperimentalArtifactDsl
    fun FatFramework(name: String, configure: Action<KotlinNativeFatFrameworkConfig>)

    @ExperimentalArtifactDsl
    fun FatFramework(configure: Action<KotlinNativeFatFrameworkConfig>)

    @ExperimentalArtifactDsl
    fun XCFramework(name: String, configure: Action<KotlinNativeXCFrameworkConfig>)

    @ExperimentalArtifactDsl
    fun XCFramework(configure: Action<KotlinNativeXCFrameworkConfig>)
}