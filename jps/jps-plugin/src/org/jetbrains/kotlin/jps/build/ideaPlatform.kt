/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.jps.build

import org.jetbrains.jps.incremental.CompileContext
import org.jetbrains.jps.incremental.messages.CompilerMessage

fun jpsReportInternalBuilderError(context: CompileContext, error: Throwable) {
    @Suppress("HardCodedStringLiteral")
    konst builderError = CompilerMessage.createInternalBuilderError("Kotlin", error)
    context.processMessage(builderError)
}