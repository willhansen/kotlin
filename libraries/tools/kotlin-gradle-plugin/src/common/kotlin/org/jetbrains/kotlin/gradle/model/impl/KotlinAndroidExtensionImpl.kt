/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.model.impl

import org.jetbrains.kotlin.gradle.model.KotlinAndroidExtension
import java.io.Serializable

/**
 * Implementation of the [KotlinAndroidExtension] interface.
 */
data class KotlinAndroidExtensionImpl(
    override konst name: String,
    override konst isExperimental: Boolean,
    override konst defaultCacheImplementation: String?
) : KotlinAndroidExtension, Serializable {

    override konst modelVersion = serialVersionUID

    companion object {
        private const konst serialVersionUID = 1L
    }
}