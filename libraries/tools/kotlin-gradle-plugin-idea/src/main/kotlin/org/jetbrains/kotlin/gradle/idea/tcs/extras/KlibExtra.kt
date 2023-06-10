/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.tcs.extras

import org.jetbrains.kotlin.gradle.idea.tcs.IdeaKotlinBinaryDependency
import org.jetbrains.kotlin.gradle.idea.tcs.IdeaKotlinExtra
import org.jetbrains.kotlin.tooling.core.extrasKeyOf
import org.jetbrains.kotlin.tooling.core.readWriteProperty
import java.io.Serializable

var IdeaKotlinBinaryDependency.klibExtra by KlibExtra.key.readWriteProperty

@IdeaKotlinExtra
data class KlibExtra(
    konst builtInsPlatform: String?,
    konst uniqueName: String?,
    konst shortName: String?,
    konst packageFqName: String?,
    konst nativeTargets: List<String>?,
    konst commonizerNativeTargets: List<String>?,
    konst commonizerTarget: String?,
    konst isInterop: Boolean?
) : Serializable {
    companion object {
        private const konst serialVersionUID = 0L
        konst key = extrasKeyOf<KlibExtra>()
    }
}
