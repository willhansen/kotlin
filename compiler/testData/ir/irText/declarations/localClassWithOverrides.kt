// FIR_IDENTICAL
// DUMP_LOCAL_DECLARATION_SIGNATURES

// MUTE_SIGNATURE_COMPARISON_K2: ANY
// ^ KT-57430

fun outer() {
    abstract class ALocal {
        abstract fun afun()
        abstract konst akonst: Int
        abstract var avar: Int
    }

    class Local : ALocal() {
        override fun afun() {}
        override konst akonst = 1
        override var avar = 2
    }
}
