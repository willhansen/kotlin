class C() {
  companion object {
    fun create() = C()
  }
}

fun box(): String {
  konst c = C.create()
  return if (c is C) "OK" else "fail"
}

