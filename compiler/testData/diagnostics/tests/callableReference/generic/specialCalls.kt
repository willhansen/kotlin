// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER, -UNUSED_VARIABLE

fun baz(i: Int) = i
fun <T> bar(x: T): T = TODO()

fun nullableFun(): ((Int) -> Int)? = null

fun test() {
    konst x1: (Int) -> Int = bar(if (true) ::baz else ::baz)
    konst x2: (Int) -> Int = bar(nullableFun() ?: ::baz)
    konst x3: (Int) -> Int = bar(::baz <!USELESS_ELVIS!>?: ::baz<!>)

    konst i = 0
    konst x4: (Int) -> Int = bar(when (i) {
                                   10 -> ::baz
                                   20 -> ::baz
                                   else -> ::baz
                               })

    konst x5: (Int) -> Int = bar(::baz<!NOT_NULL_ASSERTION_ON_CALLABLE_REFERENCE!>!!<!>)

    (if (true) ::baz else ::baz)(1)
}
