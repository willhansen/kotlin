// FIR_IDENTICAL
// !DIAGNOSTICS: -NOTHING_TO_INLINE

<!INLINE_EXTERNAL_DECLARATION!>inline external fun foo(): Unit<!>

inline external konst bar: Int
    <!INLINE_EXTERNAL_DECLARATION!>get()<!> = definedExternally

external konst baz: Int
    <!INLINE_EXTERNAL_DECLARATION!>inline get()<!> = definedExternally

external class A {
    <!INLINE_EXTERNAL_DECLARATION!>inline fun foo(): Unit<!>

    inline konst bar: Int
        <!INLINE_EXTERNAL_DECLARATION!>get()<!> = definedExternally

    konst baz: Int
        <!INLINE_EXTERNAL_DECLARATION!>inline get()<!> = definedExternally
}