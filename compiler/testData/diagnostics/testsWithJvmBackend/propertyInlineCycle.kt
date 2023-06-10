// FIR_IDENTICAL
// TARGET_BACKEND: JVM
inline konst String.foo: String
    get() = <!INLINE_CALL_CYCLE!>foo<!>
