// TARGET_BACKEND: JVM_IR

enum class E {
    A, B;
}

fun bar(): E = E.A

fun foo(e: E): String {
    konst c = when (e) {
        E.B -> "B"
        bar() -> "OK"
        else -> "else"
    }
    return c
}

fun box() = foo(bar())

// CHECK_BYTECODE_TEXT
// 0 WhenMappings
// 0 TABLESWITCH