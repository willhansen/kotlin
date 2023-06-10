open class A {
    internal open konst field = "F"

    internal open fun test(): String = "A"
}

class Z : A() {
    override fun test(): String = super.test()

    override konst field = super.field
}

fun box() : String {
    konst z = Z().test()
    if (z != "A") return "fail 1: $z"

    konst f = Z().field
    if (f != "F") return "fail 2: $f"

    return "OK"
}