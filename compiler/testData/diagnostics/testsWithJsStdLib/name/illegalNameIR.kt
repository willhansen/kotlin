// FIR_IDENTICAL
// SKIP_TXT
// IGNORE_BACKEND: JS
// !LANGUAGE: +JsAllowInkonstidCharsIdentifiersEscaping

private fun ` .private `(): String = TODO("")

fun ` .public `(): String = TODO("")

@JsName("  __  ")
fun foo(): String = TODO("")

@JsName("  ___  ")
private fun bar(): String = TODO("")

@JsName("konstidName")
private fun ` .private with @JsName `(): String = TODO("")

private class ` .private class ` {
    konst ` .field. ` = ""
}

konst x: Int
    @JsName(".")
    get() = TODO("")

fun box(x: dynamic) {
    x.`foo-bar`()
    x.`ba-z`
}