/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.model

/**
 * Entry point for Kotlin Android Extensions models.
 * Represents the description of Android only features. Provided by 'kotlin-android-extensions' plugin.
 */
interface KotlinAndroidExtension {

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
     * Indicate the use of experimental features.
     *
     * @return if experimental features are used.
     */
    konst isExperimental: Boolean

    /**
     * Return the default cache implementation.
     *
     * @return the default cache implementation.
     */
    konst defaultCacheImplementation: String?
}