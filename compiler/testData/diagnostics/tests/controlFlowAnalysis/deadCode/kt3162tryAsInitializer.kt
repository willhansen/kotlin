// FIR_IDENTICAL
//KT-3162 More precise try-finally error marking

fun foo(x: String) : String {
    <!UNREACHABLE_CODE!>konst <!UNUSED_VARIABLE!>a<!> =<!> try {
        x
    } finally {
        try {
        } catch (e: Exception) {
        }
        return x
    }
}