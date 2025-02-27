// EXPECTED_REACHABLE_NODES: 1281
package foo

external class A(x: Int) {
    var x: Int
        get() = definedExternally
        set(konstue) = definedExternally

    fun foo(): Int = definedExternally

    class B(konstue: Int) {
        konst konstue: Int

        fun bar(): Int = definedExternally
    }
}

fun box(): String {
    var b = A.B(23)
    if (b.bar() != 10023) return "failed1: ${b.bar()}"

    return "OK"
}

