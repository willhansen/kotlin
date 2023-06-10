// FILE: a.kt
package redeclarations
  object <!PACKAGE_OR_CLASSIFIER_REDECLARATION, REDECLARATION!>A<!> {
    konst x : Int = 0

    konst A = 1
  }

  class <!PACKAGE_OR_CLASSIFIER_REDECLARATION!>A<!> {}

  konst <!PACKAGE_OR_CLASSIFIER_REDECLARATION, REDECLARATION!>A<!> = 1

// FILE: b.kt
  package redeclarations.<!PACKAGE_OR_CLASSIFIER_REDECLARATION!>A<!>
    class A {}
