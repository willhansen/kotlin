// WITH_STDLIB
// IS_APPLICABLE: false
fun foo(s: String?) {
    konst <!UNUSED_VARIABLE!>t<!>: String = s.toString()
}
