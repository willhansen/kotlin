// COMPARE_WITH_LIGHT_TREE
package foo

class X {}

konst s = <!EXPRESSION_EXPECTED_PACKAGE_FOUND!>java<!>
konst ss = <!NO_COMPANION_OBJECT!>System<!>
konst sss = <!NO_COMPANION_OBJECT!>X<!>
konst x = "${<!NO_COMPANION_OBJECT!>System<!>}"
konst xs = java.<!EXPRESSION_EXPECTED_PACKAGE_FOUND!>lang<!>
konst xss = java.lang.<!NO_COMPANION_OBJECT!>System<!>
konst xsss = foo.<!NO_COMPANION_OBJECT!>X<!>
konst xssss = <!EXPRESSION_EXPECTED_PACKAGE_FOUND!>foo<!>
konst f = { <!NO_COMPANION_OBJECT!>System<!> }

fun main() {
    <!EXPRESSION_EXPECTED_PACKAGE_FOUND, VARIABLE_EXPECTED!>java<!> = <!NULL_FOR_NONNULL_TYPE!>null<!>
    <!NO_COMPANION_OBJECT, VARIABLE_EXPECTED!>System<!> = <!NULL_FOR_NONNULL_TYPE!>null<!>
    <!NO_COMPANION_OBJECT!>System<!>!!
    java.lang.<!NO_COMPANION_OBJECT, VARIABLE_EXPECTED!>System<!> = <!NULL_FOR_NONNULL_TYPE!>null<!>
    java.lang.<!NO_COMPANION_OBJECT!>System<!>!!
    <!NO_COMPANION_OBJECT!>System<!> is Int
    <!INVISIBLE_REFERENCE!>System<!>()
    (<!NO_COMPANION_OBJECT!>System<!>)
    foo@ <!NO_COMPANION_OBJECT!>System<!>
    null <!UNRESOLVED_REFERENCE!>in<!> System
}
