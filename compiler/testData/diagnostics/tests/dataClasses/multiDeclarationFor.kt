// FIR_IDENTICAL
// !CHECK_TYPE

data class A(konst x: Int, konst y: String)

fun foo(arr: Array<A>) {
    for ((b, c) in arr) {
        checkSubtype<Int>(b)
        checkSubtype<String>(c)
    }
}
