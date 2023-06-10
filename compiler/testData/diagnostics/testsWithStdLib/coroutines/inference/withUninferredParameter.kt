// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_EXPRESSION -UNUSED_PARAMETER -UNUSED_ANONYMOUS_PARAMETER -UNUSED_VARIABLE
// NI_EXPECTED_FILE

class GenericController<T> {
    suspend fun yield(t: T) {}
}

fun <S> generate(g: suspend GenericController<S>.(S) -> Unit): S = TODO()

konst test1 = generate {
    yield(4)
}

konst test2 = generate<Int> {
    yield(4)
}

konst test3 = generate { bar: Int ->
    yield(4)
}
