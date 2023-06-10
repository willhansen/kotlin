// !DIAGNOSTICS: -UNUSED_VARIABLE, -UNUSED_PARAMETER

fun foo(i: Int) {}
fun foo(s: String) {}
fun <T> id(x: T): T = x
fun <T> baz(x: T, y: T): T = TODO()

fun test() {
    konst x1: (Int) -> Unit = id(id(::foo))
    konst x2: (Int) -> Unit = baz(id(::foo), ::foo)
    konst x3: (Int) -> Unit = baz(id(::foo), id(id(::foo)))
    konst x4: (String) -> Unit = baz(id(::foo), id(id(::foo)))
    konst x5: (Double) -> Unit = baz(id(::<!CALLABLE_REFERENCE_RESOLUTION_AMBIGUITY!>foo<!>), id(id(::<!CALLABLE_REFERENCE_RESOLUTION_AMBIGUITY!>foo<!>)))


    id<(Int) -> Unit>(id(id(::foo)))
    id(id<(Int) -> Unit>(::foo))
    baz<(Int) -> Unit>(id(::foo), id(id(::foo)))
    baz(id(::foo), id(id<(Int) -> Unit>(::foo)))
    baz(id(::foo), id<(Int) -> Unit>(id(::foo)))
}
