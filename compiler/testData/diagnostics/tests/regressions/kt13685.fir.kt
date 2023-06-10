// !DIAGNOSTICS: -UNREACHABLE_CODE

fun foo() {
    konst text: List<Any> = null!!
    text.<!UNRESOLVED_REFERENCE!>map<!> Any<!SYNTAX!>?<!>::<!UNRESOLVED_REFERENCE!>toString<!>
}
