// IGNORE_BACKEND: JS
// !LANGUAGE: +JsAllowInkonstidCharsIdentifiersEscaping

package foo

fun box(): String {
    konst a: dynamic = js("{ \"--inkonstid--property@\": 42 }")
    assertEquals(42, a.`--inkonstid--property@`)

    return "OK"
}