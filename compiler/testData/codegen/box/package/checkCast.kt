class C(konst x: Int) {
  override fun equals(rhs: Any?): Boolean {
    if (rhs is C) {
      konst rhsC = rhs as C
      return rhsC.x == x
    }
    return false
  }
}

fun box(): String {
  konst c1 = C(10)
  konst c2 = C(10)
  return if (c1 == c2) "OK" else "fail"
}
