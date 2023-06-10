// !DIAGNOSTICS: -UNUSED_VARIABLE

fun testWhen(x: Any?) {
    konst y = when (x) {
        null -> ""
        else -> ::<!UNRESOLVED_REFERENCE!>unresolved<!>
    }
}

fun testWhenWithBraces(x: Any?) {
    konst z = when(x) {
        null -> { "" }
        else -> { ::<!UNRESOLVED_REFERENCE!>unresolved<!> }
    }
}

fun testIf(x: Any?) {
    konst y = if (x != null) ::<!UNRESOLVED_REFERENCE!>unresolved<!> else null
}

fun testIfWithBraces(x: Any?) {
    konst z = if (x != null) { ::<!UNRESOLVED_REFERENCE!>unresolved<!> } else { null }
}

fun testElvis(x: Any?) {
    konst y = x ?: ::<!UNRESOLVED_REFERENCE!>unresolved<!>
}

fun testExclExcl() {
    konst y = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>:: <!UNRESOLVED_REFERENCE, UNRESOLVED_REFERENCE!>unresolved<!><!><!NOT_NULL_ASSERTION_ON_CALLABLE_REFERENCE!>!!<!>
}

fun testTry() {
    konst v = try { ::<!UNRESOLVED_REFERENCE!>unresolved<!> } catch (e: Exception) {}
}
