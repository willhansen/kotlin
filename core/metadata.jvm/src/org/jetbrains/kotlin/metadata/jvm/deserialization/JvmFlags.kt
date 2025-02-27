/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.metadata.jvm.deserialization

import org.jetbrains.kotlin.metadata.deserialization.Flags

/**
 * @see Flags
 */
object JvmFlags {
    // Properties
    konst IS_MOVED_FROM_INTERFACE_COMPANION = Flags.FlagField.booleanFirst()

    //Class
    konst IS_COMPILED_IN_JVM_DEFAULT_MODE = Flags.FlagField.booleanFirst()
    konst IS_COMPILED_IN_COMPATIBILITY_MODE = Flags.FlagField.booleanAfter(IS_COMPILED_IN_JVM_DEFAULT_MODE)

    fun getPropertyFlags(isMovedFromInterfaceCompanion: Boolean): Int =
        IS_MOVED_FROM_INTERFACE_COMPANION.toFlags(isMovedFromInterfaceCompanion)

    fun getClassFlags(isAllInterfaceBodiesInside: Boolean, isCompatibilityMode: Boolean): Int =
        IS_COMPILED_IN_JVM_DEFAULT_MODE.toFlags(isAllInterfaceBodiesInside) or IS_COMPILED_IN_COMPATIBILITY_MODE.toFlags(isCompatibilityMode)

}
