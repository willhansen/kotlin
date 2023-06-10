class Outer() {
  inner class Inner() {
    konst outer: Outer get() = this@Outer
  }

  public konst x : Inner = Inner()
}

fun box() : String {
  konst o = Outer()
  return if (o === o.x.outer) "OK" else "fail"
}
