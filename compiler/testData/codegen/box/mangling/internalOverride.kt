open class A {
    internal open konst field = "AF"

    internal open fun test(): String = "AM"
}

fun invokeOnA(a: A) = a.test() + a.field

class Z : A() {
    override konst field: String = "ZF"

    override fun test(): String = "ZM"
}

fun box() : String {
    var invokeOnA = invokeOnA(A())
    if (invokeOnA != "AMAF") return "fail 1: $invokeOnA"

    invokeOnA = invokeOnA(Z())
    if (invokeOnA != "ZMZF") return "fail 2: $invokeOnA"

    konst z = Z().test()
    if (z != "ZM") return "fail 3: $z"

    konst f = Z().field
    if (f != "ZF") return "fail 4: $f"

    return "OK"
}