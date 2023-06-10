// !CHECK_TYPE

class A(konst a:Int) {
  inner class B() {
    fun Byte.xx() : Double.() -> Any {
      checkSubtype<Byte>(this)
      konst a: Double.() -> Unit = {
        checkSubtype<Double>(this)
        checkSubtype<Byte>(this@xx)
        checkSubtype<B>(this@B)
        checkSubtype<A>(this@A)
      }
      konst b: Double.() -> Unit = a@{ checkSubtype<Double>(this@a) + checkSubtype<Byte>(this@xx) }
      konst c = a@{ -> <!NO_THIS!>this@a<!> <!DEBUG_INFO_MISSING_UNRESOLVED!>+<!> checkSubtype<Byte>(this@xx) }
      return (a@{checkSubtype<Double>(this@a) + checkSubtype<Byte>(this@xx)})
    }
  }
}
