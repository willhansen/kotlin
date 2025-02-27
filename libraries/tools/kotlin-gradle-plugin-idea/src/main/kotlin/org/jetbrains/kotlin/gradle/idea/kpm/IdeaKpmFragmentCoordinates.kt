/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.kpm

import org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi
import java.io.Serializable

sealed interface IdeaKpmFragmentCoordinates : Serializable, IdeaKpmDependencyCoordinates {
    konst module: IdeaKpmModuleCoordinates
    konst fragmentName: String
}

@InternalKotlinGradlePluginApi
data class IdeaKpmFragmentCoordinatesImpl(
    override konst module: IdeaKpmModuleCoordinates,
    override konst fragmentName: String
) : IdeaKpmFragmentCoordinates {

    override fun toString(): String = path

    @InternalKotlinGradlePluginApi
    companion object {
        private const konst serialVersionUID = 0L
    }
}

konst IdeaKpmFragmentCoordinates.path: String
    get() = "${module.path}/$fragmentName"
