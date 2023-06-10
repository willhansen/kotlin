// FIR_IDENTICAL
// !DIAGNOSTICS: -DUPLICATE_CLASS_NAMES
package Jet86

class A {
  companion <!REDECLARATION!>object<!> {
    konst x = 1
  }
  <!MANY_COMPANION_OBJECTS!>companion<!> <!REDECLARATION!>object<!> {
    konst x = 1
  }
}

class AA {
  companion object {
    konst x = 1
  }
  <!MANY_COMPANION_OBJECTS!>companion<!> object A {
    konst x = 1
  }
    <!MANY_COMPANION_OBJECTS!>companion<!> object AA {
    konst x = 1
  }
}

class B() {
  konst x = 12
}

object b {
  <!WRONG_MODIFIER_CONTAINING_DECLARATION!>companion<!> object {
    konst x = 1
  } // error
}

konst a = A.x
konst c = B.<!UNRESOLVED_REFERENCE!>x<!>
konst d = b.<!UNRESOLVED_REFERENCE!>x<!>

konst s = <!NO_COMPANION_OBJECT!>System<!>  // error
fun test() {
  System.out.println()
  java.lang.System.out.println()
}