// MUTE_SIGNATURE_COMPARISON_K2: ANY
// ^ KT-57754

interface I {
    konst <T> T.id: T
        get() = this
}

class A(i: I) : I by i
