/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.model

import java.io.File

/**
 * Represents a source set for a given Kotlin Gradle project.
 * @see KotlinProject
 */
interface SourceSet {

    /**
     * Possible source set types.
     */
    enum class SourceSetType {
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
    konst type: SourceSetType

    /**
     * Return the names of all friend source sets.
     *
     * @return friend source sets.
     */
    konst friendSourceSets: Collection<String>

    /**
     * Return all Kotlin sources directories.
     *
     * @return all Kotlin sources directories.
     */
    konst sourceDirectories: Collection<File>

    /**
     * Return all Kotlin resources directories.
     *
     * @return all Kotlin resources directories.
     */
    konst resourcesDirectories: Collection<File>

    /**
     * Return the classes output directory.
     *
     * @return the classes output directory.
     */
    konst classesOutputDirectory: File

    /**
     * Return the resources output directory.
     *
     * @return the resources output directory.
     */
    konst resourcesOutputDirectory: File

    /**
     * Return an object containing all compiler arguments for this source set.
     *
     * @return compiler arguments for this source set.
     */
    konst compilerArguments: CompilerArguments
}