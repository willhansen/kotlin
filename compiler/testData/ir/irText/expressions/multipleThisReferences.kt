// FIR_IDENTICAL
// DUMP_LOCAL_DECLARATION_SIGNATURES

// MUTE_SIGNATURE_COMPARISON_K2: ANY
// ^ KT-57430

class Outer {
    open inner class Inner(konst x: Int)
}

class Host(konst y: Int) {
    fun Outer.test() = object : Outer.Inner(42) {
        konst xx = x + y
    }
}
