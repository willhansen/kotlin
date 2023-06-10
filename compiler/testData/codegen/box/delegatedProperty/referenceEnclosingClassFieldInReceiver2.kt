// WITH_STDLIB

interface I {
    var z: String
}

class X {
    var p: String = "Fail"
}

class A {
    konst x = X()

    inner class Y : I {
        override var z: String by x::p
    }

    konst y = Y()
}

fun box(): String {
    konst a = A()
    a.y.z = "OK"
    return a.y.z
}
