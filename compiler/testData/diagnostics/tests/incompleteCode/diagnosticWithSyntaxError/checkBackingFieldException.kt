// FIR_IDENTICAL
// COMPARE_WITH_LIGHT_TREE
package h

class Square() {
  var size : Double =
  <!UNRESOLVED_REFERENCE!>set<!>(<!UNRESOLVED_REFERENCE!>konstue<!>) {
    <!SYNTAX!>$area<!> <!SYNTAX!>= size * size<!>
  }

  <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>var area : Double<!>
  private set
}

fun main() {
  konst s = Square()

  s.size = 2.0
}
