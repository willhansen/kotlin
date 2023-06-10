// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER
// !OPT_IN: kotlin.RequiresOptIn
// NI_EXPECTED_FILE

@file:OptIn(ExperimentalTypeInference::class)

import kotlin.experimental.ExperimentalTypeInference

interface Base<K>

interface Controller<T> : Base<T> {
    suspend fun yield(t: T) {}
}

interface SpecificController<T> : Base<String> {
    suspend fun yield(t: T) {}
}

fun <S> generate(g: suspend Controller<S>.() -> Unit): S = TODO()
fun <S> generateSpecific(g: suspend SpecificController<S>.() -> Unit): S = TODO()

fun Base<*>.starBase() {}
fun Base<String>.stringBase() {}

konst test1 = generate {
    starBase()
    yield("foo")
}

konst test2 = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>generate<!> {
    starBase()
}

konst test3 = generate {
    yield("bar")
    stringBase()
}

konst test4 = generateSpecific {
    yield(42)
    starBase()
}

konst test5 = generateSpecific {
    yield(42)
    stringBase()
}

konst test6 = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>generateSpecific<!> {
    stringBase()
}
