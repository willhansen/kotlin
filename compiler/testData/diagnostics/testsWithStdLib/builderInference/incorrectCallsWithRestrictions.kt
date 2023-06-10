// FIR_IDENTICAL
// !OPT_IN: kotlin.RequiresOptIn
// !DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_PARAMETER -UNUSED_VARIABLE

@file:OptIn(ExperimentalTypeInference::class)

import kotlin.experimental.ExperimentalTypeInference

class GenericController<T> {
    suspend fun yield(t: T) {}
    fun notYield(t: T) {}

    suspend fun yieldBarReturnType(t: T) = t
    fun barReturnType(): T = TODO()
}

fun <S> generate(g: suspend GenericController<S>.() -> Unit): List<S> = TODO()

konst test1 = generate {
    yield(3)
}

konst test2 = generate {
    yield(3)
    notYield(3)
}

konst test3 = generate {
    yield(3)
    yieldBarReturnType(3)
}

konst test4 = generate {
    yield(3)
    barReturnType()
}
