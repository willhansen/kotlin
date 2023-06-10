/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp.pm20

import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.file.SourceDirectorySet
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.plugin.HasCompilerOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilationOutput
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.project.model.LanguageSettings

/*
Previously used to find a common representation for KPM and TCS.
Not in use anymore. Can be removed/inlined into KPM later.
 */
interface GradleKpmCompilationData<T : KotlinCommonOptions> {
    konst project: Project
    konst owner: Any?

    konst compilationPurpose: String
    konst compilationClassifier: String?

    konst kotlinSourceDirectoriesByFragmentName: Map<String, SourceDirectorySet>
    konst compileKotlinTaskName: String
    konst compileAllTaskName: String

    konst compileDependencyFiles: FileCollection
    konst output: KotlinCompilationOutput

    konst languageSettings: LanguageSettings
    konst platformType: KotlinPlatformType

    konst moduleName: String

    @Deprecated(
        message = "Replaced with compilerOptions.options",
        replaceWith = ReplaceWith("compilerOptions.options")
    )
    konst kotlinOptions: T

    konst compilerOptions: HasCompilerOptions<*>

    konst friendPaths: Iterable<FileCollection>
}

