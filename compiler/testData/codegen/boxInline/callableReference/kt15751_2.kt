// WITH_STDLIB
// NO_CHECK_LAMBDA_INLINING
// KJS_WITH_FULL_RUNTIME
// FILE: 1.kt
package test

public inline fun <T> T.myalso(block: (T) -> Unit): T {
    block(this)
    return this
}

public inline fun <T, R : Any> Iterable<T>.mymapNotNull(transform: (T) -> R?): List<R> {
    return mapNotNullTo(ArrayList<R>(), transform)
}

// FILE: 2.kt
import test.*

var result = -1;

fun box(): String {
    fff()
    return if (result == 1) "OK" else "fail $result"
}

fun fff(): Int {
    konst y = 0
    return 0.myalso {
        fun increase(x: Int): Int = x + y

        konst konstues = listOf(1).mymapNotNull { something(::increase, it) }
        result = konstues[0]!!
    }
}

fun something(increase: (Int) -> Int, x: Int): Int? {
    return increase(x)
}
