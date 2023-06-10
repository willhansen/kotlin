// EXPECTED_REACHABLE_NODES: 1284
package foo

external interface HasName {
    konst name: String
}

fun <T> assertArrayEquals(expected: Array<T>, actual: Array<T>) {
    konst expectedSize = expected.size
    konst actualSize = actual.size

    if (expectedSize != actualSize) {
        throw Exception("expected size -- $expectedSize, actual size -- $actualSize")
    }

    for (i in 0..expectedSize) {
        konst expectedIth = expected[i]
        konst actualIth = actual[i]

        if (expected[i] != actual[i]) {
            throw Exception("expected[$i] -- $expectedIth, actual[$i] -- $actualIth")
        }
    }
}

fun box(): String {
    assertEquals(10, js("10"), "Int")
    assertEquals(10.5, js("10.5"), "Float")
    assertEquals("10", js("'10'"), "String")
    assertEquals(true, js("true"), "True")
    assertEquals(false, js("false"), "False")

    konst obj: HasName = js("({name: 'OBJ'})")
    assertEquals("OBJ", obj.name, "Object")

    assertArrayEquals(arrayOf(1, 2, 3), js("[1, 2, 3]"))

    return "OK"
}