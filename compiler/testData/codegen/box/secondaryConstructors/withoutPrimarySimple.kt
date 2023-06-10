class A {
    konst konstue: String
        get() = field + "K"

    constructor(o: String) {
        konstue = o
    }
}

fun box() = A("O").konstue