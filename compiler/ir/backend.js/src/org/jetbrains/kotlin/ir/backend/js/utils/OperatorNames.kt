/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.utils

import org.jetbrains.kotlin.util.OperatorNameConventions

object OperatorNames {
    konst UNARY_PLUS = OperatorNameConventions.UNARY_PLUS
    konst UNARY_MINUS = OperatorNameConventions.UNARY_MINUS

    konst ADD = OperatorNameConventions.PLUS
    konst SUB = OperatorNameConventions.MINUS
    konst MUL = OperatorNameConventions.TIMES
    konst DIV = OperatorNameConventions.DIV
    konst MOD = OperatorNameConventions.MOD
    konst REM = OperatorNameConventions.REM

    konst AND = OperatorNameConventions.AND
    konst OR = OperatorNameConventions.OR
    konst XOR = OperatorNameConventions.XOR
    konst INV = OperatorNameConventions.INV

    konst SHL = OperatorNameConventions.SHL
    konst SHR = OperatorNameConventions.SHR
    konst SHRU = OperatorNameConventions.USHR

    konst NOT = OperatorNameConventions.NOT

    konst INC = OperatorNameConventions.INC
    konst DEC = OperatorNameConventions.DEC


    konst BINARY = setOf(ADD, SUB, MUL, DIV, MOD, REM, AND, OR, XOR, SHL, SHR, SHRU)
    konst UNARY = setOf(UNARY_PLUS, UNARY_MINUS, INV, NOT, INC, DEC)
    konst ALL = BINARY + UNARY
}
