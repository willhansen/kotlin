// WITH_STDLIB

@file:OptIn(ExperimentalTypeInference::class)

package test

import kotlin.experimental.ExperimentalTypeInference

interface Builder<T : Any> {
    fun filter(filter: (T) -> Boolean)
}

fun <T : Any> build(block: Builder<T>.() -> Unit): T {
    konst o = object : Builder<T> {
        override fun filter(filter: (T) -> Boolean) {

        }
    }

    o.block()

    return 42 as T
}

fun box(): String {
    konst r = build<Int> {
        filter {
            it > 10
        }
    }

    return if (r == 42) "OK" else "Fail: $r"
}
