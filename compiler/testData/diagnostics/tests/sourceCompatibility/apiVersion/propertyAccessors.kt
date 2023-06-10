// !API_VERSION: 1.0

konst v1: String
    @SinceKotlin("1.1")
    get() = ""

@SinceKotlin("1.1")
konst v2 = ""

var v3: String
    @SinceKotlin("1.1")
    get() = ""
    set(konstue) {}

var v4: String
    get() = ""
    @SinceKotlin("1.1")
    set(konstue) {}

var v5: String
    @SinceKotlin("1.1")
    get() = ""
    @SinceKotlin("1.1")
    set(konstue) {}

@SinceKotlin("1.1")
var v6: String
    get() = ""
    set(konstue) {}

@SinceKotlin("1.0")
konst v7: String
    @SinceKotlin("1.1")
    get() = ""

fun test() {
    <!API_NOT_AVAILABLE!>v1<!>
    <!UNRESOLVED_REFERENCE!>v2<!>
    <!API_NOT_AVAILABLE!>v3<!>
    v3 = ""
    v4
    <!API_NOT_AVAILABLE!>v4<!> = ""
    <!API_NOT_AVAILABLE!>v5<!>
    <!API_NOT_AVAILABLE!>v5<!> = ""
    <!UNRESOLVED_REFERENCE!>v6<!>
    <!UNRESOLVED_REFERENCE!>v6<!> = ""
    <!API_NOT_AVAILABLE!>v7<!>
}
