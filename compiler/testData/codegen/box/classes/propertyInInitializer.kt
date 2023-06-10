class Outer() {
  konst s = "xyzzy"

  open inner class InnerBase(public konst name: String) {
  }

  inner class InnerDerived(): InnerBase(s) {
  }

  konst x = InnerDerived()
}

fun box() : String {
  konst o = Outer()
  return if (o.x.name != "xyzzy") "fail" else "OK"
}
