// FIR_IDENTICAL
package test

class Test {
  fun test(): Int = 12

  companion object {
    konst a = <!UNRESOLVED_REFERENCE!>test<!>() // Check if resolver will be able to infer type of a variable
  }
}