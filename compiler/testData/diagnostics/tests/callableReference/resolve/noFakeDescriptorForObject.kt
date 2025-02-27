// FIR_IDENTICAL
// !CHECK_TYPE
// KT-12322 Overload resolution ambiguity with constructor references when class has a companion object

class Foo {
    companion object
}

fun test() {
    konst a = ::Foo
    checkSubtype<() -> Foo>(a)
}
