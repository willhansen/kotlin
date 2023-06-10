// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER

interface I

class A : I by impl {

    companion object {
        konst impl = object : I {}
    }
}
