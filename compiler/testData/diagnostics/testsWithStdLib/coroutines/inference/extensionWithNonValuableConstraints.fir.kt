// !DIAGNOSTICS: -UNUSED_PARAMETER
// !OPT_IN: kotlin.RequiresOptIn
// NI_EXPECTED_FILE

@file:OptIn(ExperimentalTypeInference::class)

import kotlin.experimental.ExperimentalTypeInference

interface Base

interface Controller<T> : Base {
    suspend fun yield(t: T) {}
}

fun <S> generate(g: suspend Controller<S>.() -> Unit): S = TODO()

fun Base.baseExtension() {}
fun Controller<out Any?>.outNullableAnyExtension() {}
fun Controller<out Any>.outAnyExtension() {}
fun Controller<Any?>.invNullableAnyExtension() {}
fun <S> Controller<S>.genericExtension() {}

fun Controller<String>.safeExtension() {}

konst test1 = generate {
    yield("foo")
    baseExtension()
}

konst test2 = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>generate<!> {
    baseExtension()
}

konst test3 = generate {
    yield(42)
    outNullableAnyExtension()
}

konst test4 = generate {
    outNullableAnyExtension()
}

konst test5 = generate {
    yield(42)
    outAnyExtension()
}

konst test6 = generate {
    yield("bar")
    invNullableAnyExtension()
}

konst test7 = generate {
    yield("baz")
    genericExtension<Int>()
}

konst test8 = generate {
    safeExtension()
}
