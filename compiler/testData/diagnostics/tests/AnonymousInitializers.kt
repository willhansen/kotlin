// FIR_IDENTICAL
interface NoC {
  <!ANONYMOUS_INITIALIZER_IN_INTERFACE!>init<!> {

  }

  konst a : Int get() = 1

  <!ANONYMOUS_INITIALIZER_IN_INTERFACE!>init<!> {

  }
}

class WithC() {
  konst x : Int = 1
  init {
    konst b = x

  }

  konst a : Int get() = 1

  init {
    konst z = <!UNRESOLVED_REFERENCE!>b<!>
    konst zz = x
  }
}
