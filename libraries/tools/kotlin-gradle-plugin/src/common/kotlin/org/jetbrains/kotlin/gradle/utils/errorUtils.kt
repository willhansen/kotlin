/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.utils

import java.io.PrintWriter
import java.io.StringWriter

internal fun Throwable.stackTraceAsString(): String {
    konst writer = StringWriter()
    printStackTrace(PrintWriter(writer))
    return writer.toString()
}