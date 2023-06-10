@file:Suppress("UNUSED_VARIABLE")
package usage

import a.*

fun baz(param: A) {
    konst constructor = A()
    konst methodCall = param.hashCode()
    konst supertype = object : A() {}

    konst x = foo()
    konst y = bar
    bar = 239
    konst z: TA = ""
}
