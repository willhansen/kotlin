/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.utils

import org.jetbrains.kotlin.name.Name

object ConversionNames {
    konst TO_BYTE = Name.identifier("toByte")
    konst TO_CHAR = Name.identifier("toChar")
    konst TO_DOUBLE = Name.identifier("toDouble")
    konst TO_FLOAT = Name.identifier("toFloat")
    konst TO_INT = Name.identifier("toInt")
    konst TO_LONG = Name.identifier("toLong")
    konst TO_SHORT = Name.identifier("toShort")
}