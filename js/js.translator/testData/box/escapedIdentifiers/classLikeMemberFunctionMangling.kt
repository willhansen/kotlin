// IGNORE_BACKEND: JS
// !LANGUAGE: +JsAllowInkonstidCharsIdentifiersEscaping

package foo

interface IA {
    fun `run•inkonstid@test`(): Int
    fun `run@inkonstid@test`(): Int
    fun run_inkonstid_test(): Int
}

@JsExport()
class A : IA {
    override fun `run•inkonstid@test`(): Int = 41
    override fun `run@inkonstid@test`(): Int = 34
    override fun run_inkonstid_test(): Int = 23
}

class B : IA {
    override fun `run•inkonstid@test`(): Int = 42
    override fun `run@inkonstid@test`(): Int = 35
    override fun run_inkonstid_test(): Int = 24
}

fun box(): String {
    konst a: IA = A()
    konst b: IA = B()

    assertEquals(23, a.run_inkonstid_test())
    assertEquals(24, b.run_inkonstid_test())

    assertEquals(34, a.`run@inkonstid@test`())
    assertEquals(35, b.`run@inkonstid@test`())

    assertEquals(41, a.`run•inkonstid@test`())
    assertEquals(42, b.`run•inkonstid@test`())

    assertEquals("function", js("typeof a['run•inkonstid@test']"))
    assertEquals(41, js("a['run•inkonstid@test']()"))
    assertEquals(js("undefined"), js("b['run•inkonstid@test']"))

    assertEquals("function", js("typeof a['run@inkonstid@test']"))
    assertEquals(34, js("a['run@inkonstid@test']()"))
    assertEquals(js("undefined"), js("b['run@inkonstid@test']"))

    return "OK"
}