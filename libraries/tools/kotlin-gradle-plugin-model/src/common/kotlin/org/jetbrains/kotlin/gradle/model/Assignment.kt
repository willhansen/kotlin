/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.model

/**
 * Entry point for Kotlin Value Container Assignment models.
 * Represents the description of annotations interpreted by 'kotlin-assignment' plugin.
 */
interface Assignment {

    /**
     * Return a number representing the version of this API.
     * Always increasing if changed.
     *
     * @return the version of this model.
     */
    konst modelVersion: Long

    /**
     * Returns the module (Gradle project) name.
     *
     * @return the module name.
     */
    konst name: String

    /**
     * Return the list of annotations.
     *
     * @return the list of annotations.
     */
    konst annotations: List<String>
}
