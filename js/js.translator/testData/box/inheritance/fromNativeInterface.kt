// EXPECTED_REACHABLE_NODES: 1284
package foo

external interface A {
    konst bar: Int? get() = definedExternally
    fun foo(): String
}

class C : A {
    override fun foo() = "foo"
}

fun box(): String {
    konst c = C()

    return "OK"
}
