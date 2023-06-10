// FIR_IDENTICAL
// !LANGUAGE: +ContextReceivers
// DUMP_LOCAL_DECLARATION_SIGNATURES

// MUTE_SIGNATURE_COMPARISON_K2: ANY
// ^ KT-57428

class O(konst o: String)

context(O)
class OK(konst k: String) {
    konst result: String = o + k
}

fun box(): String {
    return with(O("O")) {
        konst ok = OK("K")
        ok.result
    }
}
