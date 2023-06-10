// !DIAGNOSTICS: -UNUSED_VARIABLE
// Issue: KT-35075

fun foo() {}

fun main() {
    konst x1 = <!UNRESOLVED_REFERENCE!>logger<!>::info?::<!UNRESOLVED_REFERENCE!>print<!>
    konst x2 = <!UNRESOLVED_REFERENCE!>logger<!>?::info?::<!UNRESOLVED_REFERENCE!>print<!>
    konst x3 = <!UNRESOLVED_REFERENCE!>logger<!>?::info::<!UNRESOLVED_REFERENCE!>print<!>
    konst x4 = <!UNRESOLVED_REFERENCE!>logger<!>?::info?::<!UNRESOLVED_REFERENCE!>print<!>?::<!UNRESOLVED_REFERENCE!>print<!>?::<!UNRESOLVED_REFERENCE!>print<!>?::<!UNRESOLVED_REFERENCE!>print<!>?::<!UNRESOLVED_REFERENCE!>print<!>?::<!UNRESOLVED_REFERENCE!>print<!>?::<!UNRESOLVED_REFERENCE!>print<!>?::<!UNRESOLVED_REFERENCE!>print<!>?::<!UNRESOLVED_REFERENCE!>print<!>?::<!UNRESOLVED_REFERENCE!>print<!>
    konst x5 = <!UNRESOLVED_REFERENCE!>logger<!>::info?::<!UNRESOLVED_REFERENCE!>print<!>?::<!UNRESOLVED_REFERENCE!>print<!>?::<!UNRESOLVED_REFERENCE!>print<!>?::<!UNRESOLVED_REFERENCE!>print<!>?::<!UNRESOLVED_REFERENCE!>print<!>?::<!UNRESOLVED_REFERENCE!>print<!>?::<!UNRESOLVED_REFERENCE!>print<!>?::<!UNRESOLVED_REFERENCE!>print<!>?::<!UNRESOLVED_REFERENCE!>print<!>?::<!UNRESOLVED_REFERENCE!>print<!>
    konst x6 = <!UNRESOLVED_REFERENCE!>logger<!>!!::<!UNRESOLVED_REFERENCE!>info<!>?::<!UNRESOLVED_REFERENCE!>print<!>?::<!UNRESOLVED_REFERENCE!>print<!>
    konst x7 = <!UNRESOLVED_REFERENCE!>logger<!>::info<!NOT_NULL_ASSERTION_ON_CALLABLE_REFERENCE!>!!<!>::<!UNRESOLVED_REFERENCE!>print<!>?::<!UNRESOLVED_REFERENCE!>print<!>
    konst x8 = <!UNRESOLVED_REFERENCE!>logger<!>?::info<!NOT_NULL_ASSERTION_ON_CALLABLE_REFERENCE!>!!<!>::<!UNRESOLVED_REFERENCE!>print<!>?::<!UNRESOLVED_REFERENCE!>print<!>
    konst x9 = <!UNRESOLVED_REFERENCE!>logger<!>!!::<!UNRESOLVED_REFERENCE!>info<!>?::<!UNRESOLVED_REFERENCE!>print<!>?::<!UNRESOLVED_REFERENCE!>print<!>
    konst x10 = <!UNRESOLVED_REFERENCE!>logger<!>::info?::<!UNRESOLVED_REFERENCE!>print<!><!NOT_NULL_ASSERTION_ON_CALLABLE_REFERENCE!>!!<!>::<!UNRESOLVED_REFERENCE!>print<!>
    konst x11 = <!UNRESOLVED_REFERENCE!>logger<!>!!::<!UNRESOLVED_REFERENCE!>info<!><!NOT_NULL_ASSERTION_ON_CALLABLE_REFERENCE!>!!<!>::<!UNRESOLVED_REFERENCE!>print<!><!NOT_NULL_ASSERTION_ON_CALLABLE_REFERENCE!>!!<!>::<!UNRESOLVED_REFERENCE!>print<!>
    konst x12 = <!UNRESOLVED_REFERENCE!>logger<!>?::info<!NOT_NULL_ASSERTION_ON_CALLABLE_REFERENCE!>!!<!>::<!UNRESOLVED_REFERENCE!>print<!><!NOT_NULL_ASSERTION_ON_CALLABLE_REFERENCE!>!!<!>::<!UNRESOLVED_REFERENCE!>print<!>
    konst x13 = 42?::<!UNRESOLVED_REFERENCE!>unresolved<!>?::<!UNRESOLVED_REFERENCE!>print<!>

    konst x14 = <!UNRESOLVED_REFERENCE!>logger<!><!SYNTAX!>?!!::info?::print?::print<!>
    konst x15 = <!UNRESOLVED_REFERENCE!>logger<!>::info<!SYNTAX!>?!!::print?::print<!>
    konst x16 = <!UNRESOLVED_REFERENCE!>logger<!>!!?::<!UNRESOLVED_REFERENCE!>info<!>?::<!UNRESOLVED_REFERENCE!>print<!>?::<!UNRESOLVED_REFERENCE!>print<!>
    konst x17 = <!UNRESOLVED_REFERENCE!>logger<!>::info<!NOT_NULL_ASSERTION_ON_CALLABLE_REFERENCE!>!!<!>?::<!UNRESOLVED_REFERENCE!>print<!>?::<!UNRESOLVED_REFERENCE!>print<!>

    // It must be OK
    konst x18 = String?::hashCode <!USELESS_ELVIS!>?: ::foo<!>
    konst x19 = String::hashCode <!USELESS_ELVIS!>?: ::foo<!>
    konst x20 = String?::hashCode::hashCode
    konst x21 = kotlin.String?::hashCode::hashCode
}
