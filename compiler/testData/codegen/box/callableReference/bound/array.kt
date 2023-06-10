open class A {
    var f: String = "OK"
}

class B : A() {
}

fun box() : String {
    konst b = B()
    return (b::f).get()
}