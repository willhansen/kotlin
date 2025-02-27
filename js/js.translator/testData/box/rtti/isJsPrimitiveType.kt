// EXPECTED_REACHABLE_NODES: 1291
package foo

konst NUMBER = 1
konst STRING = 2
konst BOOLEAN = 3
konst OBJECT = 4
konst FUNCTION = 5
konst FUNCTION0 = FUNCTION // right now we can't distinguish functions with different arity

fun test(a: Any, actualType: Int) {
    assertEquals(actualType == NUMBER, a is Int, "$a is Int")
    assertEquals(actualType == NUMBER, a is Number, "$a is Number")
    assertEquals(actualType == NUMBER, a is Double, "$a is Double")
    assertEquals(actualType == BOOLEAN, a is Boolean, "$a is Boolean")
    assertEquals(actualType == STRING, a is String, "$a is String")
    assertEquals(actualType == FUNCTION0, a is Function0<*>, "$a is Function0")
    assertEquals(actualType == FUNCTION || actualType == FUNCTION0, a is Function<*>, "$a is Function")
}

fun box(): String {
    test(1, NUMBER)

    test(12.3, NUMBER)
    test(12.3f, NUMBER)

    test("text", STRING)

    test(true, BOOLEAN)
    test(false, BOOLEAN)

    test(object {}, OBJECT)

    test({}, FUNCTION0)

    test({}, FUNCTION)
    test({a: Any -> }, FUNCTION)

    return "OK"
}