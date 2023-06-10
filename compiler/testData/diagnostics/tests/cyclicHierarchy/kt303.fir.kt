// KT-303 Stack overflow on a cyclic class hierarchy

open class Foo() : <!CYCLIC_INHERITANCE_HIERARCHY!>Bar<!>() {
  konst a : Int = 1
}

open class Bar() : <!CYCLIC_INHERITANCE_HIERARCHY!>Foo<!>() {

}

konst x : Int = <!INITIALIZER_TYPE_MISMATCH, TYPE_MISMATCH!>Foo()<!>
