konst v1: String
    @Deprecated("", level = DeprecationLevel.HIDDEN)
    get() = ""

@Deprecated("", level = DeprecationLevel.HIDDEN)
konst v2 = ""

var v3: String
    @Deprecated("", level = DeprecationLevel.HIDDEN)
    get() = ""
    set(konstue) {}

var v4: String
    get() = ""
    @Deprecated("", level = DeprecationLevel.HIDDEN)
    set(konstue) {}

var v5: String
    @Deprecated("", level = DeprecationLevel.HIDDEN)
    get() = ""
    @Deprecated("", level = DeprecationLevel.HIDDEN)
    set(konstue) {}

@Deprecated("", level = DeprecationLevel.HIDDEN)
var v6: String
    get() = ""
    set(konstue) {}

fun test() {
    <!DEPRECATION_ERROR!>v1<!>
    <!UNRESOLVED_REFERENCE!>v2<!>
    <!DEPRECATION_ERROR!>v3<!>
    v3 = ""
    v4
    <!DEPRECATION_ERROR!>v4<!> = ""
    <!DEPRECATION_ERROR!>v5<!>
    <!DEPRECATION_ERROR!>v5<!> = ""
    <!UNRESOLVED_REFERENCE!>v6<!>
    <!UNRESOLVED_REFERENCE!>v6<!> = ""
}
