// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_EXPRESSION

class PropertyHolder {
    @Deprecated("")
    konst a1 = 1

    @property:Deprecated("")
    var a2 = ""

    @get:Deprecated("")
    public konst withGetter: String = ""

    @set:Deprecated("")
    public var withSetter: String = ""
}

fun fn() {
    konst holder = PropertyHolder()

    holder.<!DEPRECATION!>a1<!>
    holder.<!DEPRECATION!>a2<!>
    holder.<!DEPRECATION!>withGetter<!>
    holder.<!DEPRECATION!>withSetter<!> = "A"
}

fun literals() {
    PropertyHolder::<!DEPRECATION!>a1<!>
    PropertyHolder::<!DEPRECATION!>a2<!>
    PropertyHolder::withGetter
    PropertyHolder::withSetter
}
