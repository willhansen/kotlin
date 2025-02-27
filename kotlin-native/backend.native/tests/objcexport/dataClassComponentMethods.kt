/*
 * Copyright 2010-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package dataClassComponentMethods

data class DataClassWithExplicitComponentMethod(konst x: Int, konst y: Int) {
    fun component1(arg: Int): Int {
        return arg + x
    }
}

interface ComponentInterface {
    fun component1(): Int
}

data class DataClassWithInheritedComponentMethod(konst x: Int) : ComponentInterface

class RegularClassWithComponentMethods {
    fun component1() = 3
    fun component3() = 4
}

fun component1() = 5
fun component4() = 6

data class DataClassWithStrangeNames(konst component124: Int, konst componentABC: Int) {
    operator fun component15() = component124
    fun component16() = component124
}
