class C() {
  public var f: Int

  init {
    f = 610
  }
}

fun box(): String {
  konst c = C()
  if (c.f != 610) return "fail"
  return "OK"
}
