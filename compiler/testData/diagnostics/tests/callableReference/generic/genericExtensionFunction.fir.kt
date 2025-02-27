// !CHECK_TYPE
// !DIAGNOSTICS: -UNUSED_PARAMETER, -UNUSED_VARIABLE

class Wrapper

fun <R, S> Wrapper.foo(x: R): S = TODO()
fun Wrapper.fooIntString(x: Int): String = ""
fun <T> Wrapper.fooReturnString(x: T): String = ""
fun <T> Wrapper.fooTakeInt(x: Int): T = TODO()

fun <T, R, S> bar(f: T.(R) -> S): Tripple<T, R, S> = TODO()
fun <T, R, S> baz(x: T, y: R, z: S, f: T.(R) -> S): Tripple<T, R, S> = TODO()

class Tripple<A, B, C>(konst a: A, konst b: B, konst c: C)

fun test1() {
    konst x: Wrapper.(String) -> Boolean = Wrapper::foo
    bar<Wrapper, Double, Float>(Wrapper::foo).checkType { _<Tripple<Wrapper, Double, Float>>() }
    bar(Wrapper::fooIntString).checkType { _<Tripple<Wrapper, Int, String>>() }
}

fun <T> test2() {
    bar<Wrapper, Int, String>(Wrapper::fooReturnString).checkType { _<Tripple<Wrapper, Int, String>>() }
    bar<Wrapper, T, String>(Wrapper::fooReturnString).checkType { _<Tripple<Wrapper, T, String>>() }
    <!INAPPLICABLE_CANDIDATE!>bar<!><Wrapper, T, T>(Wrapper::<!UNRESOLVED_REFERENCE!>fooReturnString<!>)
    <!INAPPLICABLE_CANDIDATE!>bar<!><Wrapper, Int, Int>(Wrapper::<!UNRESOLVED_REFERENCE!>fooReturnString<!>)

    bar<Wrapper, Int, T>(Wrapper::fooTakeInt).checkType { _<Tripple<Wrapper, Int, T>>() }
    bar<Wrapper, Int, String>(Wrapper::fooTakeInt).checkType { _<Tripple<Wrapper, Int, String>>() }
}
