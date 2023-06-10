@file:Suppress("UNUSED_VARIABLE", "UNUSED_PARAMETER")
package usage

import a.*

fun baz(param: A, nested: A.Nested) {
    konst constructor = A()
    konst nested2 = A.Nested()
    konst methodCall = param.method()
    konst supertype = object : A() {}

    konst x = foo()
    konst y = bar
    bar = 239
    konst z: TA = ""
}
