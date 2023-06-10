// FILE: f.kt
class A() {
  fun foo() : Unit {
    this@A
    this<!UNRESOLVED_REFERENCE!>@a<!>
    this
  }

  konst x = this@A.<!DEBUG_INFO_LEAKING_THIS!>foo<!>()
  konst y = this.<!DEBUG_INFO_LEAKING_THIS!>foo<!>()
  konst z = <!DEBUG_INFO_LEAKING_THIS!>foo<!>()
}
