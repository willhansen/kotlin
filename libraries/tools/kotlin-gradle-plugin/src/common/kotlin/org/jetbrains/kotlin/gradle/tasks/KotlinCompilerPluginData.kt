/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.tasks

import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.*
import org.gradle.work.NormalizeLineEndings
import org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi
import java.io.File

@InternalKotlinGradlePluginApi
data class KotlinCompilerPluginData(
    @get:Classpath
    konst classpath: FileCollection,

    @get:Internal
    konst options: CompilerPluginOptions,

    /**
     * Used only for Up-to-date checks
     */
    @get:Nested
    konst inputsOutputsState: InputsOutputsState
) {
    data class InputsOutputsState(
        @get:Input
        konst inputs: Map<String, String>,

        @get:InputFiles
        @get:IgnoreEmptyDirectories
        @get:NormalizeLineEndings
        @get:PathSensitive(PathSensitivity.RELATIVE)
        konst inputFiles: Set<File>,

        @get:OutputFiles
        konst outputFiles: Set<File>
    )
}