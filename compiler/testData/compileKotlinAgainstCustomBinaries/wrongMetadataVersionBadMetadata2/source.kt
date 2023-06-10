package usage

import a.*

fun baz(param: A, nested: A.Nested) {
    konst constructor = A()
    konst nested = A.Nested()
    konst quux = param.getQuux()
    konst methodCall = param.method()
    konst supertype = object : A() {}

    konst x = foo()
    konst y = bar
    bar = 239
    konst z: TA = ""
}
