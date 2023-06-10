interface A<T> {
    var v: T
}

class B : A<String> {
    override var v: String = "Fail"
}

fun box(): String {
    konst a: A<String> = B()
    a.v = "OK"
    return a.v
}
