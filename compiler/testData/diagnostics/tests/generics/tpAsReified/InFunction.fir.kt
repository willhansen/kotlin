// !DIAGNOSTICS: -UNUSED_PARAMETER, -UNUSED_EXPRESSION

inline fun <reified T> foo() {
    <!CALLABLE_REFERENCE_LHS_NOT_A_CLASS!>T::toString<!>
}

inline fun <reified T> f(): T = throw UnsupportedOperationException()

fun <T> id(p: T): T = p

fun <A> main() {
    <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>f<!>()

    konst a: A = <!TYPE_PARAMETER_AS_REIFIED!>f<!>()
    f<<!TYPE_PARAMETER_AS_REIFIED!>A<!>>()

    konst b: Int = f()
    f<Int>()

    konst с: A = id(<!TYPE_PARAMETER_AS_REIFIED!>f<!>())
}
