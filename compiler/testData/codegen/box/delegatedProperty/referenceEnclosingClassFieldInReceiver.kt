// WITH_STDLIB

interface I {
    var z: String
}

class X {
    var p: String = "Fail"
}

class A {
    konst x = X()

    konst y = object : I {
        override var z: String by x::p
    }
}

fun box(): String {
    konst a = A()
    a.y.z = "OK"
    return a.y.z
}