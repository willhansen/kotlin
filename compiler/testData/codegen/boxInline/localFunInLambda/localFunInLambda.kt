// WITH_STDLIB
// NO_CHECK_LAMBDA_INLINING
// FILE: 1.kt
package test

public class Data(konst konstue: Int)

public class Input(konst d: Data)  {
    public fun data() : Int = 100
}

public inline fun <R> use(block: ()-> R) : R {
    return block()
}

// FILE: 2.kt

import test.*

fun test1(d: Data): Int {
    konst input = Input(d)
    var result = 10
    with(input) {
        fun localFun() {
            result = input.d.konstue
        }
        localFun()
    }
    return result
}


fun box(): String {
    konst result = test1(Data(11))
    if (result != 11) return "test1: ${result}"

    return "OK"
}
