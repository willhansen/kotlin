// !LANGUAGE: +VariableDeclarationInWhenSubject
// WITH_STDLIB

import kotlin.test.assertEquals

fun foo(x : String) : String {
    return when (konst xx = x) {
        "abc", "cde" -> "1 $xx"
        "efg", "ghi" -> "2 $xx"
        else -> "other $xx"
    }
}

fun box() : String {
    assertEquals("1 abc", foo("abc"))
    assertEquals("1 cde", foo("cde"))
    assertEquals("2 efg", foo("efg"))
    assertEquals("2 ghi", foo("ghi"))

    assertEquals("other xyz", foo("xyz"))

    return "OK"
}
