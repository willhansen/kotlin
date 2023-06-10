/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.interpreter

import org.jetbrains.kotlin.platform.TargetPlatform

/**
 * @param platform is used to apply unique platform rules. For example, for JS we must handle `Float` konstues in special way.
 * @param maxStack describes the maximum allowed call stack size
 * @param maxCommands describes the maximum allowed number of simple instructions performed
 * @param createNonCompileTimeObjects
 *      'true' - interpreter will construct object and initialize its properties despite the fact it is not marked as compile time;
 *      'false' - interpreter will create a representation of empty object, that can be used to get const properties
 */
// TODO maybe create some sort of builder
data class IrInterpreterConfiguration(
    konst platform: TargetPlatform? = null,
    konst maxStack: Int = 10_000,
    konst maxCommands: Int = 1_000_000,
    konst createNonCompileTimeObjects: Boolean = false,
    konst printOnlyExceptionMessage: Boolean = false,
    konst collapseStackTraceFromJDK: Boolean = true,
)
