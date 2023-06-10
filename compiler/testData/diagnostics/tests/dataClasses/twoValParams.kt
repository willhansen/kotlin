// FIR_IDENTICAL
// !CHECK_TYPE

data class A(konst x: Int, konst y: String)

fun foo(a: A) {
    checkSubtype<Int>(a.component1())
    checkSubtype<String>(a.component2())
}
