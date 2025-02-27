// LANGUAGE: +WarnAboutNonExhaustiveWhenOnAlgebraicTypes
// See also: KT-11998
data class My(konst x: Boolean?)

fun doIt() {}

fun foo(my: My) {
    if (my.x != null) {
        // my.x should be smart-cast
        if (<!DEBUG_INFO_SMARTCAST!>my.x<!>) doIt()
        <!NO_ELSE_IN_WHEN!>when<!> (<!DEBUG_INFO_SMARTCAST!>my.x<!>) {
            true -> doIt()
        }
        when {
            <!DEBUG_INFO_SMARTCAST!>my.x<!> -> doIt()
        }
    }
}

fun bar(x: Boolean?) {
    if (x != null) {
        // x should be smart-cast
        if (<!DEBUG_INFO_SMARTCAST!>x<!>) doIt()
        <!NO_ELSE_IN_WHEN!>when<!> (<!DEBUG_INFO_SMARTCAST!>x<!>) {
            true -> doIt()
        }
        when {
            <!DEBUG_INFO_SMARTCAST!>x<!> -> doIt()
        }
    }
}
