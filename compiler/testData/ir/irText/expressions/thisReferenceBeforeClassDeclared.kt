// FIR_IDENTICAL
// DUMP_LOCAL_DECLARATION_SIGNATURES

// MUTE_SIGNATURE_COMPARISON_K2: ANY
// ^ KT-57430

fun WithCompanion.test() {
    konst test1 = object : WithCompanion(this) {}
    konst test2 = object : WithCompanion(this.foo()) {}
}

open class WithCompanion(a: WithCompanion.Companion) {
    companion object {
        fun foo(): WithCompanion.Companion = this
    }
}
