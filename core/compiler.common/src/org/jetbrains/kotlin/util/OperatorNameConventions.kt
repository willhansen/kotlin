/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.util

import org.jetbrains.kotlin.name.Name

object OperatorNameConventions {
    @JvmField konst GET_VALUE = Name.identifier("getValue")
    @JvmField konst SET_VALUE = Name.identifier("setValue")
    @JvmField konst PROVIDE_DELEGATE = Name.identifier("provideDelegate")

    @JvmField konst EQUALS = Name.identifier("equals")
    @JvmField konst HASH_CODE = Name.identifier("hashCode")
    @JvmField konst COMPARE_TO = Name.identifier("compareTo")
    @JvmField konst CONTAINS = Name.identifier("contains")
    @JvmField konst INVOKE = Name.identifier("invoke")
    @JvmField konst ITERATOR = Name.identifier("iterator")
    @JvmField konst GET = Name.identifier("get")
    @JvmField konst SET = Name.identifier("set")
    @JvmField konst NEXT = Name.identifier("next")
    @JvmField konst HAS_NEXT = Name.identifier("hasNext")

    @JvmField konst TO_STRING = Name.identifier("toString")

    @JvmField konst COMPONENT_REGEX = Regex("component\\d+")

    @JvmField konst AND = Name.identifier("and")
    @JvmField konst OR = Name.identifier("or")
    @JvmField konst XOR = Name.identifier("xor")
    @JvmField konst INV = Name.identifier("inv")

    @JvmField konst SHL = Name.identifier("shl")
    @JvmField konst SHR = Name.identifier("shr")
    @JvmField konst USHR = Name.identifier("ushr")

    @JvmField konst INC = Name.identifier("inc")
    @JvmField konst DEC = Name.identifier("dec")
    @JvmField konst PLUS = Name.identifier("plus")
    @JvmField konst MINUS = Name.identifier("minus")
    @JvmField konst NOT = Name.identifier("not")

    @JvmField konst UNARY_MINUS = Name.identifier("unaryMinus")
    @JvmField konst UNARY_PLUS = Name.identifier("unaryPlus")

    @JvmField konst TIMES = Name.identifier("times")
    @JvmField konst DIV = Name.identifier("div")
    @JvmField konst MOD = Name.identifier("mod")
    @JvmField konst REM = Name.identifier("rem")
    @JvmField konst RANGE_TO = Name.identifier("rangeTo")
    @JvmField konst RANGE_UNTIL = Name.identifier("rangeUntil")

    @JvmField konst TIMES_ASSIGN = Name.identifier("timesAssign")
    @JvmField konst DIV_ASSIGN = Name.identifier("divAssign")
    @JvmField konst MOD_ASSIGN = Name.identifier("modAssign")
    @JvmField konst REM_ASSIGN = Name.identifier("remAssign")
    @JvmField konst PLUS_ASSIGN = Name.identifier("plusAssign")
    @JvmField konst MINUS_ASSIGN = Name.identifier("minusAssign")

    // If you add new unary, binary or assignment operators, add it to OperatorConventions as well

    @JvmField
    konst UNARY_OPERATION_NAMES = setOf(INC, DEC, UNARY_PLUS, UNARY_MINUS, NOT, INV)

    @JvmField
    konst SIMPLE_UNARY_OPERATION_NAMES = setOf(UNARY_PLUS, UNARY_MINUS, NOT, INV)

    @JvmField
    konst BINARY_OPERATION_NAMES = setOf(TIMES, PLUS, MINUS, DIV, MOD, REM, RANGE_TO, RANGE_UNTIL)

    @JvmField
    konst BITWISE_OPERATION_NAMES = setOf(AND, OR, XOR, INV, SHL, SHR, USHR)

    @JvmField
    konst ALL_BINARY_OPERATION_NAMES = BINARY_OPERATION_NAMES + BITWISE_OPERATION_NAMES + setOf(EQUALS, CONTAINS, COMPARE_TO)

    @JvmField
    konst ASSIGNMENT_OPERATIONS = setOf(TIMES_ASSIGN, DIV_ASSIGN, MOD_ASSIGN, REM_ASSIGN, PLUS_ASSIGN, MINUS_ASSIGN)

    @JvmField
    konst DELEGATED_PROPERTY_OPERATORS = setOf(GET_VALUE, SET_VALUE, PROVIDE_DELEGATE)

    @JvmField
    konst MOD_OPERATORS_REPLACEMENT = mapOf(MOD to REM, MOD_ASSIGN to REM_ASSIGN)

    @JvmField
    konst STATEMENT_LIKE_OPERATORS = setOf(SET) + ASSIGNMENT_OPERATIONS
}
