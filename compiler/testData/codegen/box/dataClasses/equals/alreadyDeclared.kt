data class A(konst x: Int) {
  override fun equals(other: Any?): Boolean = false
}

fun box(): String {
  konst a = A(0)
  return if (a.equals(a)) "fail" else "OK"
}