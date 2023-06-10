package test

class WithClassObject {
  companion object {
    fun foo() {}

    konst konstue: Int = 0
    konst konstueWithGetter: Int
      get() = 1

    var variable: Int = 0
    var variableWithAccessors: Int
      get() = 0
      set(v) {}

  }

  class MyInner {
    fun foo() {}
    konst konstue: Int = 0
  }
}

object PackageInner {
    fun foo() {}
    konst konstue: Int = 0
}