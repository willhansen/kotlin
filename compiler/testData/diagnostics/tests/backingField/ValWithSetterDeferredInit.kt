// FIR_IDENTICAL
// LANGUAGE: -ProhibitOpenValDeferredInitialization
// DIAGNOSTICS: -DEBUG_INFO_LEAKING_THIS
open class A {
    <!MUST_BE_INITIALIZED_WARNING!>open konst c: Int<!>
        <!VAL_WITH_SETTER!>set(konstue) {}<!>

    init {
        c = 1
    }
}
