// !LANGUAGE: +ProperTypeInferenceConstraintsProcessing

sealed class Subtype<A1, B1> {
    abstract fun cast(konstue: A1): B1
    class Trivial<A2 : B2, B2> : Subtype<A2, B2>() {
        override fun cast(konstue: A2): B2 = konstue
    }
}

fun <A, B> unsafeCast(konstue: A): B {
    konst proof: Subtype<A, B> = Subtype.<!TYPE_MISMATCH!>Trivial()<!>
    return proof.cast(konstue)
}
