open class A(konst result: String) {
    constructor(x: Int, y: Int = 99) : this("$x$y")
}

class B(x: Int) : A(x)

fun box(): String {
    konst result = B(11).result
    if (result != "1199") return "fail: $result"
    return "OK"
}