// SEPARATE_SMAP_DUMPS
// SKIP_INLINE_CHECK_IN: lParams$default
// IGNORE_BACKEND: JS

// FILE: 1.kt

package test
//A lot of blank lines [Don't delete]
//A lot of blank lines [Don't delete]
//A lot of blank lines [Don't delete]
//A lot of blank lines [Don't delete]
//A lot of blank lines [Don't delete]
//A lot of blank lines [Don't delete]
//A lot of blank lines [Don't delete]
//A lot of blank lines [Don't delete]
//A lot of blank lines [Don't delete]
//A lot of blank lines [Don't delete]
//A lot of blank lines [Don't delete]
//A lot of blank lines [Don't delete]
//A lot of blank lines [Don't delete]
//A lot of blank lines [Don't delete]
//A lot of blank lines [Don't delete]
//A lot of blank lines [Don't delete]
//A lot of blank lines [Don't delete]
//A lot of blank lines [Don't delete]
//A lot of blank lines [Don't delete]
//A lot of blank lines [Don't delete]
//A lot of blank lines [Don't delete]
//A lot of blank lines [Don't delete]
//A lot of blank lines [Don't delete]

inline fun kValue(crossinline s: () -> String): String { konst lambda = { s() + "K" }; return lambda() }

inline fun lParams(initParams: () -> String = {
    "" + kValue { "O" }
}): String {
    konst z = "body"
    return initParams()
}

// FILE: 2.kt
import test.*

fun box(): String {
    return run {
        lParams()
    }
}
