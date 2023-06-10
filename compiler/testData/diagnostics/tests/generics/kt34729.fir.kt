// !DIAGNOSTICS: -UNUSED_PARAMETER

interface ILength {
    konst length: Int
}

class Impl(override konst length: Int) : ILength

fun <T> foo(a: (Int) -> T) = 0
fun <T : ILength> bar(a: (Int) -> T) {
    a(42).length
}

fun test() {
    foo<String> <!ARGUMENT_TYPE_MISMATCH!>{ }<!>
    bar<Impl> <!ARGUMENT_TYPE_MISMATCH!>{ }<!>
}
