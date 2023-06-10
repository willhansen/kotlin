// IGNORE_BACKEND: JS_IR_ES6
// IGNORE_BACKEND: JS_IR
// EXPECTED_REACHABLE_NODES: 1299
package foo

open class A {
    fun foo() = "A::foo"
}

class B : A() {
    fun boo() = "B::boo"

    konst far = { foo() }
    konst gar = { boo() }
}


fun box(): String {
    konst b = B()
    konst f = b.far
    konst g = b.gar

    assertEquals("A::foo", f())
    assertEquals("B::boo", g())

    konst fs: String = ekonst("B\$far\$lambda").toString()
    konst gs = (ekonst("B\$gar\$lambda").toString() as String).replaceAll("boo", "foo").replaceAll("gar", "far")

    assertEquals(gs, fs)

    return "OK"
}


// Helpers

inline fun String.replace(regexp: RegExp, replacement: String): String = asDynamic().replace(regexp, replacement)

fun String.replaceAll(regexp: String, replacement: String): String = replace(RegExp(regexp, "g"), replacement)

external class RegExp(regexp: String, flags: String)
