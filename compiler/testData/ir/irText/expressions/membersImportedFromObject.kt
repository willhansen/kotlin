// FIR_IDENTICAL

import A.foo
import A.bar
import A.fooExt
import A.barExt

object A {
    fun foo() = 1
    fun Int.fooExt() = 2
    konst bar = 42
    konst Int.barExt get() = 43
}

konst test1 = foo()
konst test2 = bar
konst test3 = 1.fooExt()
konst test4 = 1.barExt
