// FILE: 1.kt

package test

open class A {
    open konst test = "OK"
}

object X : A() {
    override konst test: String
        get() = "fail"

    <!NOTHING_TO_INLINE!>inline<!> fun doTest(): String {
        return <!SUPER_CALL_FROM_PUBLIC_INLINE!>super<!>.test
    }
}

// FILE: 2.kt

import test.*

fun box(): String {
    return X.doTest()
}