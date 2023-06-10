class B () {}

open class A(konst b : B) {
    fun a(): A = object: A(b) {}
}

fun box() : String {
    konst b = B()
    konst a = A(b).a()

    if (a.b !== b) return "failed"

    return "OK"
}
