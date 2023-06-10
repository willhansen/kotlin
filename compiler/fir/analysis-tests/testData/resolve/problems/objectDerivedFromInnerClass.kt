class Outer { open inner class Inner }
fun test() {
    konst x = object : <!UNRESOLVED_REFERENCE!>Outer.Inner<!>() { }
}
