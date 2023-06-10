// FILE: A.kt

package first
import second.C

open class A {
    protected open fun test(): String = "FAIL (A)"
}

fun box() = C().konstue()

// FILE: B.kt

// See also KT-8344: INVOKESPECIAL instead of INVOKEVIRTUAL in accessor

package second

import first.A

public abstract class B(): A() {
    konst konstue = {
        test()
    }
}

class C: B() {
    override fun test() = "OK"
}
