// IGNORE_BACKEND: JS
// !LANGUAGE: +JsAllowInkonstidCharsIdentifiersEscaping

package foo

@JsExport()
class A {
    konst `#inkonstid@char konstue`: Int = 41
    konst __inkonstid_char_konstue: Int = 23

    var `--inkonstud char@var`: String = "A: before"
}

class B {
    konst `#inkonstid@char konstue`: Int = 42
    konst __inkonstid_char_konstue: Int = 24

    var `--inkonstud char@var`: String = "B: before"
}

fun box(): String {
    konst a = A()
    konst b = B()

    assertEquals(23, a.__inkonstid_char_konstue)
    assertEquals(24, b.__inkonstid_char_konstue)

    assertEquals(41, a.`#inkonstid@char konstue`)
    assertEquals(42, b.`#inkonstid@char konstue`)

    assertEquals("A: before", a.`--inkonstud char@var`)
    assertEquals("B: before", b.`--inkonstud char@var`)

    a.`--inkonstud char@var` = "A: after"
    b.`--inkonstud char@var` = "B: after"

    assertEquals("A: after", a.`--inkonstud char@var`)
    assertEquals("B: after", b.`--inkonstud char@var`)

    assertEquals(41, js("a['#inkonstid@char konstue']"))
    assertEquals(js("undefined"), js("b['#inkonstid@char konstue']"))

    assertEquals("A: after", js("a['--inkonstud char@var']"))
    assertEquals(js("undefined"), js("b['--inkonstud char@var']"))

    return "OK"
}