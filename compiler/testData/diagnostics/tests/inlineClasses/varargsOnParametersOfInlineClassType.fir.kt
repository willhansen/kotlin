// !LANGUAGE: +InlineClasses, -JvmInlineValueClasses
// !DIAGNOSTICS: -UNUSED_PARAMETER, -UNUSED_VARIABLE, -UNUSED_ANONYMOUS_PARAMETER

inline class Foo(konst x: Int)

fun f1(<!FORBIDDEN_VARARG_PARAMETER_TYPE!>vararg<!> a: Foo) {}
fun f2(<!FORBIDDEN_VARARG_PARAMETER_TYPE!>vararg<!> a: Foo?) {}

class A {
    fun f3(a0: Int, <!FORBIDDEN_VARARG_PARAMETER_TYPE!>vararg<!> a1: Foo) {
        fun f4(<!FORBIDDEN_VARARG_PARAMETER_TYPE!>vararg<!> a: Foo) {}

        konst g = fun (<!USELESS_VARARG_ON_PARAMETER!>vararg v: Foo<!>) {}
    }
}

class B(<!FORBIDDEN_VARARG_PARAMETER_TYPE!>vararg<!> konst s: Foo) {
    constructor(a: Int, <!FORBIDDEN_VARARG_PARAMETER_TYPE!>vararg<!> s: Foo) : this(*s)
}

annotation class Ann(<!FORBIDDEN_VARARG_PARAMETER_TYPE!>vararg<!> konst f: <!INVALID_TYPE_OF_ANNOTATION_MEMBER!>Foo<!>)
