// FIR_IDENTICAL
// DUMP_LOCAL_DECLARATION_SIGNATURES

// MUTE_SIGNATURE_COMPARISON_K2: ANY
// ^ KT-57430

fun box(): String {
    return object {
        konst a = A("OK")
        inner class A(konst ok: String)
    }.a.ok
}
