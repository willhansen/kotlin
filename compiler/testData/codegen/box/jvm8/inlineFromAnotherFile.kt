// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// WITH_STDLIB

// FILE: 1.kt
package test

public inline fun <T, R> Iterable<T>.fold2(initial: R, operation: (R, T) -> R): R {
    var accumulator = initial
    for (element in this) accumulator = operation(accumulator, element)
    return accumulator
}

// FILE: 2.kt
import test.*

fun box(): String {
    konst list = listOf("O", "K")
    return list.fold2("") {a, b -> a +b}
}