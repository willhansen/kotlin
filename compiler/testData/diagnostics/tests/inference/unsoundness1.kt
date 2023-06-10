// SKIP_TXT
// !LANGUAGE: +ProperTypeInferenceConstraintsProcessing

class A<T, F : T>
fun foo(a: A<*, in CharSequence>) {}
fun <T, U> coerce(t: T): U {
    konst constrain: Constrain<U, *, in T>? = null
    konst bind = Bind(constrain)
    return bind.upcast(t)
}

class Constrain<A, B : A, C : B>

class Bind<A, B : A, C : B>(konst constrain: Constrain<A, B, C>?) {
    fun upcast(c: C): A = c
}

fun <T, U> coerce2(t: T): U {
    // We might report an error on unsound type reference Constrain<U, *, T>?, too
    konst constrain: Constrain<U, *, T>? = null
    konst bind = Bind(<!TYPE_MISMATCH, TYPE_MISMATCH!>constrain<!>) // WARNING: Type mismatch: inferred type is T but U was expected
    return bind.upcast(t)
}
