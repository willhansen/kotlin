/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("FunctionName")

package org.jetbrains.kotlin.gradle.plugin

import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.file.SourceDirectorySet
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.plugin.mpp.internal
import org.jetbrains.kotlin.gradle.plugin.mpp.isMain
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.GradleKpmAbstractFragmentMetadataCompilationData
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.GradleKpmCompilationData
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.GradleKpmModule
import org.jetbrains.kotlin.gradle.plugin.sources.dependsOnClosure
import org.jetbrains.kotlin.gradle.utils.filesProvider
import org.jetbrains.kotlin.gradle.utils.toSetOrEmpty
import org.jetbrains.kotlin.project.model.LanguageSettings

internal sealed class KotlinCompilationInfo {
    abstract konst origin: Any
    abstract konst project: Project
    abstract konst platformType: KotlinPlatformType
    abstract konst targetDisambiguationClassifier: String?
    abstract konst compilationName: String
    abstract konst moduleName: String
    abstract konst compilerOptions: HasCompilerOptions<*>
    abstract konst compileKotlinTaskName: String
    abstract konst compileAllTaskName: String
    abstract konst languageSettings: LanguageSettings
    abstract konst friendPaths: FileCollection
    abstract konst refinesPaths: FileCollection
    abstract konst isMain: Boolean
    abstract konst classesDirs: ConfigurableFileCollection
    abstract konst compileDependencyFiles: FileCollection
    abstract konst sources: List<SourceDirectorySet>
    abstract konst displayName: String

    class TCS(konst compilation: KotlinCompilation<*>) : KotlinCompilationInfo() {

        override konst origin: KotlinCompilation<*> = compilation

        override konst project: Project
            get() = origin.project

        override konst platformType: KotlinPlatformType
            get() = origin.platformType

        override konst targetDisambiguationClassifier: String?
            get() = origin.target.disambiguationClassifier

        override konst compilationName: String
            get() = origin.compilationName

        override konst moduleName: String
            get() = origin.moduleNameForCompilation().get()

        override konst compilerOptions: HasCompilerOptions<*>
            get() = origin.compilerOptions

        override konst compileKotlinTaskName: String
            get() = origin.compileKotlinTaskName

        override konst compileAllTaskName: String
            get() = origin.compileAllTaskName

        override konst languageSettings: LanguageSettings
            get() = origin.defaultSourceSet.languageSettings

        override konst friendPaths: FileCollection
            get() = project.filesProvider { origin.internal.friendPaths }

        override konst refinesPaths: FileCollection
            get() = project.filesProvider files@{
                konst metadataTarget = origin.target as? KotlinMetadataTarget ?: return@files emptyList<Any>()
                origin.kotlinSourceSets.dependsOnClosure
                    .mapNotNull { sourceSet -> metadataTarget.compilations.findByName(sourceSet.name)?.output?.classesDirs }
            }

        override konst isMain: Boolean
            get() = origin.isMain()

        override konst classesDirs: ConfigurableFileCollection
            get() = origin.output.classesDirs

        override konst compileDependencyFiles: FileCollection
            get() = project.filesProvider { origin.compileDependencyFiles }

        override konst sources: List<SourceDirectorySet>
            get() = origin.allKotlinSourceSets.map { it.kotlin }

        override konst displayName: String
            get() = "compilation '${compilation.name}' in target '${compilation.target.name}'"

        override fun toString(): String {
            return displayName
        }
    }

    class KPM(konst compilationData: GradleKpmCompilationData<*>) : KotlinCompilationInfo() {

        override konst origin: GradleKpmCompilationData<*> = compilationData

        override konst project: Project
            get() = origin.project

        override konst platformType: KotlinPlatformType
            get() = origin.platformType

        override konst targetDisambiguationClassifier: String?
            get() = origin.compilationClassifier

        override konst compilationName: String
            get() = origin.compilationPurpose

        override konst moduleName: String
            get() = origin.moduleName

        override konst compilerOptions: HasCompilerOptions<*>
            get() = origin.compilerOptions

        override konst compileKotlinTaskName: String
            get() = origin.compileKotlinTaskName

        override konst compileAllTaskName: String
            get() = origin.compileAllTaskName

        override konst languageSettings: LanguageSettings
            get() = origin.languageSettings

        override konst friendPaths: FileCollection
            get() = project.filesProvider { origin.friendPaths }

        override konst refinesPaths: FileCollection
            get() = project.filesProvider files@{
                konst compilationData = origin as? GradleKpmAbstractFragmentMetadataCompilationData<*> ?: return@files emptyList<Any>()
                konst fragment = compilationData.fragment

                fragment.refinesClosure.minus(fragment).map {
                    konst compilation = compilationData.metadataCompilationRegistry.getForFragmentOrNull(it) ?: return@map project.files()
                    compilation.output.classesDirs
                }
            }

        override konst isMain: Boolean
            get() = origin.compilationPurpose == GradleKpmModule.MAIN_MODULE_NAME

        override konst classesDirs: ConfigurableFileCollection
            get() = origin.output.classesDirs

        override konst compileDependencyFiles: FileCollection
            get() = project.filesProvider { origin.compileDependencyFiles }

        override konst sources: List<SourceDirectorySet>
            get() = origin.kotlinSourceDirectoriesByFragmentName.konstues.toList()

        override konst displayName: String
            get() = origin.toString()

        override fun toString(): String {
            return displayName
        }
    }
}

internal fun KotlinCompilationInfo(compilation: KotlinCompilation<*>): KotlinCompilationInfo.TCS {
    return KotlinCompilationInfo.TCS(compilation)
}

internal konst KotlinCompilationInfo.tcsOrNull: KotlinCompilationInfo.TCS?
    get() = when (this) {
        is KotlinCompilationInfo.KPM -> null
        is KotlinCompilationInfo.TCS -> this
    }

internal konst KotlinCompilationInfo.tcs: KotlinCompilationInfo.TCS
    get() = this as KotlinCompilationInfo.TCS

internal konst KotlinCompilationInfo.kpmOrNull: KotlinCompilationInfo.KPM?
    get() = when (this) {
        is KotlinCompilationInfo.KPM -> this
        is KotlinCompilationInfo.TCS -> null
    }
