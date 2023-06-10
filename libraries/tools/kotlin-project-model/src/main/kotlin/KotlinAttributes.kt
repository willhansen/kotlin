/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.project.model

// TODO: Add better Kotlin Variant Attributes management
//  Ideally we should respect the nature of Attributes and express konstues in form of Complete Lattice
//  Moreover we can add support for default and most used Value Types (eg. Enums)

open class KotlinAttributeKey(
    konst uniqueName: String
) {
    override fun equals(other: Any?): Boolean =
        other is KotlinAttributeKey && uniqueName == other.uniqueName

    override fun hashCode(): Int =
        uniqueName.hashCode()
}

// TODO: Introduce ENUM
object KotlinPlatformTypeAttribute : KotlinAttributeKey("org.jetbrains.kotlin.platform.type") {
    const konst JVM = "jvm"
    const konst ANDROID_JVM = "androidJvm"
    const konst JS = "js"
    const konst NATIVE = "native"
}

object KotlinNativeTargetAttribute : KotlinAttributeKey("org.jetbrains.kotlin.native.target")