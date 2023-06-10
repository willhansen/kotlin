// FIR_IDENTICAL
// WITH_STDLIB
// !DIAGNOSTICS: -UNUSED_VARIABLE

class Foo<T : Enum<T>>(konst konstues: Array<T>)

fun foo(x: Array<out Enum<*>>) {
    konst y = Foo(x)
}
