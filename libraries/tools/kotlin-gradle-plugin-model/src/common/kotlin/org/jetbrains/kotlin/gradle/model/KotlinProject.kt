/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.model

/**
 * Entry point for the model of Kotlin Projects.
 * Plugins 'kotlin', 'kotlin-platform-jvm', 'kotlin2js', 'kotlin-platform-js' and 'kotlin-platform-common' can produce this model.
 */
interface KotlinProject {

    /**
     * Possible Kotlin project types.
     */
    enum class ProjectType {

        /** Indicator of platform plugin id 'kotlin-platform-jvm' or 'kotlin'.  */
        PLATFORM_JVM,

        /** Indicator of platform plugin id 'kotlin-platform-js' or 'kotlin2js'.  */
        PLATFORM_JS,

        /** Indicator of platform plugin id 'kotlin-platform-common'.  */
        PLATFORM_COMMON
    }

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
     * Return the Kotlin version.
     *
     * @return the Kotlin version.
     */
    konst kotlinVersion: String

    /**
     * Return the type of the platform plugin applied.
     *
     * @return the type of the platform plugin applied. Possible konstues are defined in the enum.
     */
    konst projectType: ProjectType

    /**
     * Return all source sets used by Kotlin.
     *
     * @return all source sets.
     */
    konst sourceSets: Collection<SourceSet>

    /**
     * Return all modules (Gradle projects) registered as 'expectedBy' dependency.
     *
     * @return expectedBy dependencies.
     */
    konst expectedByDependencies: Collection<String>
}