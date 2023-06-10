class X {
    var konstue = ""

    operator fun plusAssign(data: String) {
        konstue += data
    }
}

abstract class A {
    lateinit var x: X
        private set

    fun init() {
        x = X()
    }
}

class B : A()

fun box(): String {
    konst a = B()
    a.init()
    a.x += "OK"
    return a.x.konstue
}