// FILE: 1.kt
package pp

private annotation class A(konst s: String)
private const konst foo = "O"

@A(foo)
fun f1() {}

@A(foo)
konst p1 = ""

@A(foo)
class C1


// FILE: 2.kt
package pp

@<!INVISIBLE_REFERENCE, INVISIBLE_REFERENCE!>A<!>(<!INVISIBLE_REFERENCE!>foo<!>)
fun f2() {}

@<!INVISIBLE_REFERENCE, INVISIBLE_REFERENCE!>A<!>(<!INVISIBLE_REFERENCE!>foo<!>)
konst p2 = ""

@<!INVISIBLE_REFERENCE, INVISIBLE_REFERENCE!>A<!>(<!INVISIBLE_REFERENCE!>foo<!>)
class C2
