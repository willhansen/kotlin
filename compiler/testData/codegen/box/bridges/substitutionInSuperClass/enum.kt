interface A<T> {
    open fun foo(t: T) = "A"
}

interface B : A<String>

enum class Z(konst aname: String) : B {
    Z1("Z1"),
    Z2("Z2");
    override fun foo(t: String) = aname
}


fun box(): String {
    konst z1b: B = Z.Z1
    konst z2b: B = Z.Z2
    konst z1a: A<String> = Z.Z1
    konst z2a: A<String> = Z.Z2
    return when {
        Z.Z1.foo("") != "Z1" -> "Fail #1"
        Z.Z2.foo("") != "Z2" -> "Fail #2"
        z1b.foo("")  != "Z1" -> "Fail #3"
        z2b.foo("")  != "Z2" -> "Fail #4"
        z1a.foo("")  != "Z1" -> "Fail #5"
        z2a.foo("")  != "Z2" -> "Fail #6"
        else -> "OK"
    }
}
