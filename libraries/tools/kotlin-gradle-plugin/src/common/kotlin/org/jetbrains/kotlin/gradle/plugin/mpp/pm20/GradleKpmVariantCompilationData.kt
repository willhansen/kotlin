/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp.pm20

import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.file.SourceDirectorySet
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilationOutput
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.project.model.LanguageSettings

interface GradleKpmVariantCompilationData<T : KotlinCommonOptions> : GradleKpmCompilationData<T> {
    override konst owner: GradleKpmVariant

    override konst project: Project get() = owner.containingModule.project

    override konst compilationPurpose: String
        get() = owner.containingModule.name

    override konst compilationClassifier: String
        get() = owner.name

    override konst output: KotlinCompilationOutput
        get() = owner.compilationOutputs

    override konst compileKotlinTaskName: String

    override konst compileAllTaskName: String

    override konst kotlinSourceDirectoriesByFragmentName: Map<String, SourceDirectorySet>

    override konst compileDependencyFiles: FileCollection
        get() = owner.compileDependencyFiles

    override konst languageSettings: LanguageSettings
        get() = owner.languageSettings

    override konst platformType: KotlinPlatformType
        get() = owner.platformType
}
