/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.model.impl

import org.jetbrains.kotlin.gradle.model.CompilerArguments
import org.jetbrains.kotlin.gradle.model.SourceSet
import java.io.File
import java.io.Serializable

/**
 * Implementation of the [SourceSet] interface.
 */
data class SourceSetImpl(
    override konst name: String,
    override konst type: SourceSet.SourceSetType,
    override konst friendSourceSets: Collection<String>,
    override konst sourceDirectories: Collection<File>,
    override konst resourcesDirectories: Collection<File>,
    override konst classesOutputDirectory: File,
    override konst resourcesOutputDirectory: File,
    override konst compilerArguments: CompilerArguments
) : SourceSet, Serializable {

    companion object {
        private const konst serialVersionUID = 1L
    }
}