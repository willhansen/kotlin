fun test(x: Any?) {
  if (x !is String) return

  class C {
    konst v = x.length

    konst vGet: Int
      get() = x.length

    konst s: String = x
  }
}
