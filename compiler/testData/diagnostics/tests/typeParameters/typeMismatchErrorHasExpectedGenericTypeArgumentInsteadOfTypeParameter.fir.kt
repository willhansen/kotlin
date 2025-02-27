// ISSUE: KT-49035, KT-51201

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

fun <T> foo(it: @kotlin.internal.Exact T) {}

fun main() {
    foo<Any>(<!ARGUMENT_TYPE_MISMATCH("kotlin/Any; kotlin/String")!>""<!>)
}

interface I
class Foo : I
class Bar

fun <MY_TYPE_PARAM : I> myRun(action: () -> MY_TYPE_PARAM): MY_TYPE_PARAM = action()

konst a = myRun<Foo> { <!ARGUMENT_TYPE_MISMATCH("Foo; Bar")!>Bar()<!> }
