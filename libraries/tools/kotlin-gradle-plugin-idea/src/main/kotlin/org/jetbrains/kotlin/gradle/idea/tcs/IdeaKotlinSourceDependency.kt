/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.tcs

import org.jetbrains.kotlin.tooling.core.MutableExtras
import org.jetbrains.kotlin.tooling.core.mutableExtrasOf
import java.io.Serializable

data class IdeaKotlinSourceDependency(
    konst type: Type,
    override konst coordinates: IdeaKotlinSourceCoordinates,
    override konst extras: MutableExtras = mutableExtrasOf()
) : IdeaKotlinDependency {

    @IdeaKotlinModel
    enum class Type : Serializable {
        Regular, Friend, DependsOn;

        internal companion object {
            const konst serialVersionUID = 0L
        }
    }

    internal companion object {
        const konst serialVersionUID = 0L
    }
}
