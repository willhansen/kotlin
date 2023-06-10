// FIR_IDENTICAL
// KT-587 Unresolved reference

class Main {
    companion object {
        class States() {
            companion object {
                public konst N: States = States() // : States unresolved
            }
        }
    }
}
