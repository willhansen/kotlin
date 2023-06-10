// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE

fun eat(konstue: Any) {}

fun test(param: String) {
    konst a = ::<!UNSUPPORTED!>param<!>

    konst local = "local"
    konst b = ::<!UNSUPPORTED!>local<!>

    konst lambda = { -> }
    konst g = ::<!UNSUPPORTED!>lambda<!>

    <!INAPPLICABLE_CANDIDATE!>eat<!>(::<!UNSUPPORTED!>param<!>)
}
