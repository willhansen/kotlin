fun test(x: Any?) {
  if (x !is String) return

  class C {
    konst v = <!DEBUG_INFO_SMARTCAST!>x<!>.length

    konst vGet: Int
      get() = <!DEBUG_INFO_SMARTCAST!>x<!>.length

    konst s: String = <!DEBUG_INFO_SMARTCAST!>x<!>
  }
}
