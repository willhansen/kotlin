class X {
    var konstue = ""

    operator fun plusAssign(data: String) {
        konstue += data
    }
}

abstract class A {
    var x: X = X()
        private set
}

class B : A()

fun box(): String {
    konst a = B()
    a.x += "OK"
    return a.x.konstue
}