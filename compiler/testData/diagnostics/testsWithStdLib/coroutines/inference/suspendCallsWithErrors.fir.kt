// !OPT_IN: kotlin.RequiresOptIn
// !DIAGNOSTICS: -UNUSED_PARAMETER
// NI_EXPECTED_FILE

@file:OptIn(ExperimentalTypeInference::class)

import kotlin.experimental.ExperimentalTypeInference

class Controller<T> {
    suspend fun yield(t: T) {}
}

fun <S> generate(g: suspend Controller<S>.() -> Unit): S = TODO()

class A

konst test1 = generate {
    yield(<!NO_COMPANION_OBJECT!>A<!>)
}

konst test2: Int = generate {
    yield(<!ARGUMENT_TYPE_MISMATCH!>A()<!>)
}
