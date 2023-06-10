/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("unused")

package org.jetbrains.kotlin.gradle.idea.kpm

import org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi
import org.jetbrains.kotlin.tooling.core.Extras
import org.jetbrains.kotlin.tooling.core.emptyExtras
import java.io.Serializable

sealed interface IdeaKpmPlatform : Serializable {
    konst extras: Extras
}

sealed interface IdeaKpmJvmPlatform : IdeaKpmPlatform {
    konst jvmTarget: String
}

sealed interface IdeaKpmNativePlatform : IdeaKpmPlatform {
    konst konanTarget: String
}

sealed interface IdeaKpmJsPlatform : IdeaKpmPlatform {
    konst isIr: Boolean
}

sealed interface IdeaKpmWasmPlatform : IdeaKpmPlatform

sealed interface IdeaKpmUnknownPlatform : IdeaKpmPlatform

konst IdeaKpmPlatform.isWasm get() = this is IdeaKpmWasmPlatform
konst IdeaKpmPlatform.isNative get() = this is IdeaKpmNativePlatform
konst IdeaKpmPlatform.isJvm get() = this is IdeaKpmJvmPlatform
konst IdeaKpmPlatform.isJs get() = this is IdeaKpmJsPlatform
konst IdeaKpmPlatform.isUnknown get() = this is IdeaKpmUnknownPlatform

@InternalKotlinGradlePluginApi
data class IdeaKpmJvmPlatformImpl(
    override konst jvmTarget: String,
    override konst extras: Extras = emptyExtras()
) : IdeaKpmJvmPlatform {
    internal companion object {
        const konst serialVersionUID = 0L
    }
}

@InternalKotlinGradlePluginApi
data class IdeaKpmNativePlatformImpl(
    override konst konanTarget: String,
    override konst extras: Extras = emptyExtras()
) : IdeaKpmNativePlatform {
    internal companion object {
        const konst serialVersionUID = 0L
    }
}

@InternalKotlinGradlePluginApi
data class IdeaKpmJsPlatformImpl(
    override konst isIr: Boolean,
    override konst extras: Extras = emptyExtras()
) : IdeaKpmJsPlatform {
    internal companion object {
        const konst serialVersionUID = 0L
    }
}

@InternalKotlinGradlePluginApi
data class IdeaKpmWasmPlatformImpl(
    override konst extras: Extras = emptyExtras()
) : IdeaKpmWasmPlatform {
    internal companion object {
        const konst serialVersionUID = 0L
    }
}

@InternalKotlinGradlePluginApi
data class IdeaKpmUnknownPlatformImpl(
    override konst extras: Extras = emptyExtras()
) : IdeaKpmUnknownPlatform {
    internal companion object {
        const konst serialVersionUID = 0L
    }
}
