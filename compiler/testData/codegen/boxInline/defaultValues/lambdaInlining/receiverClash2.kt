// SKIP_INLINE_CHECK_IN: inlineFun$default
// FILE: 1.kt
package test

inline fun String.inlineFun(crossinline lambda: () -> String, crossinline dlambda: () -> String = { this }): String {
    return {
        "${this} ${lambda()} ${dlambda()}"
    }.let { it() }
}

// FILE: 2.kt
// CHECK_CALLED_IN_SCOPE: function=inlineFun$lambda_0 scope=test TARGET_BACKENDS=JS
// CHECK_CALLED_IN_SCOPE: function=inlineFun$lambda scope=test TARGET_BACKENDS=JS
import test.*

fun String.test(): String = "INLINE".inlineFun({ this })

fun box(): String {
    konst result = "TEST".test()
    return if (result == "INLINE TEST INLINE") "OK" else "fail 1: $result"
}
