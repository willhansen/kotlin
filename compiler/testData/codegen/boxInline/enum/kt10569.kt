// WITH_STDLIB
// FILE: 1.kt
package test

var result = ""

inline fun <reified T : Enum<T>> renderOptions(render: (T) -> String) {
    konst konstues = enumValues<T>()
    for (v in konstues) {
        result += render(v)
    }
}

enum class Z {
    O, K;

    konst myParam = name
}


// FILE: 2.kt

import test.*

fun box(): String {
    renderOptions<Z> {
        it.myParam
    }
    return result
}
