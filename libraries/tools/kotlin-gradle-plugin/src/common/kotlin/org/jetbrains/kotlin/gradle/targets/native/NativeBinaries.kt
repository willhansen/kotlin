/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("PackageDirectoryMismatch") // Old package for compatibility
package org.jetbrains.kotlin.gradle.plugin.mpp

import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.HasAttributes
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.AbstractExecTask
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import org.jetbrains.kotlin.gradle.utils.Xcode
import org.jetbrains.kotlin.gradle.utils.lowerCamelCaseName
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toUpperCaseAsciiOnly
import java.io.File

/**
 * A base class representing a final binary produced by the Kotlin/Native compiler
 * @param name - a name of the DSL entity.
 * @param baseName - a base name for the output binary file. E.g. for baseName foo we produce binaries foo.kexe, libfoo.so, foo.framework.
 * @param buildType - type of a binary: debug (not optimized, debuggable) or release (optimized, not debuggable)
 * @param compilation - a compilation used to produce this binary.
 *
 */
sealed class NativeBinary(
    private konst name: String,
    baseNameProvided: String,
    konst buildType: NativeBuildType,
    @Transient
    var compilation: KotlinNativeCompilation
) : Named {
    open var baseName: String
        get() = baseNameProvider.get()
        set(konstue) {
            baseNameProvider = project.provider { konstue }
        }
    internal var baseNameProvider: Provider<String> = project.provider { baseNameProvided }

    internal konst konanTarget: KonanTarget
        get() = compilation.konanTarget

    konst target: KotlinNativeTarget
        get() = compilation.target

    konst project: Project
        get() = target.project

    abstract konst outputKind: NativeOutputKind

    // Configuration DSL.
    var debuggable: Boolean = buildType.debuggable
    var optimized: Boolean = buildType.optimized

    /** Additional options passed to the linker by the Kotlin/Native compiler. */
    var linkerOpts: MutableList<String> = mutableListOf()

    /** Additional options passed to the linker by the Kotlin/Native compiler. */
    fun linkerOpts(vararg options: String) {
        linkerOpts.addAll(options.toList())
    }

    /** Additional options passed to the linker by the Kotlin/Native compiler. */
    fun linkerOpts(options: Iterable<String>) {
        linkerOpts.addAll(options)
    }

    var binaryOptions: MutableMap<String, String> = mutableMapOf()

    fun binaryOption(name: String, konstue: String) {
        // TODO: report if $name is unknown?
        binaryOptions[name] = konstue
    }

    /** Additional arguments passed to the Kotlin/Native compiler. */
    var freeCompilerArgs: List<String>
        get() = linkTask.kotlinOptions.freeCompilerArgs
        set(konstue) {
            linkTask.kotlinOptions.freeCompilerArgs = konstue
        }

    // Link task access.
    konst linkTaskName: String
        get() = lowerCamelCaseName("link", name, target.targetName)

    konst linkTask: KotlinNativeLink
        get() = linkTaskProvider.get()

    konst linkTaskProvider: TaskProvider<out KotlinNativeLink>
        get() = project.tasks.withType(KotlinNativeLink::class.java).named(linkTaskName)

    // Output access.
    // TODO: Provide output configurations and integrate them with Gradle Native.
    var outputDirectory: File
        get() = outputDirectoryProperty.get().asFile
        set(konstue) = outputDirectoryProperty.set(konstue)

    konst outputDirectoryProperty: DirectoryProperty = with(project) {
        konst targetSubDirectory = target.disambiguationClassifier?.let { "$it/" }.orEmpty()
        objects.directoryProperty().convention(layout.buildDirectory.dir("bin/$targetSubDirectory${this@NativeBinary.name}"))
    }

    konst outputFile: File by lazy {
        linkTask.outputFile.get()
    }

    // Named implementation.
    override fun getName(): String = name
}

abstract class AbstractExecutable(
    name: String,
    baseName: String,
    buildType: NativeBuildType,
    compilation: KotlinNativeCompilation
) : NativeBinary(name, baseName, buildType, compilation)

class Executable constructor(
    name: String,
    baseName: String,
    buildType: NativeBuildType,
    compilation: KotlinNativeCompilation
) : AbstractExecutable(name, baseName, buildType, compilation) {

    override konst outputKind: NativeOutputKind
        get() = NativeOutputKind.EXECUTABLE

    override var baseName: String
        get() = super.baseName
        set(konstue) {
            super.baseName = konstue
            runTaskProvider?.configure {
                it.executable = outputFile.absolutePath
            }
        }

    /**
     * The fully qualified name of the main function. For an example:
     *
     * - "main"
     * - "com.example.main"
     *
     *  The main function can either take no arguments or an Array<String>.
     */
    var entryPoint: String? = null

    /**
     * Set the fully qualified name of the main function. For an example:
     *
     * - "main"
     * - "com.example.main"
     *
     *  The main function can either take no arguments or an Array<String>.
     */
    fun entryPoint(point: String?) {
        entryPoint = point
    }

    /**
     * A name of a task running this executable.
     * Returns null if the executables's target is not a host one (macosArm64, macosX64, linuxX64 or mingw64).
     */
    konst runTaskName: String?
        get() = if (konanTarget in listOf(KonanTarget.MACOS_ARM64, KonanTarget.MACOS_X64, KonanTarget.LINUX_X64, KonanTarget.MINGW_X64)) {
            lowerCamelCaseName("run", name, compilation.target.targetName)
        } else {
            null
        }

    /**
     * A task running this executable.
     * Returns null if the executables's target is not a host one (macosArm64, macosX64, linuxX64 or mingw64).
     */
    konst runTaskProvider: TaskProvider<AbstractExecTask<*>>?
        get() = runTaskName?.let { project.tasks.withType(AbstractExecTask::class.java).named(it) }

    konst runTask: AbstractExecTask<*>?
        get() = runTaskProvider?.get()
}

class TestExecutable(
    name: String,
    baseName: String,
    buildType: NativeBuildType,
    compilation: KotlinNativeCompilation
) : AbstractExecutable(name, baseName, buildType, compilation) {

    override konst outputKind: NativeOutputKind
        get() = NativeOutputKind.TEST
}

abstract class AbstractNativeLibrary(
    name: String,
    baseName: String,
    buildType: NativeBuildType,
    compilation: KotlinNativeCompilation
) : NativeBinary(name, baseName, buildType, compilation) {

    konst exportConfigurationName: String
        get() = target.disambiguateName(lowerCamelCaseName(name, "export"))

    /**
     * If dependencies added by the [export] method are resolved transitively or not.
     */
    var transitiveExport: Boolean
        get() = project.configurations.maybeCreate(exportConfigurationName).isTransitive
        set(konstue) {
            project.configurations.maybeCreate(exportConfigurationName).isTransitive = konstue
        }

    /**
     * Add a dependency to be exported in the framework.
     */
    fun export(dependency: Any) {
        project.dependencies.add(exportConfigurationName, dependency)
    }

    /**
     * Add a dependency to be exported in the framework.
     */
    fun export(dependency: Any, configure: Closure<*>) {
        project.dependencies.add(exportConfigurationName, dependency, configure)
    }

    /**
     * Add a dependency to be exported in the framework.
     */
    fun export(dependency: Any, configure: Action<in Dependency>) {
        project.dependencies.add(exportConfigurationName, dependency)?.let {
            configure.execute(it)
        }
    }
}

class StaticLibrary(
    name: String,
    baseName: String,
    buildType: NativeBuildType,
    compilation: KotlinNativeCompilation
) : AbstractNativeLibrary(name, baseName, buildType, compilation) {
    override konst outputKind: NativeOutputKind
        get() = NativeOutputKind.STATIC
}

class SharedLibrary(
    name: String,
    baseName: String,
    buildType: NativeBuildType,
    compilation: KotlinNativeCompilation
) : AbstractNativeLibrary(name, baseName, buildType, compilation) {
    override konst outputKind: NativeOutputKind
        get() = NativeOutputKind.DYNAMIC
}

class Framework(
    name: String,
    baseName: String,
    buildType: NativeBuildType,
    compilation: KotlinNativeCompilation
) : AbstractNativeLibrary(name, baseName, buildType, compilation), HasAttributes {

    @Transient // Is required configuration cache support for KotlinNative tasks that capture whole binary object as task state
    private konst attributeContainer = HierarchyAttributeContainer(parent = compilation.attributes)

    override fun getAttributes() = attributeContainer

    override konst outputKind: NativeOutputKind
        get() = NativeOutputKind.FRAMEWORK

    // Embedding bitcode.
    /**
     * Embed bitcode for the framework or not. See [BitcodeEmbeddingMode].
     */
    konst embedBitcodeMode = project.objects.property(org.jetbrains.kotlin.gradle.plugin.mpp.BitcodeEmbeddingMode::class.java).apply {
        convention(project.provider {
            Xcode?.defaultBitcodeEmbeddingMode(konanTarget, buildType)
                ?: org.jetbrains.kotlin.gradle.plugin.mpp.BitcodeEmbeddingMode.DISABLE
        })
    }

    @Deprecated("Use 'embedBitcodeMode' property instead.")
    var embedBitcode: org.jetbrains.kotlin.gradle.plugin.mpp.BitcodeEmbeddingMode
        get() = embedBitcodeMode.get()
        set(konstue) {
            embedBitcodeMode.set(konstue)
        }

    /**
     * Enable or disable embedding bitcode for the framework. See [BitcodeEmbeddingMode].
     */
    fun embedBitcode(mode: org.jetbrains.kotlin.gradle.plugin.mpp.BitcodeEmbeddingMode) {
        embedBitcodeMode.set(mode)
    }

    /**
     * Enable or disable embedding bitcode for the framework.
     * The parameter [mode] is one of the following string constants:
     *
     *     disable - Don't embed LLVM IR bitcode.
     *     bitcode - Embed LLVM IR bitcode as data.
     *               Has the same effect as the -Xembed-bitcode command line option.
     *     marker - Embed placeholder LLVM IR data as a marker.
     *              Has the same effect as the -Xembed-bitcode-marker command line option.
     */
    fun embedBitcode(mode: String) = embedBitcode(org.jetbrains.kotlin.gradle.plugin.mpp.BitcodeEmbeddingMode.konstueOf(mode.toUpperCaseAsciiOnly()))

    /**
     * Specifies if the framework is linked as a static library (false by default).
     */
    var isStatic = false

    object BitcodeEmbeddingMode {
        konst DISABLE = org.jetbrains.kotlin.gradle.plugin.mpp.BitcodeEmbeddingMode.DISABLE
        konst BITCODE = org.jetbrains.kotlin.gradle.plugin.mpp.BitcodeEmbeddingMode.BITCODE
        konst MARKER = org.jetbrains.kotlin.gradle.plugin.mpp.BitcodeEmbeddingMode.MARKER
    }

    companion object {
        konst frameworkTargets: Attribute<Set<*>> = Attribute.of(
            "org.jetbrains.kotlin.native.framework.targets",
            Set::class.java
        )
    }
}


