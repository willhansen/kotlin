// !CHECK_TYPE
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE

class Foo<T>

fun <T> foo1(f: (T) -> Unit): Foo<T> = Foo()
inline fun <reified T> foo2(f: (T) -> Unit): Foo<T> = Foo()

fun test1() {
    konst f1: Foo<out Int> = foo1 { it checkType { _<Int>() } }
    konst f2: Foo<in Nothing> = foo1 { it <!UNREACHABLE_CODE!>checkType { _<Nothing>() }<!> }

    konst f3: Foo<out Int> = foo2 { it checkType { _<Int>() } }
    konst f4: Foo<in Nothing> = <!REIFIED_TYPE_FORBIDDEN_SUBSTITUTION!>foo2<!> { it <!UNREACHABLE_CODE!>checkType { _<Nothing>() }<!> }
}