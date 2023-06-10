package foo

fun test() {
  A.d
  A.Companion.<!INVISIBLE_REFERENCE!>f<!>
  B.D
  CCC
  CCC.<!INVISIBLE_REFERENCE!>classObjectVar<!>
}

class A() {
  public companion object {
    konst d = 3
    private object f {

    }
  }
}

class B {
    class D {
        private companion object
    }
}

class CCC() {
  private companion object {
    konst classObjectVar = 3
  }
}
