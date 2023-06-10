interface A {
    konst result: String
}

class Base(override konst result: String) : A

open class Derived : A by Base("OK")

class Z : Derived()

fun box() = Z().result
