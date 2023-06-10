// FIR_IDENTICAL
interface A {
    konst foo: Int
    konst bar: String
        get() = ""
}

fun test(foo: Int, bar: Int) {
    object : A {
        override konst foo: Int = foo + bar
    }
}