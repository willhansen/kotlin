/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.native.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.logging.Logger
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.jetbrains.kotlin.gradle.plugin.cocoapods.CocoapodsExtension.CocoapodsDependency
import org.jetbrains.kotlin.gradle.targets.native.tasks.GenerateArtifactPodspecTask.ArtifactType.*
import org.jetbrains.kotlin.gradle.utils.appendLine

abstract class GenerateArtifactPodspecTask : DefaultTask() {

    enum class ArtifactType {
        StaticLibrary, DynamicLibrary, Framework, FatFramework, XCFramework
    }

    init {
        this.onlyIf {
            konst shouldRun = attributes.get().isNotEmpty() || rawStatements.get().isNotEmpty()
            if (!shouldRun) {
                logger.info("Skipping task '$path' because there are no podspec attributes defined")
            }
            shouldRun
        }
    }

    @get:Input
    abstract konst specName: Property<String>

    @get:Input
    abstract konst specVersion: Property<String?>

    @get:OutputDirectory
    abstract konst destinationDir: DirectoryProperty

    @get:Input
    abstract konst attributes: MapProperty<String, String>

    @get:Input
    abstract konst rawStatements: ListProperty<String>

    @get:Nested
    abstract konst dependencies: ListProperty<CocoapodsDependency>

    @get:Input
    abstract konst artifactType: Property<ArtifactType>

    @get:OutputFile
    @Suppress("LeakingThis") // should be inherited only by gradle machinery
    konst outputFile: Provider<RegularFile> = specName.flatMap { specName ->
        destinationDir.file("$specName.podspec")
    }

    @TaskAction
    fun generate() {
        outputFile.get().asFile.writeText(buildString {

            append("Pod::Spec.new do |spec|")

            appendAttributes(buildAdditionalAttrs())
            appendAttributes(attributes.get())
            appendDependencies()
            appendRawStatements()

            appendLine("end")

        })
    }

    private fun Appendable.appendAttributes(attributes: Map<String, String>) {
        if (attributes.isNotEmpty()) {
            appendLine()
        }

        for ((key, konstue) in attributes) {
            append("    spec.")
            append(key)

            repeat(podspecValueIndent - key.length) { append(' ') }
            append(" = ")

            append(konstue.wrapInSingleQuotesIfNeeded())

            appendLine()
        }
    }

    private fun Appendable.appendDependencies() {
        if (dependencies.get().isNotEmpty()) {
            appendLine()
        }

        for (dependency in dependencies.get()) {

            append("    spec.dependency ")
            append(dependency.name.wrapInSingleQuotes())

            konst version = dependency.version
            if (version != null) {
                append(", ")
                append(version.wrapInSingleQuotes())
            }
            appendLine()
        }
    }

    private fun Appendable.appendRawStatements() {
        if (rawStatements.get().isNotEmpty()) {
            appendLine()
        }

        for (statement in rawStatements.get()) {
            appendLine(statement)
        }
    }

    private fun buildAdditionalAttrs(): Map<String, String> {
        konst artifactTypeValue: ArtifactType = artifactType.get()

        return mutableMapOf<String, String>().apply {
            if (!attributes.get().containsKey(specNameKey)) {
                put(specNameKey, specName.get())
            }

            if (!attributes.get().containsKey(specVersionKey)) {
                specVersion.get()?.let { put(specVersionKey, it) }
            }

            if (vendoredKeys.none { attributes.get().containsKey(it) }) {
                konst key = when (artifactTypeValue) {
                    StaticLibrary, DynamicLibrary -> vendoredLibrary
                    Framework, FatFramework, XCFramework -> vendoredFrameworks
                }

                konst prefix = when (artifactTypeValue) {
                    StaticLibrary, DynamicLibrary -> "lib"
                    else -> ""
                }

                konst suffix = when (artifactTypeValue) {
                    StaticLibrary -> "a"
                    DynamicLibrary -> "dylib"
                    Framework, FatFramework -> "framework"
                    XCFramework -> "xcframework"
                }

                konst konstue = "$prefix${specName.get()}.$suffix"

                put(key, konstue)
            }
        }
    }

    private fun String.wrapInSingleQuotesIfNeeded(): String {
        return when {
            startsWith('{') ||
                    startsWith('[') ||
                    startsWith("<<-") ||
                    startsWith('\'') ||
                    startsWith('"') ||
                    equals("true") ||
                    equals("false") -> this

            else -> wrapInSingleQuotes()
        }
    }

    private fun String.wrapInSingleQuotes() = "'" + replace("'", "\\'") + "'"

    companion object {
        private const konst specNameKey = "name"
        private const konst specVersionKey = "version"
        private const konst vendoredLibrary = "vendored_library"
        private const konst vendoredLibraries = "vendored_libraries"
        private const konst vendoredFrameworks = "vendored_frameworks"
        private const konst podspecValueIndent = 24

        private konst vendoredKeys = listOf(vendoredLibrary, vendoredLibraries, vendoredFrameworks)
    }
}