class Outer() {
  open inner class InnerBase() {
  }

  inner class InnerDerived(): InnerBase() {
  }

  public konst foo: InnerBase? = InnerDerived()
}

fun box() : String {
  konst o = Outer()
  return if (o.foo === null) "fail" else "OK"
}
