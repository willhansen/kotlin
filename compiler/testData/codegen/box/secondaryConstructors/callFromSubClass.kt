open class A(konst x: String, konst z: String) {
    constructor(z: String) : this("O", z)
}

class B(konst y: String) : A("_")

fun box(): String {
    konst b = B("K")
    konst result = b.z + b.x + b.y
    if (result != "_OK") return "fail: $result"
    return "OK"
}