// FILE: f.kt
class A() {
  fun foo() : Unit {
    this@A
    this<!UNRESOLVED_LABEL!>@a<!>
    this
  }

  konst x = this@A.foo()
  konst y = this.foo()
  konst z = foo()
}
