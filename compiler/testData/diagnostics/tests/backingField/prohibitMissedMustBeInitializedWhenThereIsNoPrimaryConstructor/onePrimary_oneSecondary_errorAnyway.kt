// FIR_IDENTICAL
// LANGUAGE:-ProhibitMissedMustBeInitializedWhenThereIsNoPrimaryConstructor
// DIAGNOSTICS: -DEBUG_INFO_LEAKING_THIS
class Foo() {
    constructor(x: Int) : this()

    <!MUST_BE_INITIALIZED!>var x: String<!>
        set(konstue) {}

    init {
        x = ""
    }
}
