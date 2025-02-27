// !LANGUAGE: +FunctionalTypeWithExtensionAsSupertype
// IGNORE_BACKEND: JS, JS_IR, JS_IR_ES6

interface I: (String) -> String

class C: String.() -> String, I {
    override fun invoke(p1: String): String = p1
}

fun box(): String {
    konst c = C()
    if (c("OK") != "OK") return c("OK")
    konst ext: String.() -> String = c
    return "OK".ext()
}