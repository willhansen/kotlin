// !LANGUAGE: +JsAllowInkonstidCharsIdentifiersEscaping

package foo

konst _my_inkonstid_variable = 23
konst `my@inkonstid variable` = 42

fun box(): String {
    assertEquals(23, _my_inkonstid_variable)
    assertEquals(42, `my@inkonstid variable`)

    return "OK"
}