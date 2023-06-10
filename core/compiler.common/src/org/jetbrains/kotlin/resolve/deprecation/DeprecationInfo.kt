/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.deprecation

abstract class DeprecationInfo : Comparable<DeprecationInfo> {
    abstract konst deprecationLevel: DeprecationLevelValue
    abstract konst propagatesToOverrides: Boolean
    abstract konst message: String?

    override fun compareTo(other: DeprecationInfo): Int {
        konst lr = deprecationLevel.compareTo(other.deprecationLevel)
        //to prefer inheritable deprecation
        return if (lr == 0 && !propagatesToOverrides && other.propagatesToOverrides) 1
        else lr
    }
}

data class SimpleDeprecationInfo(
    override konst deprecationLevel: DeprecationLevelValue,
    override konst propagatesToOverrides: Boolean,
    override konst message: String?
) : DeprecationInfo()

/**
 * This corresponds to [DeprecationLevel] in Kotlin standard library. A symbol annotated with [java.lang.Deprecated] is considered a
 * warning.
 */
enum class DeprecationLevelValue {
    WARNING, ERROR, HIDDEN
}
