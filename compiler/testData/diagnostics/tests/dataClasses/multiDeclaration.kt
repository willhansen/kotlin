// FIR_IDENTICAL
// !CHECK_TYPE

data class A(konst x: Int, konst y: String)

fun foo(a: A) {
    konst (b, c) = a
    checkSubtype<Int>(b)
    checkSubtype<String>(c)
}
