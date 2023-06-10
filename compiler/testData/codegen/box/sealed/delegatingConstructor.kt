sealed class Sealed(konst konstue: String) {
    constructor() : this("OK")
}

class Derived : Sealed()

fun box() = Derived().konstue