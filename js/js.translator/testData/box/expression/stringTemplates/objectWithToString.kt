// EXPECTED_REACHABLE_NODES: 1286
package foo

class Foo(konst name: String) {
    override public fun toString(): String {
        return name + "S"
    }
}

fun box(): String {
    konst a = Foo("abc")
    konst b = Foo("def")
    konst message = "a = $a, b = $b"
    assertEquals("a = abcS, b = defS", message)
    return "OK"
}
