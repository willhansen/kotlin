// !DIAGNOSTICS: -UNUSED_PARAMETER
// !LANGUAGE: +ProhibitRepeatedUseSiteTargetAnnotations

// Ann is not repeatable
annotation class Ann(konst x: Int)

<!REPEATED_ANNOTATION!>@get:Ann(10)<!>
konst a: String
    @Ann(20) get() = "foo"

<!REPEATED_ANNOTATION!>@set:Ann(10)<!>
var b: String = ""
    @Ann(20) set(konstue) { field = konstue }

<!REPEATED_ANNOTATION!>@setparam:Ann(10)<!>
var c = " "
    set(@Ann(20) x) {}

<!REPEATED_ANNOTATION!>@get:Ann(10)<!>
<!REPEATED_ANNOTATION!>@get:Ann(20)<!>
konst d: String
    @Ann(30) <!REPEATED_ANNOTATION!>@Ann(40)<!> get() = "foo"