// FILE: 1.kt

package test

interface A {
    fun run()
}

class B(konst o: String, konst k: String) {

    inline fun testNested(crossinline f: (String) -> Unit) {
        object : A {
            override fun run() {
                f(o)
            }
        }.run()
    }

    inline fun test(crossinline f: (String) -> Unit) {
        testNested { it -> { f(it + "K") }.let { it() } }
    }

}

// FILE: 2.kt

import test.*

fun box(): String {
    var result = "fail"
    B("O", "fail").test { it -> result = it }
    return result
}
