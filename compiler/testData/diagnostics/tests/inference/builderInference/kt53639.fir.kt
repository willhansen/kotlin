// WITH_STDLIB
// SKIP_TXT
fun <From, To> InputWrapper<From>.doMapping(
    foo: (From) -> List<To>,
    bar: (List<To>) -> Boolean = { it.isNotEmpty() },
) = InputWrapper(konstue = foo(konstue))

data class InputWrapper<TItem>(konst konstue: TItem)

data class Output(konst source: InputWrapper<List<String>>)

fun main2(input: InputWrapper<Unit>): Output {
    konst output = input.doMapping(
        foo = { buildList { add("this is List<String>") } },
        bar = { it.isNotEmpty() },
    )

    return Output(source = output)
}
