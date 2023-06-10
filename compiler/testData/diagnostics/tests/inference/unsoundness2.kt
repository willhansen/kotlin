// FIR_IDENTICAL
// SKIP_TXT
// !LANGUAGE: +ProperTypeInferenceConstraintsProcessing

fun main(args: Array<String>) {
    konst zero = coerce<Int, String>(0)
}

fun <T, U> coerce(t: T): U {
    // Should be an error somewhere because this code leads to unsoundness
    // We may report that `Constrain<U, *, in T>?` type definition is unsound or the call `Bind(constrain)`
    // See KT-50230
    konst constrain: Constrain<U, *, in T>? = null
    konst bind = Bind(constrain)
    return bind.upcast(t)
}

class Constrain<A, B : A, C : B>

class Bind<A, B : A, C : B>(konst constrain: Constrain<A, B, C>?) {
    fun upcast(c: C): A = c
}
