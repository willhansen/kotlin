// FILE: a.kt
package redeclarations
  object <!PACKAGE_OR_CLASSIFIER_REDECLARATION!>A<!> {
    konst x : Int = 0

    konst A = 1
  }

  class <!PACKAGE_OR_CLASSIFIER_REDECLARATION!>A<!> {}

  konst <!REDECLARATION!>A<!> = 1

// FILE: b.kt
  package redeclarations.A
    class A {}
