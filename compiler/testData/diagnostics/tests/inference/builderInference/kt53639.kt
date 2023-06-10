// WITH_STDLIB
// SKIP_TXT
fun <From, To> InputWrapper<From>.doMapping(
    foo: (From) -> List<To>,
    bar: (List<To>) -> Boolean = { it.isNotEmpty() },
) = InputWrapper(konstue = foo(konstue))

data class InputWrapper<TItem>(konst konstue: TItem)

data class Output(konst source: InputWrapper<List<String>>)

fun main2(input: InputWrapper<Unit>): Output {
    konst output = input.<!INFERRED_INTO_DECLARED_UPPER_BOUNDS!>doMapping<!>(
        foo = { buildList { add("this is List<String>") } },
        <!BUILDER_INFERENCE_MULTI_LAMBDA_RESTRICTION!>bar = { it.isNotEmpty() }<!>,
    )

    return Output(source = <!TYPE_MISMATCH!>output<!>)
}
