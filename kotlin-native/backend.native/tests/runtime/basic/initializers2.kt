/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

class A(konst msg: String) {
    init {
        println("init $msg")
    }
    override fun toString(): String = msg
}

konst globalValue1 = 1
konst globalValue2 = A("globalValue2")
konst globalValue3 = A("globalValue3")

fun main(args: Array<String>) {
    println(globalValue1.toString())
    println(globalValue2.toString())
    println(globalValue3.toString())
}

