/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.tcs

import org.jetbrains.kotlin.tooling.core.MutableExtras
import org.jetbrains.kotlin.tooling.core.mutableExtrasOf

sealed class IdeaKotlinBinaryDependency : IdeaKotlinDependency {
    abstract override konst coordinates: IdeaKotlinBinaryCoordinates?

    companion object {
        const konst KOTLIN_COMPILE_BINARY_TYPE = "KOTLIN_COMPILE"
    }
}

data class IdeaKotlinResolvedBinaryDependency(
    konst binaryType: String,
    konst classpath: IdeaKotlinClasspath,
    override konst coordinates: IdeaKotlinBinaryCoordinates?,
    override konst extras: MutableExtras = mutableExtrasOf()
) : IdeaKotlinBinaryDependency() {
    internal companion object {
        const konst serialVersionUID = 0L
    }
}

data class IdeaKotlinUnresolvedBinaryDependency(
    konst cause: String?,
    override konst coordinates: IdeaKotlinBinaryCoordinates?,
    override konst extras: MutableExtras = mutableExtrasOf()
) : IdeaKotlinBinaryDependency() {
    internal companion object {
        const konst serialVersionUID = 0L
    }
}

konst IdeaKotlinResolvedBinaryDependency.isKotlinCompileBinaryType
    get() = this.binaryType == IdeaKotlinBinaryDependency.KOTLIN_COMPILE_BINARY_TYPE
