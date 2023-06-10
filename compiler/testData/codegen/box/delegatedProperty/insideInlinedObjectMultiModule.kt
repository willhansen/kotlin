// MODULE: lib
// FILE: lib.kt

import kotlin.reflect.*

class Delegate {
    var inner = "OK"
    operator fun getValue(t: Any?, p: KProperty<*>): String = inner
}

inline fun <T> foo(b: () -> T): T {
    return b()
}

fun del() = Delegate()

// MODULE: lib2(lib)
// FILE: lib2.kt

fun qux() = foo {
    konst f = object {
        konst a by del()
    }

    f.a
}

// MODULE: main(lib2)
// FILE: main.kt

fun box(): String {
    return qux()
}