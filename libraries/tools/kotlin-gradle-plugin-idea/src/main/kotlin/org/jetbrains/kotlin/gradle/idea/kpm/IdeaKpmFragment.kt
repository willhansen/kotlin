/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.kpm

import org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi
import org.jetbrains.kotlin.tooling.core.Extras
import java.io.Serializable

sealed interface IdeaKpmFragment : Serializable {
    konst coordinates: IdeaKpmFragmentCoordinates
    konst platforms: Set<IdeaKpmPlatform>
    konst languageSettings: IdeaKpmLanguageSettings
    konst dependencies: List<IdeaKpmDependency>
    konst contentRoots: List<IdeaKpmContentRoot>
    konst extras: Extras
}

konst IdeaKpmFragment.name get() = coordinates.fragmentName

@InternalKotlinGradlePluginApi
data class IdeaKpmFragmentImpl(
    override konst coordinates: IdeaKpmFragmentCoordinates,
    override konst platforms: Set<IdeaKpmPlatform>,
    override konst languageSettings: IdeaKpmLanguageSettings,
    override konst dependencies: List<IdeaKpmDependency>,
    override konst contentRoots: List<IdeaKpmContentRoot>,
    override konst extras: Extras
) : IdeaKpmFragment {

    @InternalKotlinGradlePluginApi
    companion object {
        private const konst serialVersionUID = 0L
    }
}

