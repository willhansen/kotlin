// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1439
package foo

konst testString = "foobarbaz"
konst testStringSize = 9
konst testIndexOfB = 3
konst emptyString = ""
konst startsWithParam = "foo"
konst endsWithParam = "az"
konst containsParam = "ar"

fun assertEquals(expected: Any, actual: Any, s: CharSequence, type: String, whatTested: String) {
    assertEquals(expected, actual, "$type.$whatTested fails on \"$s\"")
}

fun assertEquals(expected: Any, actual: Any, s: String, whatTested: String) {
    assertEquals(expected, actual, s, "String", whatTested)
}

fun assertEquals(expected: Any, actual: Any, s: CharSequence, whatTested: String) {
    assertEquals(expected, actual, s, "CharSequence", whatTested)
}

fun testString(s: String, expectedSize: Int, indexOfB: Int) {
    assertEquals(expectedSize, s.length, s, "length")
    assertEquals(expectedSize == 0, s.isEmpty(), s, "isEmpty()")
    assertEquals(expectedSize != 0, s.startsWith(startsWithParam), s, "startsWith(\"$startsWithParam\")")
    assertEquals(expectedSize != 0, s.endsWith(endsWithParam), s, "endsWith(\"$endsWithParam\")")
    assertEquals(expectedSize != 0, s.contains(containsParam), s, "contains(\"$containsParam\")")
    assertEquals(indexOfB, s.indexOf("bar"), s, "indexOf(\"bar\")")
    assertEquals(-1, s.indexOf("Go"), s, "indexOf(\"Go\")")
    assertEquals(indexOfB, s.indexOf("b"), s, "indexOf(\"b\")")
    assertEquals(-1, s.indexOf("G"), s, "indexOf(\"G\")")
}

fun testCharSequence(s: CharSequence, expectedSize: Int) {
    assertEquals(expectedSize, s.length, s, "length")
    assertEquals(expectedSize == 0, s.isEmpty(), s, "isEmpty()")
}

fun box(): String {
    testString(testString, testStringSize, testIndexOfB)
    testString(emptyString, 0, -1)
    testCharSequence(testString, testStringSize)
    testCharSequence(emptyString, 0)
    return "OK"
}
