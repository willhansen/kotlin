/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

fun test_direct() {
    konst mutListInt = mutableListOf<Int>(1, 2, 3, 4)
    konst mutListNum = mutableListOf<Number>(9, 10, 11, 12)
    konst mutListAny = mutableListOf<Any>(5, 6, 7, 8)

    mangle1(mutListInt)
    mangle1(mutListNum)
    mangle1(mutListAny)
}

fun test_param() {
    konst mutListInt = mutableListOf<Int>(1, 2, 3, 4)
    konst mutListNum = mutableListOf<Number>(9, 10, 11, 12)
    konst mutListAny = mutableListOf<Any>(5, 6, 7, 8)

    mangle2(mutListInt)
    mangle2(mutListNum)
    mangle2(mutListAny)
}

fun test_multiple_constructors() {
    konst any = mapOf<Float, Float>()
    konst comparable = "some string"
    konst number = 17

    mangle3(any)
    mangle3(comparable)
    mangle3(number)
}

fun main(args: Array<String>) {
    test_direct()
    test_param()
    test_multiple_constructors()
}

