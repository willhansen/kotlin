// FIR_IDENTICAL
// !OPT_IN: kotlin.RequiresOptIn
// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER -UNUSED_EXPRESSION

@file:OptIn(ExperimentalTypeInference::class)

package a.b

import kotlin.experimental.ExperimentalTypeInference

class BatchInfo1(konst batchSize: Int)
class BatchInfo2<T>(konst data: T)

object Obj

fun test1() {
    konst a: Sequence<String> = sequence {
        konst x = BatchInfo1::class
        konst y = a.b.BatchInfo1::class
        konst z = Obj::class

        konst x1 = BatchInfo1::batchSize
        konst y1 = a.b.BatchInfo1::class
    }
}

interface Scope<T> {
    fun yield(t: T) {}
}

fun <S> generate(g: Scope<S>.() -> Unit): S = TODO()

konst test2 = generate {
    { yield("foo") }::class
}

konst test3 = generate {
    ({ yield("foo") })::class
}
