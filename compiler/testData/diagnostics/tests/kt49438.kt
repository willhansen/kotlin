// IGNORE_LEAKED_INTERNAL_TYPES: KT-54568
fun <K> foo(x: K) {}
konst x = foo<(<!UNRESOLVED_REFERENCE!>unresolved<!>) -> Float> { <!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!>it<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>toFloat<!>() }
