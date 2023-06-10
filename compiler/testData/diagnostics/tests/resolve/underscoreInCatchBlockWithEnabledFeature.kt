// !LANGUAGE: +ForbidReferencingToUnderscoreNamedParameterOfCatchBlock
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -UNUSED_ANONYMOUS_PARAMETER -UNUSED_EXPRESSION
// FULL_JDK

fun foo() {
    try {
        TODO()
    } catch (_: Exception) {
        <!UNRESOLVED_REFERENCE!>`_`<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>stackTrace<!>
    }
    try {
        TODO()
    } catch (_: Exception) {
        konst x = {
            konst x2 = {
                konst x3 = { y: Int ->
                    konst x4 = { _: Int ->
                        <!UNRESOLVED_REFERENCE!>`_`<!>
                    }
                    <!UNRESOLVED_REFERENCE!>`_`<!>
                }
                <!UNRESOLVED_REFERENCE!>`_`<!>
                10
            }
            fun bar(x: Exception = <!UNRESOLVED_REFERENCE!>`_`<!>) {}
            class Bar(`_`: Exception = <!UNINITIALIZED_PARAMETER!>`_`<!>) {
                inner class Bar2(x: Exception = <!UNRESOLVED_REFERENCE!>`_`<!>) { }
            }
        }
    } catch (_: Exception) {
        <!UNRESOLVED_REFERENCE!>`_`<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>stackTrace<!>
        konst y1 = <!UNRESOLVED_REFERENCE!>_<!>
        konst y2 = (<!UNRESOLVED_REFERENCE!>`_`<!>)
    }
    try {
        TODO()
    } catch (_: Exception) {
        try {
            TODO()
        } catch (x: Exception) {
            <!UNRESOLVED_REFERENCE!>`_`<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>stackTrace<!>
        }
    }
    konst boo1 = { `_`: Exception ->
        try {
            TODO()
        } catch (x: Exception) {
            `_`.stackTrace
        }
    }
    konst boo2 = { _: Exception ->
        try {
            TODO()
        } catch (x: Exception) {
            <!UNRESOLVED_REFERENCE!>`_`<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>stackTrace<!>
        }
    }
}
