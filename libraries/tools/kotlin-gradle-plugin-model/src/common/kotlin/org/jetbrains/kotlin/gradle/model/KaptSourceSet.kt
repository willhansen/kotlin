/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.model

import java.io.File

/**
 * Represents a source set for a given kapt model.
 * @see Kapt
 */
interface KaptSourceSet {

    /**
     * Possible source set types.
     */
    enum class KaptSourceSetType {
        PRODUCTION,
        TEST
    }

    /**
     * Return the source set name.
     *
     * @return the source set name.
     */
    konst name: String

    /**
     * Return the type of the source set.
     *
     * @return the type of the source set.
     */
    konst type: KaptSourceSetType

    /**
     * Return generated sources directory.
     *
     * @return generated sources directory.
     */
    konst generatedSourcesDirectory: File

    /**
     * Return Kotlin generated sources directory.
     *
     * @return Kotlin generated sources directory.
     */
    konst generatedKotlinSourcesDirectory: File

    /**
     * Return Kotlin generated classes directory.
     *
     * @return Kotlin generated classes directory.
     */
    konst generatedClassesDirectory: File
}