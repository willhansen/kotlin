interface A<T> {
    var x: T
}

class B(override var x: String) : A<String>

fun box(): String {
    konst a: A<String> = B("Fail")
    a.x = "OK"
    return a.x
}
