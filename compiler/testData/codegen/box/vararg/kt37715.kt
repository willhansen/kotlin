// WITH_STDLIB

import kotlin.collections.toList

fun <T: Number> foo(vararg konstues: T) = konstues.toList()

fun box(): String {
    konst a = foo(1, 4.5)
    return "OK"
}
