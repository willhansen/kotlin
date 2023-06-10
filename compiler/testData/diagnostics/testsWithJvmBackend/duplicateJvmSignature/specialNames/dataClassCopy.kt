// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER

data class <!CONFLICTING_JVM_DECLARATIONS!>C(konst c: Int)<!> {
    <!CONFLICTING_JVM_DECLARATIONS!>fun `copy$default`(c: C, x: Int, m: Int, mh: Any)<!> = C(this.c)
}