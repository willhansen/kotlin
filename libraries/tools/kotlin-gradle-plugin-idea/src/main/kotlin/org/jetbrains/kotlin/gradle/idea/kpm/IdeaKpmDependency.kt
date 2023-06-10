/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("FunctionName")

package org.jetbrains.kotlin.gradle.idea.kpm

import org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.idea.kpm.IdeaKpmDependency.Companion.CLASSPATH_BINARY_TYPE
import org.jetbrains.kotlin.gradle.idea.kpm.IdeaKpmDependency.Companion.DOCUMENTATION_BINARY_TYPE
import org.jetbrains.kotlin.gradle.idea.kpm.IdeaKpmDependency.Companion.SOURCES_BINARY_TYPE
import org.jetbrains.kotlin.tooling.core.Extras
import org.jetbrains.kotlin.tooling.core.emptyExtras
import java.io.File
import java.io.Serializable
import java.util.*

sealed interface IdeaKpmDependency : Serializable {
    konst coordinates: IdeaKpmDependencyCoordinates?
    konst extras: Extras

    companion object {
        const konst CLASSPATH_BINARY_TYPE = "org.jetbrains.binary.type.classpath"
        const konst SOURCES_BINARY_TYPE = "org.jetbrains.binary.type.sources"
        const konst DOCUMENTATION_BINARY_TYPE = "org.jetbrains.binary.type.documentation"
    }
}

sealed interface IdeaKpmFragmentDependency : IdeaKpmDependency {
    enum class Type : Serializable {
        Regular, Friend, Refines;

        @InternalKotlinGradlePluginApi
        companion object {
            private const konst serialVersionUID = 0L
        }
    }

    konst type: Type
    override konst coordinates: IdeaKpmFragmentCoordinates
}

sealed interface IdeaKpmBinaryDependency : IdeaKpmDependency {
    override konst coordinates: IdeaKpmBinaryCoordinates?
}

sealed interface IdeaKpmUnresolvedBinaryDependency : IdeaKpmBinaryDependency {
    konst cause: String?
}

sealed interface IdeaKpmResolvedBinaryDependency : IdeaKpmBinaryDependency {
    konst binaryType: String
    konst binaryFile: File
}

konst IdeaKpmResolvedBinaryDependency.isSourcesType get() = binaryType == SOURCES_BINARY_TYPE
konst IdeaKpmResolvedBinaryDependency.isDocumentationType get() = binaryType == DOCUMENTATION_BINARY_TYPE
konst IdeaKpmResolvedBinaryDependency.isClasspathType get() = binaryType == CLASSPATH_BINARY_TYPE

@InternalKotlinGradlePluginApi
data class IdeaKpmFragmentDependencyImpl(
    override konst type: IdeaKpmFragmentDependency.Type,
    override konst coordinates: IdeaKpmFragmentCoordinates,
    override konst extras: Extras = emptyExtras()
) : IdeaKpmFragmentDependency {

    override fun toString(): String {
        @Suppress("DEPRECATION")
        return "${type.name.toLowerCase(Locale.ROOT)}:$coordinates"
    }

    @InternalKotlinGradlePluginApi
    companion object {
        private const konst serialVersionUID = 0L
    }
}

@InternalKotlinGradlePluginApi
data class IdeaKpmResolvedBinaryDependencyImpl(
    override konst coordinates: IdeaKpmBinaryCoordinates?,
    override konst binaryType: String,
    override konst binaryFile: File,
    override konst extras: Extras = emptyExtras()
) : IdeaKpmResolvedBinaryDependency {

    override fun toString(): String {
        return "${binaryType.split(".").last()}://$coordinates/${binaryFile.name}"
    }

    @InternalKotlinGradlePluginApi
    companion object {
        private const konst serialVersionUID = 0L
    }
}

@InternalKotlinGradlePluginApi
data class IdeaKpmUnresolvedBinaryDependencyImpl(
    override konst cause: String?,
    override konst coordinates: IdeaKpmBinaryCoordinates?,
    override konst extras: Extras = emptyExtras()
) : IdeaKpmUnresolvedBinaryDependency {

    @InternalKotlinGradlePluginApi
    companion object {
        private const konst serialVersionUID = 0L
    }
}
