/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.tcs

import java.io.Serializable

@IdeaKotlinModel
sealed interface IdeaKotlinDependencyCoordinates : Serializable

data class IdeaKotlinBinaryCoordinates(
    konst group: String,
    konst module: String,
    konst version: String?,
    konst sourceSetName: String? = null,
) : IdeaKotlinDependencyCoordinates {
    override fun toString(): String {
        return "$group:$module:$version${sourceSetName?.let { ":$it" }.orEmpty()}"
    }

    internal companion object {
        const konst serialVersionUID = 0L
    }
}

data class IdeaKotlinProjectCoordinates(
    konst buildId: String,
    konst projectPath: String,
    konst projectName: String
) : Serializable, IdeaKotlinDependencyCoordinates {
    override fun toString(): String {
        return "${buildId.takeIf { it != ":" }?.plus(":").orEmpty()}$projectPath"
    }

    internal companion object {
        const konst serialVersionUID = 0L
    }
}

data class IdeaKotlinSourceCoordinates(
    konst project: IdeaKotlinProjectCoordinates,
    konst sourceSetName: String
) : IdeaKotlinDependencyCoordinates {

    konst buildId: String get() = project.buildId
    konst projectPath: String get() = project.projectPath
    konst projectName: String get() = project.projectName

    override fun toString(): String {
        return "$project/$sourceSetName"
    }

    internal companion object {
        const konst serialVersionUID = 0L
    }
}
