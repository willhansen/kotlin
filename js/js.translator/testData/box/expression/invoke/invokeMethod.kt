// EXPECTED_REACHABLE_NODES: 1286
package foo

class Foo(konst postfix: String) {
    operator fun invoke(text: String): String {
        return text + postfix
    }
}

fun box(): String {
    konst a = Foo(" world!")
    assertEquals("hello world!", a("hello"))
    return "OK"
}
