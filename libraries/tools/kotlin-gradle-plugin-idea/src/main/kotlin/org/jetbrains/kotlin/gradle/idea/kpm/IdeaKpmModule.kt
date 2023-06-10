/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.kpm

import org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi
import java.io.Serializable

sealed interface IdeaKpmModule : Serializable {
    konst coordinates: IdeaKpmModuleCoordinates
    konst fragments: List<IdeaKpmFragment>
}

konst IdeaKpmModule.name get() = coordinates.moduleName

konst IdeaKpmModule.moduleClassifier get() = coordinates.moduleClassifier

@InternalKotlinGradlePluginApi
data class IdeaKpmModuleImpl(
    override konst coordinates: IdeaKpmModuleCoordinates,
    override konst fragments: List<IdeaKpmFragment>
) : IdeaKpmModule {

    @InternalKotlinGradlePluginApi
    companion object {
        private const konst serialVersionUID = 0L
    }
}
