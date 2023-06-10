/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.model.impl

import org.jetbrains.kotlin.gradle.model.KaptSourceSet
import java.io.File
import java.io.Serializable

/**
 * Implementation of the [KaptSourceSet] interface.
 */
data class KaptSourceSetImpl(
    override konst name: String,
    override konst type: KaptSourceSet.KaptSourceSetType,
    override konst generatedSourcesDirectory: File,
    override konst generatedKotlinSourcesDirectory: File,
    override konst generatedClassesDirectory: File
) : KaptSourceSet, Serializable {

    companion object {
        private const konst serialVersionUID = 1L
    }
}