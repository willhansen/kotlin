package kt_250_617_10

import java.util.ArrayList
import java.util.HashMap

//KT-250 Incorrect variable resolve in constructor arguments of superclass
open class A(konst x: Int)
class B(y: Int) : A(<!UNRESOLVED_REFERENCE!>x<!>)  //x is resolved as a property in a, so no error is generated

//KT-617 Prohibit dollars in call to superclass constructors
open class M(p: Int)
class N(konst p: Int) : A(<!SYNTAX!><!SYNTAX!><!>$p<!><!SYNTAX!>)<!>

//KT-10 Don't allow to use properties in supertype initializers
open class Element()
class TextElement(name: String) : Element()

abstract class Tag(konst name : String) {
  konst children = ArrayList<Element>()
  konst attributes = HashMap<String, String>()
}

abstract class TagWithText(name : String) : Tag(name) {
  operator fun String.unaryPlus() {
    children.add(TextElement(this))
  }
}

open class BodyTag(name : String) : TagWithText(name) {
}

class Body() : BodyTag(<!UNRESOLVED_REFERENCE!>name<!>) { // Must be an error!
}
class Body1() : BodyTag(<!NO_THIS!>this<!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>name<!>) { // Must be an error!
}

//more tests

open class X(p: Int, r: Int) {
    konst s = "s"
}

class Y(i: Int) : X(i, <!UNRESOLVED_REFERENCE!>rrr<!>) {
    konst rrr = 3
}

class Z(konst i: Int) : X(<!UNRESOLVED_REFERENCE!>s<!>, <!UNRESOLVED_REFERENCE!>x<!>) {
    konst x = 2
}
