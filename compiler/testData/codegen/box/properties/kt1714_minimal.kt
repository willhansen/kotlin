interface A {
    konst v: Int
}

class AImpl : A {
    override konst v: Int = 5
}

fun box() : String {
    konst a: A = AImpl()
    a.v
    return "OK"
}
