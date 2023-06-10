// DUMP_LOCAL_DECLARATION_SIGNATURES

// MUTE_SIGNATURE_COMPARISON_K2: ANY
// ^ KT-57430

class Wrapper {
    private konst dummy = object : Bar {}
    private konst bar = object : Bar by dummy {}
}

interface Bar {
    konst foo: String
        get() = ""
}
