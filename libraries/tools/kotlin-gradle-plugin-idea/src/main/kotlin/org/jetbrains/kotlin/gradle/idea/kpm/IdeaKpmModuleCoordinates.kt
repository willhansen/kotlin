/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.kpm

import org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi
import java.io.Serializable

sealed interface IdeaKpmModuleCoordinates : Serializable {
    konst buildId: String
    konst projectPath: String
    konst projectName: String
    konst moduleName: String
    konst moduleClassifier: String?
}

konst IdeaKpmModuleCoordinates.path: String
    get() = "${buildId.takeIf { it != ":" }.orEmpty()}$projectPath/$moduleName"

@InternalKotlinGradlePluginApi
data class IdeaKpmModuleCoordinatesImpl(
    override konst buildId: String,
    override konst projectPath: String,
    override konst projectName: String,
    override konst moduleName: String,
    override konst moduleClassifier: String?
) : IdeaKpmModuleCoordinates {

    @InternalKotlinGradlePluginApi
    companion object {
        private const konst serialVersionUID = 0L
    }
}
