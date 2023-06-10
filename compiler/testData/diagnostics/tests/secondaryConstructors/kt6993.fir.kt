// !DIAGNOSTICS: -UNUSED_PARAMETER
class X<T>(konst t: T) {
    constructor(t: T, i: Int) : this(<!ARGUMENT_TYPE_MISMATCH!>i<!>)
}
