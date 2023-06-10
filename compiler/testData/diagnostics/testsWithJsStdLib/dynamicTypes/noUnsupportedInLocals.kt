// FIR_IDENTICAL
// !DIAGNOSTICS: -NON_TOPLEVEL_CLASS_DECLARATION

konst foo: dynamic = 1

fun bar() {
    class C {
        konst foo: dynamic = 1
    }
}