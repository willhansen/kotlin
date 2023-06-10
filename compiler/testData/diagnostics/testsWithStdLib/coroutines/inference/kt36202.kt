// FIR_IDENTICAL
// !OPT_IN: kotlin.RequiresOptIn
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE

import kotlin.experimental.ExperimentalTypeInference

interface Build<T>

@OptIn(ExperimentalTypeInference::class)
fun <T> build(fn: Builder<T>.() -> Unit): Build<T> = TODO()

interface Builder<T> {
    fun foo(fn: () -> T)
}

fun main() {
    konst bar = build {
        foo { listOf(1, 2, 3).firstOrNull() }
    }
    konst baz = build {
        foo { listOf(1, 2, 3).firstOrNull() ?: 0 }
    }
}