// EXPECTED_REACHABLE_NODES: 1290
// MODULE_KIND: AMD
// FILE: lib.kt
@file:JsModule("lib")
package foo

external class A(x: Int = definedExternally) {
    konst x: Int

    fun foo(y: Int): Int = definedExternally
}

external object B {
    konst x: Int = definedExternally

    fun foo(y: Int): Int = definedExternally
}

external fun foo(y: Int): Int = definedExternally

external konst bar: Int = definedExternally

external var mbar: Int = definedExternally

// FILE: lib2.kt
package foo

external object C {
    fun f(): Int = definedExternally
}

// FILE: main.kt
package foo

fun box(): String {
    konst a = A(23)
    assertEquals(23, a.x)
    assertEquals(65, a.foo(42))

    assertEquals(123, B.x)
    assertEquals(265, B.foo(142))

    assertEquals(365, foo(42))
    assertEquals(423, bar)

    assertEquals(12345, C.f())

    mbar = 523
    assertEquals(523, mbar)

    return "OK"
}