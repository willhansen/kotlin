// WITH_STDLIB
konst String.foo: String
    get() = this

abstract class A {
    abstract konst x: String

    konst y by x::foo
}

var storage = "OK"

class B : A() {
    override var x: String
        get() = storage
        set(konstue) { storage = konstue }
}

fun box(): String {
    konst b = B()
    b.x = "fail"
    return b.y
}
