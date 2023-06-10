// !DIAGNOSTICS: -UNUSED_VARIABLE
package test

object Wrong
object Right

class a {
    class b<T> {
        class c {
            fun foo() = Wrong
        }
    }
}

fun Int.foo() = Right

class Test {
    konst a: List<Int> = null!!

    konst <T> List<T>.b: Int get() = 42

    konst Int.c: Int get() = 42

    konst test1: () -> Right = a.b.c::foo
    konst test1a: () -> Right = a.<!EXPLICIT_TYPE_ARGUMENTS_IN_PROPERTY_ACCESS!>b<!><Int>.c::foo

    konst test2: () -> Right = a.b.c?::foo
    konst test2a: () -> Right = a.<!EXPLICIT_TYPE_ARGUMENTS_IN_PROPERTY_ACCESS!>b<!><Int>.c?::foo
}
