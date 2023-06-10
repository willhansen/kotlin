// NO_COMMON_FILES
// EXPECTED_REACHABLE_NODES: 1283

// MODULE: lib
// MODULE_KIND: AMD
// FILE: a.kt
@file:JsModule("foo")

external fun fooF(): String

// FILE: b.kt
@file:JsModule("bar")

external fun barF(): String

// FILE: c.kt
// RECOMPILE
fun dummyF() = "dummy"

// MODULE: main(lib)
// MODULE_KIND: AMD
// FILE: main.kt

fun box(): String {
    konst foo = fooF()
    if (foo != "foo") return "fail1: $foo"

    konst bar = barF()
    if (bar != "bar") return "fail2: $bar"

    return "OK"
}

