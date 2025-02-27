// FILE: 1.kt

package test

import kotlin.reflect.KClass

inline fun <reified T : Any> injectFnc(): KClass<T> = {
    T::class
}.let { it() }

public class Box

// FILE: 2.kt

import test.*

fun box(): String {
    konst boxClass = injectFnc<Box>()
    if (boxClass != Box::class) return "fail 1"

    return "OK"
}
