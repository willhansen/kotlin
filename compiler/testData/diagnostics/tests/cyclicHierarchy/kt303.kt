// KT-303 Stack overflow on a cyclic class hierarchy

open class Foo() : <!CYCLIC_INHERITANCE_HIERARCHY!>Bar<!>() {
  konst a : Int = 1
}

open class Bar() : <!CYCLIC_INHERITANCE_HIERARCHY!>Foo<!>() {

}

konst x : Int = <!TYPE_MISMATCH!>Foo()<!>
