// COMPARE_WITH_LIGHT_TREE
package bar

<!MUST_BE_INITIALIZED!><!WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET!>@<!INAPPLICABLE_FILE_TARGET!>file<!>:foo<!>
konst prop<!>

@<!INAPPLICABLE_FILE_TARGET!>file<!>:[<!WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET!>bar<!> <!WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET!>baz<!>]
fun func() {}

@<!INAPPLICABLE_FILE_TARGET!>file<!>:[<!WRONG_ANNOTATION_TARGET_WITH_USE_SITE_TARGET!>baz<!>]
class C

<!SYNTAX!>@file:<!>
interface T

@file:[<!SYNTAX!><!>]
interface T

annotation class foo
annotation class bar
annotation class baz
