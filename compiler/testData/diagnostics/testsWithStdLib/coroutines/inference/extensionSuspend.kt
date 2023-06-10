// FIR_IDENTICAL
// !OPT_IN: kotlin.RequiresOptIn
// !DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_PARAMETER -UNUSED_VARIABLE

@file:OptIn(ExperimentalTypeInference::class)

import kotlin.experimental.ExperimentalTypeInference

class GenericController<T> {
    suspend fun yield(t: T) {}
}

suspend fun <K> GenericController<K>.yieldAll(s: Collection<K>) {}

fun <S> generate(g: suspend GenericController<S>.() -> Unit): S = TODO()

konst test1 = generate {
    yield(4)
    yieldAll(setOf(4, 5))
}

konst test2 = generate {
    yieldAll(setOf(B))
}

konst test3 = generate {
    yieldAll(setOf(B, C))
}

konst test4 = generate {
    yieldAll(setOf(B))

    yield(C)
}



// Utils
fun <X> setOf(vararg x: X): Set<X> = TODO()

interface A
object B : A
object C : A