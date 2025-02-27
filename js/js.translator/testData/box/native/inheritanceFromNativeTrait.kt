// DONT_TARGET_EXACT_BACKEND: WASM
// WASM_MUTE_REASON: IMPLEMENTING_EXTERNAL_INTERFACE
// EXPECTED_REACHABLE_NODES: 1297
package foo

external interface NativeTrait {
    konst foo: String
    fun bar(a: Int): Any

    @JsName("boo")
    fun baz(): String
}

interface Trait : NativeTrait

class Class : NativeTrait {
    override konst foo: String = "Class().foo"
    override fun bar(a: Int): Any = "Class().bar($a)"
    override fun baz(): String = "Class().boo()"
}

class AnotherClass : Trait {
    override konst foo: String = "AnotherClass().foo"
    override fun bar(a: Int): Any = "AnotherClass().bar($a)"
    override fun baz(): String = "AnotherClass().boo()"
}

fun <T : NativeTrait> test(c: T, className: String) {
    assertEquals("$className().foo", c.foo)
    assertEquals("$className().bar(3)", c.bar(3))
    assertEquals("$className().boo()", c.baz())

    konst t: NativeTrait = c
    assertEquals("$className().foo", t.foo)
    assertEquals("$className().bar(3)", t.bar(3))
    assertEquals("$className().boo()", t.baz())
}

fun box(): String {
    test(Class(), "Class")
    test(AnotherClass(), "AnotherClass")

    return "OK"
}
